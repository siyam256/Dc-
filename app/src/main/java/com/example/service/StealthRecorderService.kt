package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.StealthApplication
import com.example.data.AudioRecordItem
import com.example.data.NotificationCamouflage
import com.example.data.RecordingRepository
import com.example.data.VaultPreferences
import com.example.security.EncryptionManager
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class StealthRecorderService : Service() {

    companion object {
        const val ACTION_START = "com.example.stealth.ACTION_START"
        const val ACTION_STOP = "com.example.stealth.ACTION_STOP"
        const val ACTION_TOGGLE = "com.example.stealth.ACTION_TOGGLE"

        private const val NOTIFICATION_ID = 9012

        private val _isRecording = MutableStateFlow(false)
        val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

        private val _recordingDurationMs = MutableStateFlow(0L)
        val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

        private val _currentAmplitude = MutableStateFlow(0)
        val currentAmplitude: StateFlow<Int> = _currentAmplitude.asStateFlow()

        fun startRecording(context: Context) {
            val intent = Intent(context, StealthRecorderService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, StealthRecorderService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun toggleRecording(context: Context) {
            val intent = Intent(context, StealthRecorderService::class.java).apply {
                action = ACTION_TOGGLE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    private var mediaRecorder: MediaRecorder? = null
    private var tempRawFile: File? = null
    private var recordingStartTime = 0L
    private var timerJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private lateinit var prefs: VaultPreferences
    private lateinit var repository: RecordingRepository

    override fun onCreate() {
        super.onCreate()
        prefs = VaultPreferences(this)
        repository = RecordingRepository(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                if (!_isRecording.value) {
                    startRecordingInternal()
                }
            }
            ACTION_STOP -> {
                if (_isRecording.value) {
                    stopRecordingInternal()
                }
            }
            ACTION_TOGGLE -> {
                if (_isRecording.value) {
                    stopRecordingInternal()
                } else {
                    startRecordingInternal()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startRecordingInternal() {
        try {
            val notification = buildCamouflageNotification()
            startForeground(NOTIFICATION_ID, notification)

            tempRawFile = File(cacheDir, "active_audio_${System.currentTimeMillis()}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(if (prefs.isHighQualityAudio) 128000 else 64000)
                setOutputFile(tempRawFile?.absolutePath)
                prepare()
                start()
            }

            recordingStartTime = System.currentTimeMillis()
            _isRecording.value = true
            _recordingDurationMs.value = 0L

            // Haptic feedback: 1 short pulse on start
            triggerVibration(longArrayOf(0, 80))

            // Start timer & amplitude poll
            timerJob?.cancel()
            timerJob = serviceScope.launch {
                while (isActive && _isRecording.value) {
                    _recordingDurationMs.value = System.currentTimeMillis() - recordingStartTime
                    try {
                        val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                        _currentAmplitude.value = maxAmp
                    } catch (_: Exception) {}
                    delay(100)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
            stopSelf()
        }
    }

    private fun stopRecordingInternal() {
        timerJob?.cancel()
        val duration = System.currentTimeMillis() - recordingStartTime

        try {
            mediaRecorder?.apply {
                try {
                    stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                release()
            }
            mediaRecorder = null
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _isRecording.value = false
        _recordingDurationMs.value = 0L
        _currentAmplitude.value = 0

        // Haptic feedback: 2 short pulses on stop
        triggerVibration(longArrayOf(0, 70, 90, 70))

        // Encrypt audio file and store into protected vault
        serviceScope.launch {
            val rawFile = tempRawFile
            if (rawFile != null && rawFile.exists() && rawFile.length() > 0) {
                val timestamp = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                val dateTitleFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
                val dateStr = dateFormat.format(Date(timestamp))
                val title = "Rec $dateStr"

                val encFileName = "SEC_${dateStr}_${UUID.randomUUID().toString().take(6)}.enc"
                val encFile = File(repository.getVaultDir(), encFileName)

                val key = EncryptionManager.deriveKey("StealthVaultAudioSecureSalt2026")
                val encrypted = EncryptionManager.encryptFile(rawFile, encFile, key)

                if (encrypted) {
                    val item = AudioRecordItem(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        encryptedFileName = encFileName,
                        timestamp = timestamp,
                        durationMs = if (duration > 500) duration else 1000L,
                        fileSizeBytes = encFile.length()
                    )
                    repository.addRecording(item)
                }
                rawFile.delete()
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun triggerVibration(pattern: LongArray) {
        if (!prefs.isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(pattern, -1)
                }
            }
        } catch (_: Exception) {}
    }

    private fun buildCamouflageNotification(): Notification {
        val (titleRes, textRes) = when (prefs.notificationCamouflage) {
            NotificationCamouflage.BATTERY -> Pair(R.string.notif_disguise_battery_title, R.string.notif_disguise_battery_text)
            NotificationCamouflage.SYSTEM -> Pair(R.string.notif_disguise_system_title, R.string.notif_disguise_system_text)
            NotificationCamouflage.AUDIO -> Pair(R.string.notif_disguise_audio_title, R.string.notif_disguise_audio_text)
            NotificationCamouflage.MINIMAL -> Pair(R.string.notif_disguise_battery_title, R.string.notif_disguise_battery_text)
        }

        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, StealthApplication.CHANNEL_ID)
            .setContentTitle(getString(titleRes))
            .setContentText(getString(textRes))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (_isRecording.value) {
            stopRecordingInternal()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
