package com.example.data

data class AudioRecordItem(
    val id: String,
    val title: String,
    val encryptedFileName: String,
    val timestamp: Long,
    val durationMs: Long,
    val fileSizeBytes: Long,
    val notes: String = ""
) {
    val formattedDuration: String
        get() {
            val totalSeconds = durationMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%02d:%02d".format(minutes, seconds)
        }

    val formattedSize: String
        get() {
            return when {
                fileSizeBytes >= 1024 * 1024 -> "%.1f MB".format(fileSizeBytes / (1024.0 * 1024.0))
                fileSizeBytes >= 1024 -> "%d KB".format(fileSizeBytes / 1024)
                else -> "%d B".format(fileSizeBytes)
            }
        }
}
