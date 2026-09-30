package com.example.model

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED
}

data class DownloadItem(
    val episodeId: String = "",
    val animeId: String = "",
    val animeTitle: String = "",
    val episodeTitle: String = "",
    val episodeNumber: Int = 1,
    val localFilePath: String = "",
    val videoUrl: String = "",
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val totalSizeBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val downloadedAt: Long = System.currentTimeMillis()
)
