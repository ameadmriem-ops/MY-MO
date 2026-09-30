package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.AnimeApp
import com.example.data.repository.AnimeRepository
import com.example.model.Episode
import com.example.model.WatchHistory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentEpisode: Episode? = null,
    val animeTitle: String = "",
    val posterUrl: String = "",
    val allEpisodes: List<Episode> = emptyList(),
    val currentStreamUrl: String = "",
    val isUsingAlternativeStream: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val errorMessage: String? = null,
    val isControlsVisible: Boolean = true,
    val isFullscreen: Boolean = false
)

class PlayerViewModel(
    private val repository: AnimeRepository = AnimeApp.instance.animeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private var exoPlayer: ExoPlayer? = null
    private var progressTrackerJob: Job? = null

    fun getOrCreatePlayer(context: Context): ExoPlayer {
        return exoPlayer ?: ExoPlayer.Builder(context.applicationContext).build().also { player ->
            exoPlayer = player
            player.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _uiState.value = _uiState.value.copy(isPlaying = isPlaying)
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            _uiState.value = _uiState.value.copy(isBuffering = true)
                        }
                        Player.STATE_READY -> {
                            _uiState.value = _uiState.value.copy(
                                isBuffering = false,
                                durationMs = player.duration.coerceAtLeast(0L),
                                errorMessage = null
                            )
                        }
                        Player.STATE_ENDED -> {
                            _uiState.value = _uiState.value.copy(isPlaying = false)
                            playNextEpisode()
                        }
                        Player.STATE_IDLE -> {
                            _uiState.value = _uiState.value.copy(isBuffering = false)
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    _uiState.value = _uiState.value.copy(
                        isBuffering = false,
                        isPlaying = false,
                        errorMessage = "تعذر تشغيل الفيديو: ${error.localizedMessage ?: "خطأ في الرابط"}"
                    )
                }
            })
            startProgressTracker()
        }
    }

    fun initEpisode(
        animeId: String,
        seasonId: String,
        episodeId: String,
        animeTitle: String,
        posterUrl: String
    ) {
        viewModelScope.launch {
            val episode = repository.getEpisodeById(animeId, seasonId, episodeId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                currentEpisode = episode,
                animeTitle = animeTitle,
                posterUrl = posterUrl
            )

            // Load all episodes for the season for next/prev navigation
            repository.getEpisodesForSeason(animeId, seasonId).collect { epList ->
                _uiState.value = _uiState.value.copy(allEpisodes = epList)
            }
        }
    }

    fun playEpisode(episode: Episode, startPositionMs: Long = 0L) {
        _uiState.value = _uiState.value.copy(
            currentEpisode = episode,
            currentStreamUrl = episode.videoUrl,
            isUsingAlternativeStream = false,
            errorMessage = null,
            isBuffering = true
        )

        val player = exoPlayer ?: return
        val streamUrl = episode.videoUrl.ifBlank { episode.alternativeVideoUrl }

        if (streamUrl.isNotBlank()) {
            val mediaItem = MediaItem.fromUri(streamUrl)
            player.setMediaItem(mediaItem)
            player.prepare()
            if (startPositionMs > 0) {
                player.seekTo(startPositionMs)
            }
            player.play()
        } else {
            _uiState.value = _uiState.value.copy(
                isBuffering = false,
                errorMessage = "رابط الفيديو غير متوفر لهذه الحلقة"
            )
        }
    }

    fun switchStreamSource() {
        val episode = _uiState.value.currentEpisode ?: return
        val player = exoPlayer ?: return
        val currentPosition = player.currentPosition

        val nextIsAlternative = !_uiState.value.isUsingAlternativeStream
        val newUrl = if (nextIsAlternative) {
            episode.alternativeVideoUrl.ifBlank { episode.videoUrl }
        } else {
            episode.videoUrl.ifBlank { episode.alternativeVideoUrl }
        }

        _uiState.value = _uiState.value.copy(
            isUsingAlternativeStream = nextIsAlternative,
            currentStreamUrl = newUrl,
            isBuffering = true,
            errorMessage = null
        )

        val mediaItem = MediaItem.fromUri(newUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.seekTo(currentPosition)
        player.play()
    }

    fun retryPlayback() {
        val currentUrl = _uiState.value.currentStreamUrl
        val player = exoPlayer ?: return
        if (currentUrl.isNotBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = null, isBuffering = true)
            player.setMediaItem(MediaItem.fromUri(currentUrl))
            player.prepare()
            player.play()
        }
    }

    fun togglePlayPause() {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekBy(offsetMs: Long) {
        val player = exoPlayer ?: return
        val target = (player.currentPosition + offsetMs).coerceIn(0L, player.duration.coerceAtLeast(0L))
        player.seekTo(target)
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs)
    }

    fun setPlaybackSpeed(speed: Float) {
        exoPlayer?.setPlaybackSpeed(speed)
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    fun toggleControlsVisibility() {
        _uiState.value = _uiState.value.copy(isControlsVisible = !_uiState.value.isControlsVisible)
    }

    fun toggleFullscreen() {
        _uiState.value = _uiState.value.copy(isFullscreen = !_uiState.value.isFullscreen)
    }

    fun playNextEpisode() {
        val current = _uiState.value.currentEpisode ?: return
        val all = _uiState.value.allEpisodes
        val next = all.find { it.episodeNumber == current.episodeNumber + 1 } ?: return
        playEpisode(next, 0L)
    }

    fun playPreviousEpisode() {
        val current = _uiState.value.currentEpisode ?: return
        val all = _uiState.value.allEpisodes
        val prev = all.find { it.episodeNumber == current.episodeNumber - 1 } ?: return
        playEpisode(prev, 0L)
    }

    private fun startProgressTracker() {
        progressTrackerJob?.cancel()
        progressTrackerJob = viewModelScope.launch {
            while (isActive) {
                exoPlayer?.let { player ->
                    if (player.isPlaying) {
                        val currentPos = player.currentPosition
                        val duration = player.duration.coerceAtLeast(0L)
                        _uiState.value = _uiState.value.copy(
                            currentPositionMs = currentPos,
                            durationMs = duration
                        )

                        // Save progress to Room watch history every 5 seconds
                        val episode = _uiState.value.currentEpisode
                        if (episode != null && duration > 0) {
                            repository.saveWatchProgress(
                                WatchHistory(
                                    animeId = episode.animeId,
                                    animeTitle = _uiState.value.animeTitle,
                                    posterUrl = _uiState.value.posterUrl,
                                    episodeId = episode.id,
                                    episodeNumber = episode.episodeNumber,
                                    episodeTitle = episode.title,
                                    positionMs = currentPos,
                                    durationMs = duration
                                )
                            )
                        }
                    }
                }
                delay(1000)
            }
        }
    }

    fun releasePlayer() {
        progressTrackerJob?.cancel()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}
