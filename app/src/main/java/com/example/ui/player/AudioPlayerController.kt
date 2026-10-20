package com.example.ui.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import com.example.data.AudioRecordItem
import com.example.data.RecordingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerController(
    private val context: Context,
    private val repository: RecordingRepository
) {
    private var mediaPlayer: MediaPlayer? = null
    private var activePlaybackFile: File? = null
    private var pollJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0)
    val currentPositionMs: StateFlow<Int> = _currentPositionMs.asStateFlow()

    private val _totalDurationMs = MutableStateFlow(0)
    val totalDurationMs: StateFlow<Int> = _totalDurationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun playOrPause(item: AudioRecordItem) {
        if (_currentlyPlayingId.value == item.id) {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                    _isPlaying.value = false
                } else {
                    player.start()
                    _isPlaying.value = true
                }
            }
        } else {
            startPlayback(item)
        }
    }

    private fun startPlayback(item: AudioRecordItem) {
        stopPlayback()

        scope.launch(Dispatchers.IO) {
            val decrypted = repository.decryptForPlayback(item) ?: return@launch
            activePlaybackFile = decrypted

            launch(Dispatchers.Main) {
                try {
                    val player = MediaPlayer().apply {
                        setDataSource(decrypted.absolutePath)
                        prepare()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            playbackParams = PlaybackParams().apply { speed = _playbackSpeed.value }
                        }
                        start()
                        setOnCompletionListener {
                            _isPlaying.value = false
                            _currentPositionMs.value = 0
                        }
                    }
                    mediaPlayer = player
                    _currentlyPlayingId.value = item.id
                    _isPlaying.value = true
                    _totalDurationMs.value = player.duration

                    startProgressPolling()
                } catch (e: Exception) {
                    e.printStackTrace()
                    stopPlayback()
                }
            }
        }
    }

    fun seekTo(positionMs: Int) {
        mediaPlayer?.seekTo(positionMs)
        _currentPositionMs.value = positionMs
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply { this.speed = speed }
            } catch (_: Exception) {}
        }
    }

    private fun startProgressPolling() {
        pollJob?.cancel()
        pollJob = scope.launch {
            while (isActive && mediaPlayer != null) {
                mediaPlayer?.let { player ->
                    try {
                        if (player.isPlaying) {
                            _currentPositionMs.value = player.currentPosition
                        }
                    } catch (_: Exception) {}
                }
                delay(200)
            }
        }
    }

    fun stopPlayback() {
        pollJob?.cancel()
        try {
            mediaPlayer?.apply {
                if (isPlaying) stop()
                release()
            }
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
        _currentlyPlayingId.value = null
        _currentPositionMs.value = 0
        _totalDurationMs.value = 0

        // Clean up temporary decrypted audio file
        activePlaybackFile?.delete()
        activePlaybackFile = null
    }

    fun release() {
        stopPlayback()
    }
}
