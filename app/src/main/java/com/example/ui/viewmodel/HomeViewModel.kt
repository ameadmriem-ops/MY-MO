package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnimeApp
import com.example.data.repository.AnimeRepository
import com.example.model.Anime
import com.example.model.WatchHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeFeedState(
    val isLoading: Boolean = true,
    val featuredAnime: List<Anime> = emptyList(),
    val popularAnime: List<Anime> = emptyList(),
    val latestAnime: List<Anime> = emptyList(),
    val actionAnime: List<Anime> = emptyList(),
    val dramaAnime: List<Anime> = emptyList(),
    val fantasyAnime: List<Anime> = emptyList(),
    val ongoingAnime: List<Anime> = emptyList(),
    val completedAnime: List<Anime> = emptyList(),
    val continueWatching: List<WatchHistory> = emptyList(),
    val errorMessage: String? = null
)

class HomeViewModel(
    private val repository: AnimeRepository = AnimeApp.instance.animeRepository
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val feedState: StateFlow<HomeFeedState> = combine(
        repository.getAllAnime(),
        repository.getWatchHistory()
    ) { animeList, historyList ->
        if (animeList.isEmpty()) {
            HomeFeedState(isLoading = false)
        } else {
            val featured = animeList.filter { it.featured }.ifEmpty { animeList.take(3) }
            val popular = animeList.filter { it.popular || it.viewsCount > 1000 }.sortedByDescending { it.viewsCount }
            val latest = animeList.sortedByDescending { it.createdAt }
            val action = animeList.filter { anime -> anime.genres.any { it.contains("أكشن", ignoreCase = true) || it.contains("Action", ignoreCase = true) } }
            val drama = animeList.filter { anime -> anime.genres.any { it.contains("دراما", ignoreCase = true) || it.contains("Drama", ignoreCase = true) } }
            val fantasy = animeList.filter { anime -> anime.genres.any { it.contains("فانتازيا", ignoreCase = true) || it.contains("Fantasy", ignoreCase = true) || it.contains("خيال", ignoreCase = true) } }
            val ongoing = animeList.filter { it.status.contains("مستمر", ignoreCase = true) }
            val completed = animeList.filter { it.status.contains("مكتمل", ignoreCase = true) }

            HomeFeedState(
                isLoading = false,
                featuredAnime = featured,
                popularAnime = popular.ifEmpty { animeList.take(6) },
                latestAnime = latest,
                actionAnime = action.ifEmpty { animeList.take(4) },
                dramaAnime = drama.ifEmpty { animeList.take(4) },
                fantasyAnime = fantasy.ifEmpty { animeList.take(4) },
                ongoingAnime = ongoing,
                completedAnime = completed,
                continueWatching = historyList.take(8)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeFeedState(isLoading = true)
    )

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(800)
            _isRefreshing.value = false
        }
    }
}
