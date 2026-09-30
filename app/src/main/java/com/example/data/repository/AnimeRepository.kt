package com.example.data.repository

import android.content.Context
import com.example.data.local.AnimeDatabase
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.WatchHistoryEntity
import com.example.data.remote.AdminDashboardStats
import com.example.data.remote.FirebaseFirestoreService
import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Favorite
import com.example.model.Season
import com.example.model.WatchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AnimeRepository(
    private val context: Context,
    private val firestoreService: FirebaseFirestoreService = FirebaseFirestoreService(),
    private val database: AnimeDatabase = AnimeDatabase.getInstance(context)
) {
    private val watchHistoryDao = database.watchHistoryDao()
    private val favoriteDao = database.favoriteDao()
    private val downloadDao = database.downloadDao()
    private val cachedAnimeDao = database.cachedAnimeDao()

    // -------------------------------------------------------------
    // Anime Lists & Flow (No auto-seeding, completely admin-driven)
    // -------------------------------------------------------------
    fun getAllAnime(): Flow<List<Anime>> {
        return firestoreService.observeAllAnime().map { remoteList ->
            if (remoteList.isNotEmpty()) {
                // Update local Room cache
                val entities = remoteList.map { anime ->
                    CachedAnimeEntity(
                        id = anime.id,
                        title = anime.title,
                        englishTitle = anime.englishTitle,
                        description = anime.description,
                        posterUrl = anime.posterUrl,
                        bannerUrl = anime.bannerUrl,
                        year = anime.year,
                        status = anime.status,
                        genresCsv = anime.genres.joinToString(","),
                        ageRating = anime.ageRating,
                        seasonsCount = anime.seasonsCount,
                        rating = anime.rating,
                        viewsCount = anime.viewsCount,
                        featured = anime.featured,
                        popular = anime.popular,
                        cachedAt = System.currentTimeMillis()
                    )
                }
                cachedAnimeDao.insertAll(entities)
                remoteList
            } else {
                // Fallback to local Room cached animes if available
                val cached = cachedAnimeDao.getCachedAnimes().firstOrNull() ?: emptyList()
                cached.map { entity ->
                    Anime(
                        id = entity.id,
                        title = entity.title,
                        englishTitle = entity.englishTitle,
                        description = entity.description,
                        posterUrl = entity.posterUrl,
                        bannerUrl = entity.bannerUrl,
                        year = entity.year,
                        status = entity.status,
                        genres = if (entity.genresCsv.isNotBlank()) entity.genresCsv.split(",") else emptyList(),
                        ageRating = entity.ageRating,
                        seasonsCount = entity.seasonsCount,
                        rating = entity.rating,
                        viewsCount = entity.viewsCount,
                        featured = entity.featured,
                        popular = entity.popular
                    )
                }
            }
        }
    }

    suspend fun getAnimeById(id: String): Anime? = withContext(Dispatchers.IO) {
        firestoreService.getAnimeById(id)
    }

    suspend fun incrementAnimeViews(animeId: String) = withContext(Dispatchers.IO) {
        firestoreService.incrementAnimeViews(animeId)
    }

    fun getSeasonsForAnime(animeId: String): Flow<List<Season>> {
        return firestoreService.observeSeasonsForAnime(animeId)
    }

    fun getEpisodesForSeason(animeId: String, seasonId: String): Flow<List<Episode>> {
        return firestoreService.observeEpisodesForSeason(animeId, seasonId)
    }

    suspend fun getEpisodeById(animeId: String, seasonId: String, episodeId: String): Episode? = withContext(Dispatchers.IO) {
        firestoreService.getEpisodeById(animeId, seasonId, episodeId)
    }

    // -------------------------------------------------------------
    // Watch History (Room + Continue Watching)
    // -------------------------------------------------------------
    fun getWatchHistory(): Flow<List<WatchHistory>> {
        return watchHistoryDao.getAllHistory().map { list ->
            list.map { entity ->
                WatchHistory(
                    animeId = entity.animeId,
                    animeTitle = entity.animeTitle,
                    posterUrl = entity.posterUrl,
                    episodeId = entity.episodeId,
                    episodeNumber = entity.episodeNumber,
                    episodeTitle = entity.episodeTitle,
                    positionMs = entity.positionMs,
                    durationMs = entity.durationMs,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    suspend fun getLatestProgressForAnime(animeId: String): WatchHistory? = withContext(Dispatchers.IO) {
        watchHistoryDao.getLatestForAnime(animeId)?.let {
            WatchHistory(
                animeId = it.animeId,
                animeTitle = it.animeTitle,
                posterUrl = it.posterUrl,
                episodeId = it.episodeId,
                episodeNumber = it.episodeNumber,
                episodeTitle = it.episodeTitle,
                positionMs = it.positionMs,
                durationMs = it.durationMs,
                updatedAt = it.updatedAt
            )
        }
    }

    suspend fun saveWatchProgress(history: WatchHistory) = withContext(Dispatchers.IO) {
        watchHistoryDao.insertOrUpdate(
            WatchHistoryEntity(
                episodeId = history.episodeId,
                animeId = history.animeId,
                animeTitle = history.animeTitle,
                posterUrl = history.posterUrl,
                episodeNumber = history.episodeNumber,
                episodeTitle = history.episodeTitle,
                positionMs = history.positionMs,
                durationMs = history.durationMs,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // -------------------------------------------------------------
    // Favorites (Room Reactive Flow)
    // -------------------------------------------------------------
    fun getFavorites(): Flow<List<Favorite>> {
        return favoriteDao.getAllFavorites().map { list ->
            list.map { entity ->
                Favorite(
                    animeId = entity.animeId,
                    animeTitle = entity.animeTitle,
                    posterUrl = entity.posterUrl,
                    rating = entity.rating,
                    genres = if (entity.genresCsv.isNotBlank()) entity.genresCsv.split(",") else emptyList(),
                    status = entity.status,
                    addedAt = entity.addedAt
                )
            }
        }
    }

    fun isFavorite(animeId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(animeId)
    }

    suspend fun toggleFavorite(anime: Anime, isFav: Boolean) = withContext(Dispatchers.IO) {
        if (isFav) {
            favoriteDao.removeFavorite(anime.id)
        } else {
            favoriteDao.addFavorite(
                FavoriteEntity(
                    animeId = anime.id,
                    animeTitle = anime.title,
                    posterUrl = anime.posterUrl,
                    rating = anime.rating,
                    genresCsv = anime.genres.joinToString(","),
                    status = anime.status,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    // -------------------------------------------------------------
    // Admin Operations
    // -------------------------------------------------------------
    suspend fun saveOrUpdateAnime(anime: Anime): String = withContext(Dispatchers.IO) {
        firestoreService.saveOrUpdateAnime(anime)
    }

    suspend fun deleteAnime(animeId: String) = withContext(Dispatchers.IO) {
        firestoreService.deleteAnime(animeId)
    }

    suspend fun saveOrUpdateSeason(season: Season): String = withContext(Dispatchers.IO) {
        firestoreService.saveOrUpdateSeason(season)
    }

    suspend fun deleteSeason(animeId: String, seasonId: String) = withContext(Dispatchers.IO) {
        firestoreService.deleteSeason(animeId, seasonId)
    }

    suspend fun saveOrUpdateEpisode(episode: Episode): String = withContext(Dispatchers.IO) {
        firestoreService.saveOrUpdateEpisode(episode)
    }

    suspend fun deleteEpisode(animeId: String, seasonId: String, episodeId: String) = withContext(Dispatchers.IO) {
        firestoreService.deleteEpisode(animeId, seasonId, episodeId)
    }

    suspend fun getAdminDashboardStats(): AdminDashboardStats = withContext(Dispatchers.IO) {
        firestoreService.fetchAdminDashboardStats()
    }
}
