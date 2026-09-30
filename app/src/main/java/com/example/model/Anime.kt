package com.example.model

data class Anime(
    val id: String = "",
    val title: String = "",
    val englishTitle: String = "",
    val description: String = "",
    val posterUrl: String = "",
    val bannerUrl: String = "",
    val year: Int = 2024,
    val status: String = "مستمر", // مستمر, مكتمل
    val genres: List<String> = emptyList(),
    val ageRating: String = "+13",
    val seasonsCount: Int = 1,
    val rating: Double = 8.5,
    val viewsCount: Long = 0,
    val featured: Boolean = false,
    val popular: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
