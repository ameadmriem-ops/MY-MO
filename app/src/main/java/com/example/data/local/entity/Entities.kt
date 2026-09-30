package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val episodeId: String,
    val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val positionMs: Long,
    val durationMs: Long,
    val updatedAt: Long
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val animeId: String,
    val animeTitle: String,
    val posterUrl: String,
    val rating: Double,
    val genresCsv: String,
    val status: String,
    val addedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val episodeId: String,
    val animeId: String,
    val animeTitle: String,
    val episodeTitle: String,
    val episodeNumber: Int,
    val localFilePath: String,
    val videoUrl: String,
    val progress: Int,
    val status: String,
    val totalSizeBytes: Long,
    val downloadedBytes: Long,
    val downloadedAt: Long
)

@Entity(tableName = "cached_anime")
data class CachedAnimeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val englishTitle: String,
    val description: String,
    val posterUrl: String,
    val bannerUrl: String,
    val year: Int,
    val status: String,
    val genresCsv: String,
    val ageRating: String,
    val seasonsCount: Int,
    val rating: Double,
    val viewsCount: Long,
    val featured: Boolean,
    val popular: Boolean,
    val cachedAt: Long
)
