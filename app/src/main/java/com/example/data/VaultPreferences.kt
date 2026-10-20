package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.security.EncryptionManager

enum class DecoyType {
    CUSTOM_IMAGE,
    CALCULATOR,
    NOTES
}

enum class DisguiseAlias {
    DEFAULT,
    CALCULATOR,
    NOTES,
    CLOCK
}

enum class NotificationCamouflage {
    BATTERY,
    SYSTEM,
    AUDIO,
    MINIMAL
}

class VaultPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("stealth_vault_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_DECOY_ENABLED = "decoy_enabled"
        private const val KEY_DECOY_TYPE = "decoy_type"
        private const val KEY_CUSTOM_IMAGE_PATH = "custom_image_path"
        private const val KEY_DISGUISE_ALIAS = "disguise_alias"
        private const val KEY_NOTIF_CAMOUFLAGE = "notif_camouflage"
        private const val KEY_HAPTIC_ENABLED = "haptic_enabled"
        private const val KEY_DOUBLE_PRESS_WINDOW = "double_press_window_ms"
        private const val KEY_HIGH_QUALITY = "high_quality_audio"
        private const val DEFAULT_PIN = "1234"
    }

    init {
        // Initialize default PIN hash if not set
        if (!prefs.contains(KEY_PIN_HASH)) {
            val defaultHash = EncryptionManager.hashPin(DEFAULT_PIN)
            prefs.edit().putString(KEY_PIN_HASH, defaultHash).apply()
        }
    }

    fun verifyPin(pin: String): Boolean {
        val inputHash = EncryptionManager.hashPin(pin)
        val savedHash = prefs.getString(KEY_PIN_HASH, "") ?: ""
        return inputHash == savedHash
    }

    fun updatePin(oldPin: String, newPin: String): Boolean {
        if (!verifyPin(oldPin)) return false
        val newHash = EncryptionManager.hashPin(newPin)
        prefs.edit().putString(KEY_PIN_HASH, newHash).apply()
        return true
    }

    var isDecoyEnabled: Boolean
        get() = prefs.getBoolean(KEY_DECOY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_DECOY_ENABLED, value).apply()

    var decoyType: DecoyType
        get() {
            val name = prefs.getString(KEY_DECOY_TYPE, DecoyType.CUSTOM_IMAGE.name)
            return try {
                DecoyType.valueOf(name ?: DecoyType.CUSTOM_IMAGE.name)
            } catch (e: Exception) {
                DecoyType.CUSTOM_IMAGE
            }
        }
        set(value) = prefs.edit().putString(KEY_DECOY_TYPE, value.name).apply()

    var customImagePath: String?
        get() = prefs.getString(KEY_CUSTOM_IMAGE_PATH, null)
        set(value) = prefs.edit().putString(KEY_CUSTOM_IMAGE_PATH, value).apply()

    var disguiseAlias: DisguiseAlias
        get() {
            val name = prefs.getString(KEY_DISGUISE_ALIAS, DisguiseAlias.DEFAULT.name)
            return try {
                DisguiseAlias.valueOf(name ?: DisguiseAlias.DEFAULT.name)
            } catch (e: Exception) {
                DisguiseAlias.DEFAULT
            }
        }
        set(value) = prefs.edit().putString(KEY_DISGUISE_ALIAS, value.name).apply()

    var notificationCamouflage: NotificationCamouflage
        get() {
            val name = prefs.getString(KEY_NOTIF_CAMOUFLAGE, NotificationCamouflage.BATTERY.name)
            return try {
                NotificationCamouflage.valueOf(name ?: NotificationCamouflage.BATTERY.name)
            } catch (e: Exception) {
                NotificationCamouflage.BATTERY
            }
        }
        set(value) = prefs.edit().putString(KEY_NOTIF_CAMOUFLAGE, value.name).apply()

    var isHapticEnabled: Boolean
        get() = prefs.getBoolean(KEY_HAPTIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_HAPTIC_ENABLED, value).apply()

    var doublePressWindowMs: Long
        get() = prefs.getLong(KEY_DOUBLE_PRESS_WINDOW, 750L)
        set(value) = prefs.edit().putLong(KEY_DOUBLE_PRESS_WINDOW, value).apply()

    var isHighQualityAudio: Boolean
        get() = prefs.getBoolean(KEY_HIGH_QUALITY, false)
        set(value) = prefs.edit().putBoolean(KEY_HIGH_QUALITY, value).apply()
}
