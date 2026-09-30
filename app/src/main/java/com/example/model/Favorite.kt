package com.example.model

data class Favorite(
    val animeId: String = "",
    val animeTitle: String = "",
    val posterUrl: String = "",
    val rating: Double = 0.0,
    val genres: List<String> = emptyList(),
    val status: String = "",
    val addedAt: Long = System.currentTimeMillis()
)
