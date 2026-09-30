package com.example

import android.app.Application
import android.util.Log
import com.example.ads.AdManager
import com.example.data.repository.AnimeRepository
import com.example.download.AnimeDownloadManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class AnimeApp : Application() {

    lateinit var animeRepository: AnimeRepository
        private set

    lateinit var downloadManager: AnimeDownloadManager
        private set

    lateinit var adManager: AdManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Safe Firebase initialization
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:104890768616:android:animestream")
                    .setProjectId("animestream-app")
                    .setApiKey("AIzaSyDummyKeyForSafeInitialization987")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            Log.w("AnimeApp", "Firebase safe init fallback: ${e.message}")
        }

        animeRepository = AnimeRepository(this)
        downloadManager = AnimeDownloadManager.getInstance(this)
        adManager = AdManager.getInstance(this)
    }

    companion object {
        lateinit var instance: AnimeApp
            private set
    }
}
