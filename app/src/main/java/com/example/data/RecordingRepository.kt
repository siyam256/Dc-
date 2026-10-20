package com.example.data

import android.content.Context
import com.example.security.EncryptionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class RecordingRepository(private val context: Context) {

    private val vaultDir: File = File(context.filesDir, "vault").apply {
        if (!exists()) mkdirs()
        // Create .nomedia so system file scanner/media player never indexes it
        val nomedia = File(this, ".nomedia")
        if (!nomedia.exists()) {
            try { nomedia.createNewFile() } catch (_: Exception) {}
        }
    }

    private val catalogFile = File(vaultDir, "catalog.json")
    private val mutex = Mutex()
    private val _recordings = MutableStateFlow<List<AudioRecordItem>>(emptyList())
    val recordings: StateFlow<List<AudioRecordItem>> = _recordings.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        scope.launch {
            loadCatalog()
        }
    }

    fun getVaultDir(): File = vaultDir

    private suspend fun loadCatalog() {
        mutex.withLock {
            if (!catalogFile.exists()) {
                _recordings.value = emptyList()
                return
            }
            try {
                val jsonString = catalogFile.readText(Charsets.UTF_8)
                val jsonArray = JSONArray(jsonString)
                val list = mutableListOf<AudioRecordItem>()
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    list.add(
                        AudioRecordItem(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            encryptedFileName = obj.getString("encryptedFileName"),
                            timestamp = obj.getLong("timestamp"),
                            durationMs = obj.getLong("durationMs"),
                            fileSizeBytes = obj.getLong("fileSizeBytes"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
                // Sort newest first
                _recordings.value = list.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                e.printStackTrace()
                _recordings.value = emptyList()
            }
        }
    }

    private suspend fun persistCatalog(list: List<AudioRecordItem>) {
        try {
            val jsonArray = JSONArray()
            list.forEach { item ->
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("encryptedFileName", item.encryptedFileName)
                    put("timestamp", item.timestamp)
                    put("durationMs", item.durationMs)
                    put("fileSizeBytes", item.fileSizeBytes)
                    put("notes", item.notes)
                }
                jsonArray.put(obj)
            }
            catalogFile.writeText(jsonArray.toString(2), Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun addRecording(item: AudioRecordItem) {
        mutex.withLock {
            val current = _recordings.value.toMutableList()
            current.add(0, item)
            _recordings.value = current
            persistCatalog(current)
        }
    }

    suspend fun deleteRecording(id: String): Boolean {
        return mutex.withLock {
            val item = _recordings.value.find { it.id == id } ?: return@withLock false
            val file = File(vaultDir, item.encryptedFileName)
            if (file.exists()) {
                file.delete()
            }
            val current = _recordings.value.filterNot { it.id == id }
            _recordings.value = current
            persistCatalog(current)
            true
        }
    }

    suspend fun renameRecording(id: String, newTitle: String): Boolean {
        return mutex.withLock {
            val index = _recordings.value.indexOfFirst { it.id == id }
            if (index == -1) return@withLock false

            val current = _recordings.value.toMutableList()
            val updated = current[index].copy(title = newTitle.trim())
            current[index] = updated
            _recordings.value = current
            persistCatalog(current)
            true
        }
    }

    fun getEncryptedFile(item: AudioRecordItem): File {
        return File(vaultDir, item.encryptedFileName)
    }

    /**
     * Decrypts an encrypted recording into a temporary cache file for in-app playback.
     */
    fun decryptForPlayback(item: AudioRecordItem): File? {
        val encFile = getEncryptedFile(item)
        if (!encFile.exists()) return null

        val cachePlayback = File(context.cacheDir, "play_${item.id}.m4a")
        val key = EncryptionManager.deriveKey("StealthVaultAudioSecureSalt2026")
        val success = EncryptionManager.decryptFile(encFile, cachePlayback, key)
        return if (success) cachePlayback else null
    }

    /**
     * Decrypts an audio recording into the export cache folder for external transfer / sharing.
     */
    fun prepareExportAudio(item: AudioRecordItem, cleanName: String): File? {
        val encFile = getEncryptedFile(item)
        if (!encFile.exists()) return null

        val exportDir = File(context.cacheDir, "exports").apply {
            if (!exists()) mkdirs()
        }
        val safeName = cleanName.replace(Regex("[^a-zA-Z0-9._-]"), "_")
        val exportFile = File(exportDir, "${safeName}.m4a")

        val key = EncryptionManager.deriveKey("StealthVaultAudioSecureSalt2026")
        val success = EncryptionManager.decryptFile(encFile, exportFile, key)
        return if (success) exportFile else null
    }
}
