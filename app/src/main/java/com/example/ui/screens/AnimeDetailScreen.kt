package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.AnimeApp
import com.example.R
import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Season
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RewardedAdDialog
import com.example.ui.theme.AnimeCrimson
import com.example.ui.theme.AnimeCyan
import com.example.ui.theme.AnimeGold
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AnimeDetailViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimeDetailScreen(
    animeId: String,
    detailViewModel: AnimeDetailViewModel,
    onBackClick: () -> Unit,
    onPlayEpisode: (animeId: String, seasonId: String, episodeId: String, animeTitle: String, posterUrl: String) -> Unit
) {
    val context = LocalContext.current
    val uiState by detailViewModel.uiState.collectAsState()

    BackHandler { onBackClick() }

    LaunchedEffect(animeId) {
        detailViewModel.loadAnimeDetails(animeId)
    }

    val anime = uiState.anime

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        if (uiState.isLoading || anime == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AnimeCrimson)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                // Header Banner Image
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        AsyncImage(
                            model = anime.bannerUrl.ifBlank { anime.posterUrl },
                            contentDescription = anime.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.hero_banner_art)
                        )

                        // Top Back Button & Favorite
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onBackClick,
                                modifier = Modifier
                                    .testTag("detail_back_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkBg.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = TextPrimary
                                )
                            }

                            IconButton(
                                onClick = { detailViewModel.toggleFavorite() },
                                modifier = Modifier
                                    .testTag("detail_fav_button")
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkBg.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = "Favorite",
                                    tint = if (uiState.isFavorite) AnimeCrimson else TextPrimary
                                )
                            }
                        }

                        // Gradient fade to DarkBg
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, DarkBg)
                                    )
                                )
                        )
                    }
                }

                // Title & Info Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = anime.title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                lineHeight = 30.sp
                            )
                        )

                        if (anime.englishTitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = anime.englishTitle,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Meta Badges (Rating, Year, Status, Age, Views)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = AnimeGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${anime.rating}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }

                            Text(
                                text = "${anime.year}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )

                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )

                            Text(
                                text = anime.status,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (anime.status.contains("مستمر")) AnimeCyan else Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )

                            Text(
                                text = "${anime.viewsCount} مشاهدة",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Genres FlowRow
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            anime.genres.forEach { genre ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = DarkSurfaceVariant,
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(DarkCardBorder, DarkCardBorder)))
                                ) {
                                    Text(
                                        text = genre,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Synopsis
                        Text(
                            text = anime.description,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    val firstEp = uiState.episodes.firstOrNull()
                                    if (firstEp != null) {
                                        detailViewModel.requestWatchEpisode(firstEp)
                                    } else {
                                        Toast.makeText(context, "لا توجد حلقات متوفرة حالياً", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("detail_watch_first_ep_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = AnimeCrimson),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.watch_now),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    detailViewModel.toggleFavorite()
                                },
                                modifier = Modifier.height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.horizontalGradient(listOf(DarkCardBorder, DarkCardBorder)))
                            ) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                    contentDescription = null,
                                    tint = if (uiState.isFavorite) AnimeCrimson else TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Seasons Header & Tabs
                        Text(
                            text = stringResource(R.string.seasons),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.seasons, key = { it.id }) { season ->
                                val isSelected = uiState.selectedSeason?.id == season.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { detailViewModel.selectSeason(season) },
                                    label = {
                                        Text(
                                            text = season.title,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AnimeCrimson,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurface,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = if (isSelected) AnimeCrimson else DarkCardBorder,
                                        selectedBorderColor = AnimeCrimson,
                                        enabled = true,
                                        selected = isSelected
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Episodes Header
                        Text(
                            text = "${stringResource(R.string.episodes)} (${uiState.episodes.size})",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Episode List Items
                if (uiState.episodes.isEmpty()) {
                    item {
                        EmptyStateView(
                            message = "لا توجد حلقات مضافة لهذا الموسم بعد.",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    items(uiState.episodes, key = { it.id }) { episode ->
                        EpisodeCardItem(
                            episode = episode,
                            anime = anime,
                            onPlayClick = {
                                detailViewModel.requestWatchEpisode(episode)
                            },
                            onDownloadClick = {
                                AnimeApp.instance.downloadManager.startDownload(anime.id, anime.title, episode)
                                Toast.makeText(context, "بدأ تنزيل الحلقة ${episode.episodeNumber}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Rewarded Ad Dialog Interceptor
        if (uiState.isAdDialogVisible && uiState.selectedEpisodeForAd != null) {
            val targetEpisode = uiState.selectedEpisodeForAd!!
            RewardedAdDialog(
                episode = targetEpisode,
                onWatchAdClicked = {
                    detailViewModel.dismissAdDialog()
                    val activity = context as? Activity
                    if (activity != null) {
                        AnimeApp.instance.adManager.showRewardedAd(
                            activity = activity,
                            onRewardEarned = {
                                onPlayEpisode(
                                    anime!!.id,
                                    targetEpisode.seasonId,
                                    targetEpisode.id,
                                    anime.title,
                                    anime.posterUrl
                                )
                            },
                            onAdDismissed = {
                                onPlayEpisode(
                                    anime!!.id,
                                    targetEpisode.seasonId,
                                    targetEpisode.id,
                                    anime.title,
                                    anime.posterUrl
                                )
                            },
                            onErrorOrFallback = { msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                onPlayEpisode(
                                    anime!!.id,
                                    targetEpisode.seasonId,
                                    targetEpisode.id,
                                    anime.title,
                                    anime.posterUrl
                                )
                            }
                        )
                    } else {
                        onPlayEpisode(
                            anime!!.id,
                            targetEpisode.seasonId,
                            targetEpisode.id,
                            anime.title,
                            anime.posterUrl
                        )
                    }
                },
                onDismiss = {
                    detailViewModel.dismissAdDialog()
                }
            )
        }
    }
}

@Composable
fun EpisodeCardItem(
    episode: Episode,
    anime: Anime,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onPlayClick() }
            .testTag("episode_card_${episode.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(DarkCardBorder, Color.Transparent)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail with Play overlay
            Box(
                modifier = Modifier
                    .size(width = 90.dp, height = 62.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = episode.thumbnailUrl.ifBlank { anime.posterUrl },
                    contentDescription = episode.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.empty_state_anime)
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Duration badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(3.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = episode.duration,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontSize = 9.sp
                        ),
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "الحلقة ${episode.episodeNumber}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = AnimeCyan
                    )
                )
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (episode.description.isNotBlank()) {
                    Text(
                        text = episode.description,
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Download Button
            IconButton(
                onClick = onDownloadClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Download,
                    contentDescription = "Download",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
