package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.data.VaultPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VolumeKeyAccessibilityService : AccessibilityService() {

    companion object {
        private val _isServiceConnected = MutableStateFlow(false)
        val isServiceConnected: StateFlow<Boolean> = _isServiceConnected.asStateFlow()

        /**
         * Checks if the Accessibility Service is enabled in Android Settings.
         */
        fun isEnabled(context: Context): Boolean {
            val expectedComponentName = "${context.packageName}/${VolumeKeyAccessibilityService::class.java.canonicalName}"
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false

            val colonSplitter = TextUtils.SimpleStringSplitter(':')
            colonSplitter.setString(enabledServices)
            while (colonSplitter.hasNext()) {
                val componentName = colonSplitter.next()
                if (componentName.equals(expectedComponentName, ignoreCase = true)) {
                    return true
                }
            }
            return false
        }

        fun openSettings(context: Context) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }
    }

    private var lastUpPressTime = 0L
    private var lastDownPressTime = 0L
    private lateinit var prefs: VaultPreferences

    override fun onServiceConnected() {
        super.onServiceConnected()
        prefs = VaultPreferences(this)
        _isServiceConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No window event inspection needed for key capture
    }

    override fun onInterrupt() {
        _isServiceConnected.value = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        _isServiceConnected.value = false
        return super.onUnbind(intent)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()
            val window = if (::prefs.isInitialized) prefs.doublePressWindowMs else 750L

            when (event.keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP -> {
                    if (now - lastUpPressTime <= window) {
                        // Double-tap Volume Up detected! Secretly START recording
                        lastUpPressTime = 0L
                        StealthRecorderService.startRecording(this)
                    } else {
                        lastUpPressTime = now
                    }
                }
                KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    if (now - lastDownPressTime <= window) {
                        // Double-tap Volume Down detected! Secretly STOP recording
                        lastDownPressTime = 0L
                        StealthRecorderService.stopRecording(this)
                    } else {
                        lastDownPressTime = now
                    }
                }
            }
        }
        // Return false so normal volume key functionality is not broken
        return super.onKeyEvent(event)
    }
}
