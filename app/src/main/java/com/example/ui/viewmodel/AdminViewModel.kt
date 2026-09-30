package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnimeApp
import com.example.data.remote.AdminDashboardStats
import com.example.data.repository.AnimeRepository
import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Season
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val stats: AdminDashboardStats = AdminDashboardStats(),
    val selectedAnime: Anime? = null,
    val seasons: List<Season> = emptyList(),
    val selectedSeason: Season? = null,
    val episodes: List<Episode> = emptyList(),
    val actionSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class AdminViewModel(
    private val repository: AnimeRepository = AnimeApp.instance.animeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    val allAnime: StateFlow<List<Anime>> = repository.getAllAnime()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val stats = repository.getAdminDashboardStats()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                stats = stats
            )
        }
    }

    fun selectAnimeForEditing(anime: Anime?) {
        _uiState.value = _uiState.value.copy(
            selectedAnime = anime,
            selectedSeason = null,
            episodes = emptyList()
        )
        if (anime != null) {
            loadSeasonsForAnime(anime.id)
        }
    }

    fun loadSeasonsForAnime(animeId: String) {
        viewModelScope.launch {
            repository.getSeasonsForAnime(animeId).collect { list ->
                _uiState.value = _uiState.value.copy(
                    seasons = list,
                    selectedSeason = if (_uiState.value.selectedSeason == null) list.firstOrNull() else _uiState.value.selectedSeason
                )
                val sel = _uiState.value.selectedSeason
                if (sel != null) {
                    loadEpisodesForSeason(animeId, sel.id)
                }
            }
        }
    }

    fun selectSeason(season: Season) {
        val anime = _uiState.value.selectedAnime ?: return
        _uiState.value = _uiState.value.copy(selectedSeason = season)
        loadEpisodesForSeason(anime.id, season.id)
    }

    private fun loadEpisodesForSeason(animeId: String, seasonId: String) {
        viewModelScope.launch {
            repository.getEpisodesForSeason(animeId, seasonId).collect { list ->
                _uiState.value = _uiState.value.copy(episodes = list)
            }
        }
    }

    fun saveAnime(anime: Anime, onComplete: () -> Unit) {
        if (anime.title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "يرجى كتابة اسم الأنمي")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val animeId = repository.saveOrUpdateAnime(anime)
                // If it was a new anime, create Season 1 automatically
                if (anime.id.isBlank()) {
                    repository.saveOrUpdateSeason(
                        Season(
                            animeId = animeId,
                            seasonNumber = 1,
                            title = "الموسم 1",
                            episodesCount = 0
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionSuccessMessage = "تم حفظ الأنمي بنجاح!"
                )
                loadDashboardStats()
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "فشل الحفظ: ${e.localizedMessage ?: "خطأ غير متوقع"}"
                )
            }
        }
    }

    fun deleteAnime(animeId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAnime(animeId)
                _uiState.value = _uiState.value.copy(
                    actionSuccessMessage = "تم حذف الأنمي بنجاح"
                )
                loadDashboardStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "فشل حذف الأنمي: ${e.localizedMessage}"
                )
            }
        }
    }

    fun addSeason(animeId: String, seasonNumber: Int, title: String) {
        viewModelScope.launch {
            try {
                repository.saveOrUpdateSeason(
                    Season(
                        animeId = animeId,
                        seasonNumber = seasonNumber,
                        title = title.ifBlank { "الموسم $seasonNumber" }
                    )
                )
                _uiState.value = _uiState.value.copy(actionSuccessMessage = "تمت إضافة الموسم بنجاح")
                loadDashboardStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "فشل إضافة الموسم: ${e.localizedMessage}")
            }
        }
    }

    fun deleteSeason(animeId: String, seasonId: String) {
        viewModelScope.launch {
            try {
                repository.deleteSeason(animeId, seasonId)
                _uiState.value = _uiState.value.copy(actionSuccessMessage = "تم حذف الموسم")
                loadDashboardStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "فشل حذف الموسم: ${e.localizedMessage}")
            }
        }
    }

    fun saveEpisode(episode: Episode, onComplete: () -> Unit) {
        if (episode.title.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "يرجى كتابة عنوان الحلقة")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                repository.saveOrUpdateEpisode(episode)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    actionSuccessMessage = "تم حفظ الحلقة بنجاح!"
                )
                loadDashboardStats()
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "فشل حفظ الحلقة: ${e.localizedMessage}"
                )
            }
        }
    }

    fun deleteEpisode(animeId: String, seasonId: String, episodeId: String) {
        viewModelScope.launch {
            try {
                repository.deleteEpisode(animeId, seasonId, episodeId)
                _uiState.value = _uiState.value.copy(actionSuccessMessage = "تم حذف الحلقة")
                loadDashboardStats()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "فشل حذف الحلقة: ${e.localizedMessage}")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(actionSuccessMessage = null, errorMessage = null)
    }
}
