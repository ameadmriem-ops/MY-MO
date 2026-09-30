package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnimeApp
import com.example.data.repository.AnimeRepository
import com.example.model.Anime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class SearchViewModel(
    private val repository: AnimeRepository = AnimeApp.instance.animeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre.asStateFlow()

    val searchResults: StateFlow<List<Anime>> = combine(
        repository.getAllAnime(),
        _searchQuery,
        _selectedGenre
    ) { animeList, query, genre ->
        if (query.isBlank() && genre == null) {
            animeList
        } else {
            animeList.filter { anime ->
                val matchesQuery = if (query.isBlank()) true else {
                    anime.title.contains(query, ignoreCase = true) ||
                            anime.englishTitle.contains(query, ignoreCase = true) ||
                            anime.description.contains(query, ignoreCase = true) ||
                            anime.genres.any { it.contains(query, ignoreCase = true) }
                }
                val matchesGenre = if (genre == null) true else {
                    anime.genres.any { it.equals(genre, ignoreCase = true) }
                }
                matchesQuery && matchesGenre
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onGenreSelected(genre: String?) {
        _selectedGenre.value = if (_selectedGenre.value == genre) null else genre
    }
}
