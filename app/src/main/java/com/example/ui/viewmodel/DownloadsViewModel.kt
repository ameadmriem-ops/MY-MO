package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.AnimeApp
import com.example.download.AnimeDownloadManager
import com.example.model.DownloadItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DownloadsViewModel(
    private val downloadManager: AnimeDownloadManager = AnimeApp.instance.downloadManager
) : ViewModel() {

    val downloads: StateFlow<List<DownloadItem>> = downloadManager.getDownloads()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteDownload(episodeId: String) {
        viewModelScope.launch {
            downloadManager.deleteDownload(episodeId)
        }
    }
}
