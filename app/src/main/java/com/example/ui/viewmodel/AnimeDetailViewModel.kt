package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnimeApp
import com.example.data.repository.AnimeRepository
import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Season
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AnimeDetailUiState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val isFavorite: Boolean = false,
    val seasons: List<Season> = emptyList(),
    val selectedSeason: Season? = null,
    val episodes: List<Episode> = emptyList(),
    val selectedEpisodeForAd: Episode? = null,
    val isAdDialogVisible: Boolean = false,
    val errorMessage: String? = null
)

class AnimeDetailViewModel(
    private val repository: AnimeRepository = AnimeApp.instance.animeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnimeDetailUiState())
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    fun loadAnimeDetails(animeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // Increment views
            repository.incrementAnimeViews(animeId)

            val anime = repository.getAnimeById(animeId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                anime = anime
            )

            // Observe favorite status
            repository.isFavorite(animeId).collect { isFav ->
                _uiState.value = _uiState.value.copy(isFavorite = isFav)
            }
        }

        // Observe seasons
        viewModelScope.launch {
            repository.getSeasonsForAnime(animeId).collect { seasonsList ->
                val currentSelected = _uiState.value.selectedSeason
                val selected = if (currentSelected != null && seasonsList.any { it.id == currentSelected.id }) {
                    currentSelected
                } else {
                    seasonsList.firstOrNull()
                }

                _uiState.value = _uiState.value.copy(
                    seasons = seasonsList,
                    selectedSeason = selected
                )

                if (selected != null) {
                    loadEpisodes(animeId, selected.id)
                }
            }
        }
    }

    fun selectSeason(season: Season) {
        val animeId = _uiState.value.anime?.id ?: return
        _uiState.value = _uiState.value.copy(selectedSeason = season)
        loadEpisodes(animeId, season.id)
    }

    private fun loadEpisodes(animeId: String, seasonId: String) {
        viewModelScope.launch {
            repository.getEpisodesForSeason(animeId, seasonId).collect { episodesList ->
                _uiState.value = _uiState.value.copy(episodes = episodesList)
            }
        }
    }

    fun toggleFavorite() {
        val anime = _uiState.value.anime ?: return
        val currentFav = _uiState.value.isFavorite
        viewModelScope.launch {
            repository.toggleFavorite(anime, currentFav)
        }
    }

    fun requestWatchEpisode(episode: Episode) {
        _uiState.value = _uiState.value.copy(
            selectedEpisodeForAd = episode,
            isAdDialogVisible = true
        )
    }

    fun dismissAdDialog() {
        _uiState.value = _uiState.value.copy(
            isAdDialogVisible = false,
            selectedEpisodeForAd = null
        )
    }
}
