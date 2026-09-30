package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY updatedAt DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestForAnime(animeId: String): WatchHistoryEntity?

    @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId")
    suspend fun getHistoryForEpisode(episodeId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE episodeId = :episodeId")
    suspend fun delete(episodeId: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAll()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE animeId = :animeId)")
    fun isFavorite(animeId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE animeId = :animeId")
    suspend fun removeFavorite(animeId: String)

    @Query("DELETE FROM favorites")
    suspend fun clearAll()
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE episodeId = :episodeId")
    suspend fun getDownload(episodeId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE episodeId = :episodeId")
    suspend fun deleteDownload(episodeId: String)

    @Query("DELETE FROM downloads")
    suspend fun clearAll()
}

@Dao
interface CachedAnimeDao {
    @Query("SELECT * FROM cached_anime ORDER BY viewsCount DESC")
    fun getCachedAnimes(): Flow<List<CachedAnimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(animes: List<CachedAnimeEntity>)

    @Query("DELETE FROM cached_anime")
    suspend fun clearCache()
}
