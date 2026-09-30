package com.example.model

data class Episode(
    val id: String = "",
    val animeId: String = "",
    val seasonId: String = "",
    val episodeNumber: Int = 1,
    val title: String = "الحلقة 1",
    val description: String = "",
    val thumbnailUrl: String = "",
    val videoUrl: String = "",
    val alternativeVideoUrl: String = "",
    val duration: String = "24:00",
    val createdAt: Long = System.currentTimeMillis()
)
