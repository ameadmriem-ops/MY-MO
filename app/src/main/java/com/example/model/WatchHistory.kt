package com.example.model

data class WatchHistory(
    val animeId: String = "",
    val animeTitle: String = "",
    val posterUrl: String = "",
    val episodeId: String = "",
    val episodeNumber: Int = 1,
    val episodeTitle: String = "",
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
