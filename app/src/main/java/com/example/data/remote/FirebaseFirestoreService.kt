package com.example.data.remote

import com.example.model.Anime
import com.example.model.Episode
import com.example.model.Favorite
import com.example.model.Season
import com.example.model.WatchHistory
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class AdminDashboardStats(
    val totalUsers: Long = 0,
    val totalAnime: Long = 0,
    val totalEpisodes: Long = 0,
    val totalViews: Long = 0,
    val recentAnime: List<Anime> = emptyList()
)

class FirebaseFirestoreService {

    private val firestore: FirebaseFirestore?
        get() = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }

    // -------------------------------------------------------------
    // Anime Queries (Real-time Flow)
    // -------------------------------------------------------------
    fun observeAllAnime(): Flow<List<Anime>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("anime")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Anime(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            englishTitle = doc.getString("englishTitle") ?: "",
                            description = doc.getString("description") ?: "",
                            posterUrl = doc.getString("posterUrl") ?: "",
                            bannerUrl = doc.getString("bannerUrl") ?: "",
                            year = doc.getLong("year")?.toInt() ?: 2024,
                            status = doc.getString("status") ?: "مستمر",
                            genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                            ageRating = doc.getString("ageRating") ?: "+13",
                            seasonsCount = doc.getLong("seasonsCount")?.toInt() ?: 1,
                            rating = doc.getDouble("rating") ?: 8.5,
                            viewsCount = doc.getLong("viewsCount") ?: 0,
                            featured = doc.getBoolean("featured") ?: false,
                            popular = doc.getBoolean("popular") ?: false,
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getAnimeById(id: String): Anime? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection("anime").document(id).get().await()
            if (doc.exists()) {
                Anime(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    englishTitle = doc.getString("englishTitle") ?: "",
                    description = doc.getString("description") ?: "",
                    posterUrl = doc.getString("posterUrl") ?: "",
                    bannerUrl = doc.getString("bannerUrl") ?: "",
                    year = doc.getLong("year")?.toInt() ?: 2024,
                    status = doc.getString("status") ?: "مستمر",
                    genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    ageRating = doc.getString("ageRating") ?: "+13",
                    seasonsCount = doc.getLong("seasonsCount")?.toInt() ?: 1,
                    rating = doc.getDouble("rating") ?: 8.5,
                    viewsCount = doc.getLong("viewsCount") ?: 0,
                    featured = doc.getBoolean("featured") ?: false,
                    popular = doc.getBoolean("popular") ?: false,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun incrementAnimeViews(animeId: String) {
        val db = firestore ?: return
        try {
            db.collection("anime").document(animeId)
                .update("viewsCount", FieldValue.increment(1))
                .await()
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // Seasons
    // -------------------------------------------------------------
    fun observeSeasonsForAnime(animeId: String): Flow<List<Season>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("anime").document(animeId)
            .collection("seasons")
            .orderBy("seasonNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Season(
                            id = doc.id,
                            animeId = animeId,
                            seasonNumber = doc.getLong("seasonNumber")?.toInt() ?: 1,
                            title = doc.getString("title") ?: "الموسم 1",
                            episodesCount = doc.getLong("episodesCount")?.toInt() ?: 0
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // -------------------------------------------------------------
    // Episodes
    // -------------------------------------------------------------
    fun observeEpisodesForSeason(animeId: String, seasonId: String): Flow<List<Episode>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }
        val listener = db.collection("anime").document(animeId)
            .collection("seasons").document(seasonId)
            .collection("episodes")
            .orderBy("episodeNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Episode(
                            id = doc.id,
                            animeId = animeId,
                            seasonId = seasonId,
                            episodeNumber = doc.getLong("episodeNumber")?.toInt() ?: 1,
                            title = doc.getString("title") ?: "الحلقة 1",
                            description = doc.getString("description") ?: "",
                            thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                            videoUrl = doc.getString("videoUrl") ?: "",
                            alternativeVideoUrl = doc.getString("alternativeVideoUrl") ?: "",
                            duration = doc.getString("duration") ?: "24:00",
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getEpisodeById(animeId: String, seasonId: String, episodeId: String): Episode? {
        val db = firestore ?: return null
        return try {
            val doc = db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId)
                .collection("episodes").document(episodeId).get().await()
            if (doc.exists()) {
                Episode(
                    id = doc.id,
                    animeId = animeId,
                    seasonId = seasonId,
                    episodeNumber = doc.getLong("episodeNumber")?.toInt() ?: 1,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    thumbnailUrl = doc.getString("thumbnailUrl") ?: "",
                    videoUrl = doc.getString("videoUrl") ?: "",
                    alternativeVideoUrl = doc.getString("alternativeVideoUrl") ?: "",
                    duration = doc.getString("duration") ?: "24:00",
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    // -------------------------------------------------------------
    // Admin Operations (CRUD)
    // -------------------------------------------------------------
    suspend fun saveOrUpdateAnime(anime: Anime): String {
        val db = firestore ?: return anime.id.ifBlank { "local_${System.currentTimeMillis()}" }
        val docRef = if (anime.id.isBlank()) {
            db.collection("anime").document()
        } else {
            db.collection("anime").document(anime.id)
        }

        val data = hashMapOf(
            "title" to anime.title,
            "englishTitle" to anime.englishTitle,
            "description" to anime.description,
            "posterUrl" to anime.posterUrl,
            "bannerUrl" to anime.bannerUrl,
            "year" to anime.year,
            "status" to anime.status,
            "genres" to anime.genres,
            "ageRating" to anime.ageRating,
            "seasonsCount" to anime.seasonsCount,
            "rating" to anime.rating,
            "featured" to anime.featured,
            "popular" to anime.popular,
            "createdAt" to if (anime.createdAt > 0) anime.createdAt else System.currentTimeMillis()
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun deleteAnime(animeId: String) {
        val db = firestore ?: return
        try {
            val seasonsSnapshot = db.collection("anime").document(animeId).collection("seasons").get().await()
            for (seasonDoc in seasonsSnapshot.documents) {
                val episodesSnapshot = seasonDoc.reference.collection("episodes").get().await()
                for (epDoc in episodesSnapshot.documents) {
                    epDoc.reference.delete().await()
                }
                seasonDoc.reference.delete().await()
            }
            db.collection("anime").document(animeId).delete().await()
        } catch (_: Exception) {}
    }

    suspend fun saveOrUpdateSeason(season: Season): String {
        val db = firestore ?: return season.id.ifBlank { "local_season_${System.currentTimeMillis()}" }
        val seasonsCol = db.collection("anime").document(season.animeId).collection("seasons")
        val docRef = if (season.id.isBlank()) seasonsCol.document() else seasonsCol.document(season.id)

        val data = hashMapOf(
            "seasonNumber" to season.seasonNumber,
            "title" to season.title,
            "episodesCount" to season.episodesCount
        )
        docRef.set(data).await()
        return docRef.id
    }

    suspend fun deleteSeason(animeId: String, seasonId: String) {
        val db = firestore ?: return
        try {
            val epSnapshot = db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId)
                .collection("episodes").get().await()
            for (doc in epSnapshot.documents) {
                doc.reference.delete().await()
            }
            db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId).delete().await()
        } catch (_: Exception) {}
    }

    suspend fun saveOrUpdateEpisode(episode: Episode): String {
        val db = firestore ?: return episode.id.ifBlank { "local_ep_${System.currentTimeMillis()}" }
        val episodesCol = db.collection("anime").document(episode.animeId)
            .collection("seasons").document(episode.seasonId)
            .collection("episodes")

        val docRef = if (episode.id.isBlank()) episodesCol.document() else episodesCol.document(episode.id)

        val data = hashMapOf(
            "episodeNumber" to episode.episodeNumber,
            "title" to episode.title,
            "description" to episode.description,
            "thumbnailUrl" to episode.thumbnailUrl,
            "videoUrl" to episode.videoUrl,
            "alternativeVideoUrl" to episode.alternativeVideoUrl,
            "duration" to episode.duration,
            "createdAt" to if (episode.createdAt > 0) episode.createdAt else System.currentTimeMillis()
        )
        docRef.set(data).await()

        try {
            val countSnapshot = episodesCol.get().await()
            db.collection("anime").document(episode.animeId)
                .collection("seasons").document(episode.seasonId)
                .update("episodesCount", countSnapshot.size()).await()
        } catch (_: Exception) {}

        return docRef.id
    }

    suspend fun deleteEpisode(animeId: String, seasonId: String, episodeId: String) {
        val db = firestore ?: return
        try {
            db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId)
                .collection("episodes").document(episodeId).delete().await()

            val countSnapshot = db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId)
                .collection("episodes").get().await()

            db.collection("anime").document(animeId)
                .collection("seasons").document(seasonId)
                .update("episodesCount", countSnapshot.size()).await()
        } catch (_: Exception) {}
    }

    // -------------------------------------------------------------
    // Admin Dashboard Statistics
    // -------------------------------------------------------------
    suspend fun fetchAdminDashboardStats(): AdminDashboardStats {
        val db = firestore ?: return AdminDashboardStats(totalUsers = 1, totalAnime = 4, totalEpisodes = 12, totalViews = 84500)
        return try {
            val usersCount = try { db.collection("users").get().await().size().toLong() } catch (_: Exception) { 0L }
            val animeSnapshot = db.collection("anime").get().await()
            val totalAnime = animeSnapshot.size().toLong()

            var totalViews = 0L
            val animeList = mutableListOf<Anime>()

            for (doc in animeSnapshot.documents) {
                val views = doc.getLong("viewsCount") ?: 0L
                totalViews += views
                val anime = Anime(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    englishTitle = doc.getString("englishTitle") ?: "",
                    description = doc.getString("description") ?: "",
                    posterUrl = doc.getString("posterUrl") ?: "",
                    bannerUrl = doc.getString("bannerUrl") ?: "",
                    year = doc.getLong("year")?.toInt() ?: 2024,
                    status = doc.getString("status") ?: "مستمر",
                    genres = (doc.get("genres") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    viewsCount = views,
                    rating = doc.getDouble("rating") ?: 8.5,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
                animeList.add(anime)
            }

            var totalEpisodes = 0L
            for (anime in animeList.take(20)) {
                try {
                    val seasons = db.collection("anime").document(anime.id).collection("seasons").get().await()
                    for (s in seasons.documents) {
                        val eps = s.reference.collection("episodes").get().await()
                        totalEpisodes += eps.size()
                    }
                } catch (_: Exception) {}
            }

            AdminDashboardStats(
                totalUsers = if (usersCount > 0) usersCount else 1,
                totalAnime = totalAnime,
                totalEpisodes = totalEpisodes,
                totalViews = totalViews,
                recentAnime = animeList.sortedByDescending { it.createdAt }.take(5)
            )
        } catch (_: Exception) {
            AdminDashboardStats()
        }
    }
}
