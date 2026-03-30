package com.wisp.app.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.wisp.app.nostr.Nip68
import com.wisp.app.nostr.Nip71
import com.wisp.app.nostr.NostrEvent
import com.wisp.app.nostr.ProfileData
import com.wisp.app.repo.EventRepository
import com.wisp.app.repo.ZapDetail
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GALLERY_KINDS = setOf(20, 21, 22)

@Composable
fun GalleryCard(
    event: NostrEvent,
    profile: ProfileData?,
    onReply: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToProfile: ((String) -> Unit)? = null,
    onNoteClick: () -> Unit = {},
    onReact: (String) -> Unit = {},
    userReactionEmojis: Set<String> = emptySet(),
    onRepost: () -> Unit = {},
    onQuote: () -> Unit = {},
    hasUserReposted: Boolean = false,
    repostCount: Int = 0,
    onZap: () -> Unit = {},
    hasUserZapped: Boolean = false,
    likeCount: Int = 0,
    replyCount: Int = 0,
    zapSats: Long = 0,
    isZapAnimating: Boolean = false,
    isZapInProgress: Boolean = false,
    eventRepo: EventRepository? = null,
    relayIcons: List<Pair<String, String?>> = emptyList(),
    repostPubkeys: List<String> = emptyList(),
    reactionDetails: Map<String, List<String>> = emptyMap(),
    zapDetails: List<ZapDetail> = emptyList(),
    repostDetails: List<String> = emptyList(),
    reactionEmojiUrls: Map<String, String> = emptyMap(),
    resolvedEmojis: Map<String, String> = emptyMap(),
    unicodeEmojis: List<String> = emptyList(),
    onOpenEmojiLibrary: (() -> Unit)? = null,
    onNavigateToProfileFromDetails: ((String) -> Unit)? = null,
    onFollowAuthor: () -> Unit = {},
    onBlockAuthor: () -> Unit = {},
    isFollowingAuthor: Boolean = false,
    isOwnEvent: Boolean = false,
    onQuotedNoteClick: ((String) -> Unit)? = null,
    noteActions: NoteActions? = null,
    onAddToList: () -> Unit = {},
    isInList: Boolean = false,
    onPin: () -> Unit = {},
    isPinned: Boolean = false,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
    showDivider: Boolean = true
) {
    val title = remember(event.id) {
        Nip68.getTitle(event) ?: Nip71.getTitle(event)
    }

    val contentWarning = remember(event.id) {
        event.tags.firstOrNull { it.size >= 1 && it[0] == "content-warning" }
    }
    var contentRevealed by remember { mutableStateOf(false) }

    // Parse media entries
    val imageEntries = remember(event.id) { Nip68.parseImetaEntries(event) }
    val videoEntries = remember(event.id) { Nip71.parseVideoMeta(event) }

    // If no imeta tags, fall back to PostCard
    if (imageEntries.isEmpty() && videoEntries.isEmpty()) {
        PostCard(
            event = event,
            profile = profile,
            onReply = onReply,
            onProfileClick = onProfileClick,
            onNavigateToProfile = onNavigateToProfile,
            onNoteClick = onNoteClick,
            onReact = onReact,
            userReactionEmojis = userReactionEmojis,
            onRepost = onRepost,
            onQuote = onQuote,
            hasUserReposted = hasUserReposted,
            repostCount = repostCount,
            onZap = onZap,
            hasUserZapped = hasUserZapped,
            likeCount = likeCount,
            replyCount = replyCount,
            zapSats = zapSats,
            isZapAnimating = isZapAnimating,
            isZapInProgress = isZapInProgress,
            eventRepo = eventRepo,
            relayIcons = relayIcons,
            repostPubkeys = repostPubkeys,
            reactionDetails = reactionDetails,
            zapDetails = zapDetails,
            repostDetails = repostDetails,
            reactionEmojiUrls = reactionEmojiUrls,
            resolvedEmojis = resolvedEmojis,
            unicodeEmojis = unicodeEmojis,
            onOpenEmojiLibrary = onOpenEmojiLibrary,
            onNavigateToProfileFromDetails = onNavigateToProfileFromDetails,
            onFollowAuthor = onFollowAuthor,
            onBlockAuthor = onBlockAuthor,
            isFollowingAuthor = isFollowingAuthor,
            isOwnEvent = isOwnEvent,
            onQuotedNoteClick = onQuotedNoteClick,
            noteActions = noteActions,
            onAddToList = onAddToList,
            isInList = isInList,
            onPin = onPin,
            isPinned = isPinned,
            onDelete = onDelete,
            modifier = modifier,
            showDivider = showDivider
        )
        return
    }

    val displayName = remember(event.pubkey, profile?.displayString) {
        profile?.displayString
            ?: event.pubkey.take(8) + "..." + event.pubkey.takeLast(4)
    }
    val timestamp = remember(event.created_at) { formatGalleryTimestamp(event.created_at) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNoteClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Author header
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfilePicture(
                url = profile?.picture,
                size = 40,
                onClick = onProfileClick
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Title
        if (!title.isNullOrBlank()) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(8.dp))

        // Content warning overlay
        if (contentWarning != null && !contentRevealed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { contentRevealed = true },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Sensitive content",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Tap to reveal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else if (event.kind == 20 && imageEntries.isNotEmpty()) {
            // Picture event
            if (imageEntries.size == 1) {
                val entry = imageEntries[0]
                val aspectRatio = parseAspectRatio(entry.dim) ?: (4f / 3f)
                AsyncImage(
                    model = entry.url,
                    contentDescription = entry.alt ?: title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio.coerceIn(0.5f, 2.5f))
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                // Multi-image pager
                val pagerState = rememberPagerState(pageCount = { imageEntries.size })
                Column {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(4f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    ) { page ->
                        val entry = imageEntries[page]
                        AsyncImage(
                            model = entry.url,
                            contentDescription = entry.alt ?: title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (imageEntries.size > 1) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            repeat(imageEntries.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .padding(horizontal = 3.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == pagerState.currentPage)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        } else if (videoEntries.isNotEmpty()) {
            // Video event (kind 21/22)
            val video = videoEntries[0]
            val defaultAspect = if (event.kind == 22) 9f / 16f else 16f / 9f
            val aspectRatio = parseAspectRatio(video.dim) ?: defaultAspect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspectRatio.coerceIn(0.5f, 2.5f))
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Thumbnail or placeholder
                if (video.thumbnailUrl != null) {
                    AsyncImage(
                        model = video.thumbnailUrl,
                        contentDescription = title ?: "Video thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
                // Play button overlay
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play video",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // Description (event.content)
        if (event.content.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            var expanded by remember { mutableStateOf(false) }
            Text(
                text = event.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { expanded = !expanded }
            )
        }

        Spacer(Modifier.height(4.dp))

        // Action bar
        ActionBar(
            onReply = onReply,
            onReact = onReact,
            userReactionEmojis = userReactionEmojis,
            onRepost = onRepost,
            onQuote = onQuote,
            hasUserReposted = hasUserReposted,
            repostCount = repostCount,
            onZap = onZap,
            hasUserZapped = hasUserZapped,
            likeCount = likeCount,
            replyCount = replyCount,
            zapSats = zapSats,
            isZapAnimating = isZapAnimating,
            isZapInProgress = isZapInProgress,
            resolvedEmojis = resolvedEmojis,
            unicodeEmojis = unicodeEmojis,
            onOpenEmojiLibrary = onOpenEmojiLibrary
        )
    }

    if (showDivider) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    }
}

private fun parseAspectRatio(dim: String?): Float? {
    if (dim == null) return null
    val parts = dim.split("x")
    if (parts.size != 2) return null
    val w = parts[0].toFloatOrNull() ?: return null
    val h = parts[1].toFloatOrNull() ?: return null
    if (h == 0f) return null
    return w / h
}

private fun formatGalleryTimestamp(epochSeconds: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - epochSeconds * 1000
    return when {
        diff < 60_000 -> "now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        diff < 604_800_000 -> "${diff / 86_400_000}d"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(epochSeconds * 1000))
    }
}

fun isGalleryEvent(event: NostrEvent): Boolean = event.kind in GALLERY_KINDS
