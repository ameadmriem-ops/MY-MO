package com.example.download

import android.content.Context
import com.example.data.local.AnimeDatabase
import com.example.data.local.entity.DownloadEntity
import com.example.model.DownloadItem
import com.example.model.DownloadStatus
import com.example.model.Episode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class AnimeDownloadManager(private val context: Context) {
    private val database = AnimeDatabase.getInstance(context)
    private val downloadDao = database.downloadDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun getDownloads(): Flow<List<DownloadItem>> {
        return downloadDao.getAllDownloads().map { list ->
            list.map { entity ->
                DownloadItem(
                    episodeId = entity.episodeId,
                    animeId = entity.animeId,
                    animeTitle = entity.animeTitle,
                    episodeTitle = entity.episodeTitle,
                    episodeNumber = entity.episodeNumber,
                    localFilePath = entity.localFilePath,
                    videoUrl = entity.videoUrl,
                    progress = entity.progress,
                    status = try { DownloadStatus.valueOf(entity.status) } catch (_: Exception) { DownloadStatus.PENDING },
                    totalSizeBytes = entity.totalSizeBytes,
                    downloadedBytes = entity.downloadedBytes,
                    downloadedAt = entity.downloadedAt
                )
            }
        }
    }

    fun startDownload(animeId: String, animeTitle: String, episode: Episode) {
        scope.launch {
            val existing = downloadDao.getDownload(episode.id)
            if (existing != null && existing.status == DownloadStatus.COMPLETED.name) {
                return@launch
            }

            val downloadsDir = File(context.filesDir, "downloads")
            if (!downloadsDir.exists()) downloadsDir.mkdirs()

            val targetFile = File(downloadsDir, "${animeId}_ep_${episode.episodeNumber}.mp4")

            val initialEntity = DownloadEntity(
                episodeId = episode.id,
                animeId = animeId,
                animeTitle = animeTitle,
                episodeTitle = episode.title,
                episodeNumber = episode.episodeNumber,
                localFilePath = targetFile.absolutePath,
                videoUrl = episode.videoUrl,
                progress = 0,
                status = DownloadStatus.DOWNLOADING.name,
                totalSizeBytes = 0L,
                downloadedBytes = 0L,
                downloadedAt = System.currentTimeMillis()
            )
            downloadDao.insertOrUpdate(initialEntity)

            try {
                val url = URL(episode.videoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.connect()

                if (connection.responseCode in 200..299) {
                    val fileLength = connection.contentLength.toLong()
                    val inputStream = connection.inputStream
                    val outputStream = FileOutputStream(targetFile)

                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalDownloaded = 0L
                    var lastProgressUpdate = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalDownloaded += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastProgressUpdate > 500) { // update every 500ms
                            lastProgressUpdate = now
                            val progress = if (fileLength > 0) ((totalDownloaded * 100) / fileLength).toInt() else 50
                            downloadDao.insertOrUpdate(
                                initialEntity.copy(
                                    progress = progress,
                                    downloadedBytes = totalDownloaded,
                                    totalSizeBytes = fileLength,
                                    status = DownloadStatus.DOWNLOADING.name
                                )
                            )
                        }
                    }

                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()

                    downloadDao.insertOrUpdate(
                        initialEntity.copy(
                            progress = 100,
                            downloadedBytes = totalDownloaded,
                            totalSizeBytes = if (fileLength > 0) fileLength else totalDownloaded,
                            status = DownloadStatus.COMPLETED.name
                        )
                    )
                } else {
                    downloadDao.insertOrUpdate(
                        initialEntity.copy(status = DownloadStatus.FAILED.name)
                    )
                }
            } catch (e: Exception) {
                downloadDao.insertOrUpdate(
                    initialEntity.copy(status = DownloadStatus.FAILED.name)
                )
            }
        }
    }

    suspend fun deleteDownload(episodeId: String) = withContext(Dispatchers.IO) {
        val download = downloadDao.getDownload(episodeId)
        if (download != null) {
            try {
                val file = File(download.localFilePath)
                if (file.exists()) file.delete()
            } catch (_: Exception) {}
            downloadDao.deleteDownload(episodeId)
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: AnimeDownloadManager? = null

        fun getInstance(context: Context): AnimeDownloadManager {
            return INSTANCE ?: synchronized(this) {
                val instance = AnimeDownloadManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
