package com.wisp.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.wisp.app.nostr.ClientMessage
import com.wisp.app.nostr.Filter
import com.wisp.app.nostr.NostrEvent
import com.wisp.app.nostr.ProfileData
import com.wisp.app.relay.RelayConfig
import com.wisp.app.relay.RelayPool
import com.wisp.app.repo.EventRepository
import com.wisp.app.repo.KeyRepository
import com.wisp.app.repo.MuteRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class SearchFilter { PEOPLE, NOTES }

enum class RelayOption {
    DEFAULT,
    ALL_RELAYS,
    INDIVIDUAL
}

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val keyRepo = KeyRepository(app)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filter = MutableStateFlow(SearchFilter.PEOPLE)
    val filter: StateFlow<SearchFilter> = _filter

    // Relay selection
    private val _selectedRelayOption = MutableStateFlow(RelayOption.DEFAULT)
    val selectedRelayOption: StateFlow<RelayOption> = _selectedRelayOption

    private val _selectedRelayUrl = MutableStateFlow<String?>(null)
    val selectedRelayUrl: StateFlow<String?> = _selectedRelayUrl

    // User's search relays
    private val _searchRelays = MutableStateFlow(keyRepo.getSearchRelays())
    val searchRelays: StateFlow<List<String>> = _searchRelays

    // Results
    private val _users = MutableStateFlow<List<ProfileData>>(emptyList())
    val users: StateFlow<List<ProfileData>> = _users

    private val _notes = MutableStateFlow<List<NostrEvent>>(emptyList())
    val notes: StateFlow<List<NostrEvent>> = _notes

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private var searchJob: Job? = null
    private var relayPool: RelayPool? = null
    private var searchCounter = 0

    private var userSubId = "search-users-0"
    private var noteSubId = "search-notes-0"

    fun selectFilter(filter: SearchFilter) {
        _filter.value = filter
    }

    fun selectDefaultRelay() {
        _selectedRelayOption.value = RelayOption.DEFAULT
        _selectedRelayUrl.value = null
    }

    fun selectAllRelays() {
        _selectedRelayOption.value = RelayOption.ALL_RELAYS
        _selectedRelayUrl.value = null
    }

    fun selectRelay(url: String) {
        _selectedRelayOption.value = RelayOption.INDIVIDUAL
        _selectedRelayUrl.value = url
    }

    fun addSearchRelay(url: String): Boolean {
        val trimmed = url.trim().trimEnd('/')
        if (!RelayConfig.isValidUrl(trimmed)) return false
        if (trimmed in _searchRelays.value) return false
        val updated = _searchRelays.value + trimmed
        keyRepo.saveSearchRelays(updated)
        _searchRelays.value = updated
        return true
    }

    fun removeSearchRelay(url: String) {
        val updated = _searchRelays.value - url
        keyRepo.saveSearchRelays(updated)
        _searchRelays.value = updated
        if (_selectedRelayOption.value == RelayOption.INDIVIDUAL && _selectedRelayUrl.value == url) {
            selectDefaultRelay()
        }
    }

    fun updateQuery(newQuery: String) {
        _query.value = newQuery
    }

    fun search(query: String, relayPool: RelayPool, eventRepo: EventRepository, muteRepo: MuteRepository? = null) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            clear()
            return
        }

        searchJob?.cancel()
        this.relayPool = relayPool

        closeSubscriptions(relayPool)

        searchCounter++
        userSubId = "search-users-$searchCounter"
        noteSubId = "search-notes-$searchCounter"

        _query.value = trimmed
        _isSearching.value = true
        _users.value = emptyList()
        _notes.value = emptyList()

        val userFilter = Filter(kinds = listOf(0), search = trimmed, limit = 20)
        val noteFilter = Filter(kinds = listOf(1), search = trimmed, limit = 50)

        val userReq = ClientMessage.req(userSubId, userFilter)
        val noteReq = ClientMessage.req(noteSubId, noteFilter)

        val relaysToQuery = when (_selectedRelayOption.value) {
            RelayOption.DEFAULT -> listOf(DEFAULT_SEARCH_RELAY)
            RelayOption.ALL_RELAYS -> _searchRelays.value
            RelayOption.INDIVIDUAL -> listOfNotNull(_selectedRelayUrl.value)
        }

        for (url in relaysToQuery) {
            relayPool.sendToRelayOrEphemeral(url, userReq)
            relayPool.sendToRelayOrEphemeral(url, noteReq)
        }

        val seenUserPubkeys = mutableSetOf<String>()
        val seenNoteIds = mutableSetOf<String>()
        var userEose = false
        var noteEose = false

        searchJob = viewModelScope.launch {
            val eventJob = launch {
                relayPool.relayEvents.collect { relayEvent ->
                    when (relayEvent.subscriptionId) {
                        userSubId -> {
                            val event = relayEvent.event
                            if (event.kind == 0 && event.pubkey !in seenUserPubkeys) {
                                if (muteRepo?.isBlocked(event.pubkey) == true) return@collect
                                seenUserPubkeys.add(event.pubkey)
                                eventRepo.cacheEvent(event)
                                val profile = ProfileData.fromEvent(event)
                                if (profile != null) {
                                    _users.value = _users.value + profile
                                }
                            }
                        }
                        noteSubId -> {
                            val event = relayEvent.event
                            if (event.kind == 1 && event.id !in seenNoteIds) {
                                if (muteRepo?.isBlocked(event.pubkey) == true) return@collect
                                seenNoteIds.add(event.id)
                                _notes.value = _notes.value + event
                                eventRepo.cacheEvent(event)
                            }
                        }
                    }
                }
            }

            val eoseJob = launch {
                relayPool.eoseSignals.collect { subId ->
                    when (subId) {
                        userSubId -> userEose = true
                        noteSubId -> noteEose = true
                    }
                    if (userEose && noteEose) {
                        _isSearching.value = false
                    }
                }
            }

            delay(5000)
            _isSearching.value = false
            closeSubscriptions(relayPool)
            eventJob.cancel()
            eoseJob.cancel()
        }
    }

    fun clear() {
        searchJob?.cancel()
        relayPool?.let { closeSubscriptions(it) }
        _query.value = ""
        _users.value = emptyList()
        _notes.value = emptyList()
        _isSearching.value = false
    }

    companion object {
        const val DEFAULT_SEARCH_RELAY = "wss://search.nostrarchives.com"
    }

    private fun closeSubscriptions(relayPool: RelayPool) {
        relayPool.closeOnAllRelays(userSubId)
        relayPool.closeOnAllRelays(noteSubId)
    }
}
