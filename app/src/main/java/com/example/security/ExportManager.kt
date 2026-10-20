package com.example.security

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.AudioRecordItem
import com.example.data.RecordingRepository
import com.example.data.VaultPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object ExportManager {

    sealed class ExportResult {
        data class ShareIntent(val intent: Intent) : ExportResult()
        data class SavedToDownloads(val path: String) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }

    /**
     * Verifies password first. Then creates decrypted copy and returns Intent to share securely.
     */
    fun shareAudioWithPassword(
        context: Context,
        item: AudioRecordItem,
        enteredPin: String,
        repository: RecordingRepository,
        prefs: VaultPreferences
    ): ExportResult {
        if (!prefs.verifyPin(enteredPin)) {
            return ExportResult.Error("ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।")
        }

        val decryptedFile = repository.prepareExportAudio(item, item.title)
            ?: return ExportResult.Error("ফাইল ডিক্রিপ্ট করতে সমস্যা হয়েছে।")

        return try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, decryptedFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(shareIntent, "অডিও শেয়ার করুন (ট্রান্সফার)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ExportResult.ShareIntent(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("শেয়ারিং তৈরি ব্যর্থ: ${e.localizedMessage}")
        }
    }

    /**
     * Verifies password first. Then decrypts and writes to device Downloads folder so user can access it in File Manager.
     */
    fun saveToDownloadsWithPassword(
        context: Context,
        item: AudioRecordItem,
        enteredPin: String,
        repository: RecordingRepository,
        prefs: VaultPreferences
    ): ExportResult {
        if (!prefs.verifyPin(enteredPin)) {
            return ExportResult.Error("ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।")
        }

        val decryptedFile = repository.prepareExportAudio(item, item.title)
            ?: return ExportResult.Error("ফাইল ডিক্রিপ্ট করতে সমস্যা হয়েছে।")

        val fileName = "${item.title.replace(Regex("[^a-zA-Z0-9._-]"), "_")}.m4a"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/StealthVault")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: return ExportResult.Error("ডাউনলোড ফোল্ডার তৈরি ব্যর্থ")

                resolver.openOutputStream(uri)?.use { outStream ->
                    FileInputStream(decryptedFile).use { inStream ->
                        inStream.copyTo(outStream)
                    }
                }
                ExportResult.SavedToDownloads("Downloads/StealthVault/$fileName")
            } else {
                @Suppress("DEPRECATION")
                val targetDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "StealthVault").apply {
                    if (!exists()) mkdirs()
                }
                val targetFile = File(targetDir, fileName)
                FileInputStream(decryptedFile).use { inStream ->
                    FileOutputStream(targetFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                ExportResult.SavedToDownloads(targetFile.absolutePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult.Error("সংরক্ষণ ব্যর্থ হয়েছে: ${e.localizedMessage}")
        }
    }
}
