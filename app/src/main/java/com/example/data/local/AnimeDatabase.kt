package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CachedAnimeDao
import com.example.data.local.dao.DownloadDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.WatchHistoryDao
import com.example.data.local.entity.CachedAnimeEntity
import com.example.data.local.entity.DownloadEntity
import com.example.data.local.entity.FavoriteEntity
import com.example.data.local.entity.WatchHistoryEntity

@Database(
    entities = [
        WatchHistoryEntity::class,
        FavoriteEntity::class,
        DownloadEntity::class,
        CachedAnimeEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AnimeDatabase : RoomDatabase() {
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun cachedAnimeDao(): CachedAnimeDao

    companion object {
        @Volatile
        private var INSTANCE: AnimeDatabase? = null

        fun getInstance(context: Context): AnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AnimeDatabase::class.java,
                    "animestream_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
