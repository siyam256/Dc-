package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioRecordItem
import com.example.data.VaultPreferences
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.StealthDarkSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Tactical PIN entry dialog to unlock the vault.
 */
@Composable
fun VaultPinDialog(
    prefs: VaultPreferences,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    fun handleDigit(d: String) {
        if (enteredPin.length < 6) {
            val updated = enteredPin + d
            enteredPin = updated
            isError = false
            if (updated.length >= 4 && prefs.verifyPin(updated)) {
                onSuccess()
            } else if (updated.length == 4 && !prefs.verifyPin(updated)) {
                isError = true
            }
        }
    }

    fun handleDelete() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DarkCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "গোপন ভল্ট আনলক",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "পাসওয়ার্ড / PIN দিন (ডিফল্ট: 1234)",
                    fontSize = 12.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // PIN Dots
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    repeat(4) { idx ->
                        val filled = idx < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isError -> CrimsonRed
                                        filled -> CrimsonRed
                                        else -> Color(0xFF2C2224)
                                    }
                                )
                                .border(
                                    1.dp,
                                    if (filled) CrimsonRed else Color(0xFF44383A),
                                    CircleShape
                                )
                        )
                    }
                }

                if (isError) {
                    Text(
                        text = "ভুল PIN! পুনরায় চেষ্টা করুন।",
                        color = CrimsonRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Compact Keypad
                val keypad = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                keypad.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (key) {
                                            "C", "DEL" -> Color(0xFF1E1B1D)
                                            else -> Color(0xFF19171A)
                                        }
                                    )
                                    .border(1.dp, Color(0xFF2A2225), CircleShape)
                                    .testTag("pin_key_$key")
                                    .clickable {
                                        when (key) {
                                            "C" -> {
                                                enteredPin = ""
                                                isError = false
                                            }
                                            "DEL" -> handleDelete()
                                            else -> handleDigit(key)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (key == "DEL") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Text(
                                        text = key,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (key == "C") CrimsonRed else TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextMuted)
            }
        }
    )
}

/**
 * Dedicated Password-Protected Export / Transfer Dialog.
 * Fulfills the exact user requirement:
 * "এই অডিও ফাইল ম্যানেজার এ দেখা যাবেনা ঐ app e থাকবে প্রটেক্টেড। পাসওয়ার্ড দিয়ে ট্রান্সফার করা লাগবে।"
 */
@Composable
fun PasswordProtectedExportDialog(
    item: AudioRecordItem,
    prefs: VaultPreferences,
    onDismiss: () -> Unit,
    onConfirmExport: (pin: String, exportType: ExportActionType) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedAction by remember { mutableStateOf(ExportActionType.SAVE_DOWNLOADS) }

    fun checkAndExport() {
        if (prefs.verifyPin(pin)) {
            onConfirmExport(pin, selectedAction)
        } else {
            errorMessage = "ভুল পাসওয়ার্ড! সঠিক পাসওয়ার্ড দিন।"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthDarkSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkCrimson),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "সুরক্ষিত অডিও ট্রান্সফার",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = item.title,
                        fontSize = 12.sp,
                        color = TextMuted,
                        maxLines = 1
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "ফাইলটি এনক্রিপ্ট করা আছে। ফাইল ম্যানেজার বা অন্য কোথাও ট্রান্সফার করতে আপনার পাসওয়ার্ড দিন:",
                    fontSize = 13.sp,
                    color = Color(0xFFC8BFC0),
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Action selector: Save to Downloads (for File Manager) or Direct Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedAction = ExportActionType.SAVE_DOWNLOADS },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedAction == ExportActionType.SAVE_DOWNLOADS)
                                DarkCrimson else Color(0xFF1B1A1E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedAction == ExportActionType.SAVE_DOWNLOADS) CrimsonRed else Color(0xFF2C2224)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = if (selectedAction == ExportActionType.SAVE_DOWNLOADS) CrimsonRed else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "ফাইল ম্যানেজার\n(Downloads)",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = TextWhite
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedAction = ExportActionType.SHARE },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedAction == ExportActionType.SHARE)
                                DarkCrimson else Color(0xFF1B1A1E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedAction == ExportActionType.SHARE) CrimsonRed else Color(0xFF2C2224)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = if (selectedAction == ExportActionType.SHARE) CrimsonRed else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "অন্য অ্যাপে শেয়ার\n(WhatsApp/Drive)",
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = TextWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PIN Input Dots & Mini Keypad
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(4) { idx ->
                        val filled = idx < pin.length
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (filled) CrimsonRed else Color(0xFF2C2224))
                                .border(1.dp, if (filled) CrimsonRed else Color(0xFF44383A), CircleShape)
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = CrimsonRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Digits
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )
                keys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { k ->
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF19171A))
                                    .border(1.dp, Color(0xFF2A2225), CircleShape)
                                    .clickable {
                                        when (k) {
                                            "C" -> {
                                                pin = ""
                                                errorMessage = null
                                            }
                                            "DEL" -> {
                                                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                errorMessage = null
                                            }
                                            else -> {
                                                if (pin.length < 6) {
                                                    pin += k
                                                    errorMessage = null
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (k == "DEL") {
                                    Icon(
                                        imageVector = Icons.Default.Backspace,
                                        contentDescription = "Delete",
                                        tint = TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = k,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (k == "C") CrimsonRed else TextWhite
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { checkAndExport() },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                enabled = pin.isNotEmpty(),
                modifier = Modifier.testTag("confirm_export_btn")
            ) {
                Text("ট্রান্সফার করুন", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextMuted)
            }
        }
    )
}

enum class ExportActionType {
    SAVE_DOWNLOADS,
    SHARE
}
