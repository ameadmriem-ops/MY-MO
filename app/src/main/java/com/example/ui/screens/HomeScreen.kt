package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Anime
import com.example.model.User
import com.example.model.WatchHistory
import com.example.ui.components.AnimeSectionRow
import com.example.ui.components.AnimeTopHeader
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.ErrorStateView
import com.example.ui.components.HeroBannerSlider
import com.example.ui.theme.AnimeCrimson
import com.example.ui.theme.DarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    currentUser: User?,
    onAnimeClick: (Anime) -> Unit,
    onWatchHistoryClick: (WatchHistory) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit
) {
    val feedState by homeViewModel.feedState.collectAsState()
    val isRefreshing by homeViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        // Top Header
        AnimeTopHeader(
            currentUser = currentUser,
            onSearchClick = onSearchClick,
            onProfileClick = onProfileClick,
            onAdminClick = onAdminClick
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { homeViewModel.refresh() },
            state = pullRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (feedState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AnimeCrimson)
                }
            } else if (feedState.errorMessage != null) {
                ErrorStateView(
                    message = feedState.errorMessage ?: "حدث خطأ أثناء تحميل البيانات",
                    onRetry = { homeViewModel.refresh() }
                )
            } else if (feedState.featuredAnime.isEmpty() && feedState.latestAnime.isEmpty() && feedState.popularAnime.isEmpty()) {
                EmptyStateView(
                    message = if (currentUser?.isAdmin == true)
                        "مرحباً بك في لوحة التحكم! لا توجد أنميات مضافة بعد. يمكنك البدء بإضافة الأنميات والمواسم والحلقات الآن."
                    else
                        "لا توجد أنميات متوفرة حالياً. سيتم إضافة محتوى جديد قريباً!",
                    icon = Icons.Filled.Movie,
                    actionButtonText = if (currentUser?.isAdmin == true) "فتح لوحة الإدارة لإضافة أنمي" else null,
                    onActionClick = if (currentUser?.isAdmin == true) onAdminClick else null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    // 1. Hero Banner Slider
                    if (feedState.featuredAnime.isNotEmpty()) {
                        item {
                            HeroBannerSlider(
                                featuredList = feedState.featuredAnime,
                                onWatchClick = { onAnimeClick(it) },
                                onDetailsClick = { onAnimeClick(it) }
                            )
                        }
                    }

                    // 2. Continue Watching Row
                    if (feedState.continueWatching.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = stringResource(R.string.section_continue_watching),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = TextPrimary
                                    ),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )

                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(feedState.continueWatching, key = { it.episodeId }) { item ->
                                        ContinueWatchingCard(
                                            history = item,
                                            onClick = { onWatchHistoryClick(item) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 3. Most Popular / الأكثر مشاهدة
                    if (feedState.popularAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_popular),
                                animeList = feedState.popularAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // 4. Latest Added / المضافة حديثاً
                    if (feedState.latestAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_latest),
                                animeList = feedState.latestAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // 5. Action & Adventures / أكشن ومغامرات
                    if (feedState.actionAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_action),
                                animeList = feedState.actionAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // 6. Fantasy / فانتازيا وخيال
                    if (feedState.fantasyAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_fantasy),
                                animeList = feedState.fantasyAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // 7. Ongoing / مستمر حالياً
                    if (feedState.ongoingAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_ongoing),
                                animeList = feedState.ongoingAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }

                    // 8. Completed / مكتمل
                    if (feedState.completedAnime.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            AnimeSectionRow(
                                title = stringResource(R.string.section_completed),
                                animeList = feedState.completedAnime,
                                onAnimeClick = onAnimeClick
                            )
                        }
                    }
                }
            }
        }
    }
}
