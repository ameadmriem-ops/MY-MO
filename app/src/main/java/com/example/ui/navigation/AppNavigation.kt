package com.example.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.Anime
import com.example.ui.components.AnimeBottomBar
import com.example.ui.screens.AdminAnimeFormScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminEpisodesScreen
import com.example.ui.screens.AnimeDetailScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AnimeDetailViewModel
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DownloadsViewModel
import com.example.ui.viewmodel.FavoritesViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SearchViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    adminViewModel: AdminViewModel = viewModel(),
    searchViewModel: SearchViewModel = viewModel(),
    favoritesViewModel: FavoritesViewModel = viewModel(),
    downloadsViewModel: DownloadsViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val currentUser by authViewModel.currentUser.collectAsState()
    val authUiState by authViewModel.uiState.collectAsState()

    var selectedAnimeForAdminEdit by remember { mutableStateOf<Anime?>(null) }
    var selectedAnimeForEpisodes by remember { mutableStateOf<Anime?>(null) }

    val bottomNavRoutes = setOf("home", "search", "favorites", "downloads", "profile")
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AnimeBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Splash Screen
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        if (currentUser != null || authUiState is AuthUiState.Authenticated) {
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                            }
                        } else {
                            navController.navigate("auth") {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Auth Screen
            composable("auth") {
                AuthScreen(
                    authViewModel = authViewModel,
                    onAuthSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen
            composable("home") {
                HomeScreen(
                    homeViewModel = homeViewModel,
                    currentUser = currentUser,
                    onAnimeClick = { anime ->
                        navController.navigate("anime_detail/${anime.id}")
                    },
                    onWatchHistoryClick = { history ->
                        val encodedTitle = Uri.encode(history.animeTitle)
                        val encodedPoster = Uri.encode(history.posterUrl)
                        navController.navigate("video_player/${history.animeId}/season_1/${history.episodeId}/$encodedTitle/$encodedPoster")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                    onProfileClick = {
                        navController.navigate("profile")
                    },
                    onAdminClick = {
                        navController.navigate("admin_dashboard")
                    }
                )
            }

            // Search Screen
            composable("search") {
                SearchScreen(
                    searchViewModel = searchViewModel,
                    onAnimeClick = { anime ->
                        navController.navigate("anime_detail/${anime.id}")
                    }
                )
            }

            // Favorites Screen
            composable("favorites") {
                FavoritesScreen(
                    favoritesViewModel = favoritesViewModel,
                    onAnimeClick = { anime ->
                        navController.navigate("anime_detail/${anime.id}")
                    }
                )
            }

            // Downloads Screen
            composable("downloads") {
                DownloadsScreen(
                    downloadsViewModel = downloadsViewModel,
                    onPlayOfflineEpisode = { episodeId, title, localPath ->
                        val encodedTitle = Uri.encode(title)
                        val encodedPath = Uri.encode(localPath)
                        navController.navigate("video_player/offline/offline/$episodeId/$encodedTitle/$encodedPath")
                    }
                )
            }

            // Profile Screen
            composable("profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onAdminClick = {
                        navController.navigate("admin_dashboard")
                    },
                    onSettingsClick = {
                        navController.navigate("settings")
                    },
                    onFavoritesClick = {
                        navController.navigate("favorites")
                    },
                    onDownloadsClick = {
                        navController.navigate("downloads")
                    },
                    onLoggedOut = {
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
            }

            // Settings Screen
            composable("settings") {
                SettingsScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Anime Detail Screen
            composable(
                route = "anime_detail/{animeId}",
                arguments = listOf(navArgument("animeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                val detailViewModel: AnimeDetailViewModel = viewModel()
                AnimeDetailScreen(
                    animeId = animeId,
                    detailViewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPlayEpisode = { aid, seasonId, episodeId, animeTitle, posterUrl ->
                        val encodedTitle = Uri.encode(animeTitle)
                        val encodedPoster = Uri.encode(posterUrl)
                        navController.navigate("video_player/$aid/$seasonId/$episodeId/$encodedTitle/$encodedPoster")
                    }
                )
            }

            // Video Player Screen
            composable(
                route = "video_player/{animeId}/{seasonId}/{episodeId}/{animeTitle}/{posterUrl}",
                arguments = listOf(
                    navArgument("animeId") { type = NavType.StringType },
                    navArgument("seasonId") { type = NavType.StringType },
                    navArgument("episodeId") { type = NavType.StringType },
                    navArgument("animeTitle") { type = NavType.StringType },
                    navArgument("posterUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val animeId = backStackEntry.arguments?.getString("animeId") ?: ""
                val seasonId = backStackEntry.arguments?.getString("seasonId") ?: ""
                val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                val animeTitle = Uri.decode(backStackEntry.arguments?.getString("animeTitle") ?: "")
                val posterUrl = Uri.decode(backStackEntry.arguments?.getString("posterUrl") ?: "")

                val playerViewModel: PlayerViewModel = viewModel()
                VideoPlayerScreen(
                    animeId = animeId,
                    seasonId = seasonId,
                    episodeId = episodeId,
                    animeTitle = animeTitle,
                    posterUrl = posterUrl,
                    playerViewModel = playerViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Admin Dashboard Screen
            composable("admin_dashboard") {
                AdminDashboardScreen(
                    adminViewModel = adminViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAddAnimeClick = {
                        selectedAnimeForAdminEdit = null
                        navController.navigate("admin_anime_form")
                    },
                    onEditAnimeClick = { anime ->
                        selectedAnimeForAdminEdit = anime
                        navController.navigate("admin_anime_form")
                    },
                    onManageEpisodesClick = { anime ->
                        selectedAnimeForEpisodes = anime
                        navController.navigate("admin_episodes")
                    }
                )
            }

            // Admin Anime Form Screen
            composable("admin_anime_form") {
                AdminAnimeFormScreen(
                    adminViewModel = adminViewModel,
                    animeToEdit = selectedAnimeForAdminEdit,
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Admin Episodes Screen
            composable("admin_episodes") {
                val anime = selectedAnimeForEpisodes
                if (anime != null) {
                    AdminEpisodesScreen(
                        adminViewModel = adminViewModel,
                        anime = anime,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
