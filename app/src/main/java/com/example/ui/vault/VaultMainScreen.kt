package com.example.ui.vault

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioRecordItem
import com.example.data.RecordingRepository
import com.example.data.VaultPreferences
import com.example.security.ExportManager
import com.example.service.StealthRecorderService
import com.example.service.VolumeKeyAccessibilityService
import com.example.ui.dialogs.ExportActionType
import com.example.ui.dialogs.PasswordProtectedExportDialog
import com.example.ui.player.AudioPlayerController
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.GlowingRed
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.StealthCardBorder
import com.example.ui.theme.StealthDarkSurface
import com.example.ui.theme.StealthGreen
import com.example.ui.theme.StealthSurfaceVariant
import com.example.ui.theme.TextDarkMuted
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VaultMainScreen(
    repository: RecordingRepository,
    playerController: AudioPlayerController,
    prefs: VaultPreferences,
    onOpenSettings: () -> Unit,
    onRelock: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val recordings by repository.recordings.collectAsState()
    val isRecording by StealthRecorderService.isRecording.collectAsState()
    val durationMs by StealthRecorderService.recordingDurationMs.collectAsState()
    val amplitude by StealthRecorderService.currentAmplitude.collectAsState()

    val currentPlayingId by playerController.currentlyPlayingId.collectAsState()
    val isPlaying by playerController.isPlaying.collectAsState()
    val currentPositionMs by playerController.currentPositionMs.collectAsState()
    val totalDurationMs by playerController.totalDurationMs.collectAsState()
    val playbackSpeed by playerController.playbackSpeed.collectAsState()

    var itemToExport by remember { mutableStateOf<AudioRecordItem?>(null) }
    var itemToRename by remember { mutableStateOf<AudioRecordItem?>(null) }
    var itemToDelete by remember { mutableStateOf<AudioRecordItem?>(null) }

    val isAccessibilityActive = remember(isRecording) {
        VolumeKeyAccessibilityService.isEnabled(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Tactical Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DarkCrimson)
                        .border(1.dp, CrimsonRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Vault Icon",
                        tint = CrimsonRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "STEALTH VAULT",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = TextWhite
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF261013))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "AES-256",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed
                            )
                        }
                    }
                    Text(
                        text = "এনক্রিপটেড অডিও • ফাইল ম্যানেজার থেকে সুরক্ষিত",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Lock Button (Immediately exits to camouflage decoy)
                IconButton(
                    onClick = {
                        playerController.stopPlayback()
                        onRelock()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StealthSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock Vault",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Settings Button
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StealthSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Tactical Recording Hub Card
            item {
                ActiveRecordingCard(
                    isRecording = isRecording,
                    durationMs = durationMs,
                    amplitude = amplitude,
                    isAccessibilityActive = isAccessibilityActive,
                    onToggleRecording = {
                        StealthRecorderService.toggleRecording(context)
                    },
                    onOpenAccessibility = {
                        VolumeKeyAccessibilityService.openSettings(context)
                    },
                    onSimulateVolUp = {
                        StealthRecorderService.startRecording(context)
                        Toast.makeText(context, "Vol-Up ডাবল ট্যাপ সিমুলেট করা হয়েছে: রেকর্ড শুরু", Toast.LENGTH_SHORT).show()
                    },
                    onSimulateVolDown = {
                        StealthRecorderService.stopRecording(context)
                        Toast.makeText(context, "Vol-Down ডাবল ট্যাপ সিমুলেট করা হয়েছে: রেকর্ড বন্ধ", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            // Vault Catalog Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "সুরক্ষিত অডিও তালিকা (${recordings.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = "পাসওয়ার্ড ছাড়া অপ্রাপ্য",
                        fontSize = 11.sp,
                        color = CrimsonRed
                    )
                }
            }

            // Empty state or Recording Cards
            if (recordings.isEmpty()) {
                item {
                    EmptyVaultState(isRecording = isRecording)
                }
            } else {
                items(recordings, key = { it.id }) { item ->
                    val isThisItemActive = currentPlayingId == item.id
                    RecordingCardItem(
                        item = item,
                        isActive = isThisItemActive,
                        isPlaying = isThisItemActive && isPlaying,
                        currentPos = if (isThisItemActive) currentPositionMs else 0,
                        totalDuration = if (isThisItemActive && totalDurationMs > 0) totalDurationMs else item.durationMs.toInt(),
                        speed = playbackSpeed,
                        onPlayPause = { playerController.playOrPause(item) },
                        onSeek = { playerController.seekTo(it) },
                        onCycleSpeed = {
                            val nextSpeed = when (playbackSpeed) {
                                1.0f -> 1.5f
                                1.5f -> 2.0f
                                else -> 1.0f
                            }
                            playerController.setSpeed(nextSpeed)
                        },
                        onExportRequest = { itemToExport = item },
                        onRenameRequest = { itemToRename = item },
                        onDeleteRequest = { itemToDelete = item }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // Password Protected Export Dialog
    itemToExport?.let { item ->
        PasswordProtectedExportDialog(
            item = item,
            prefs = prefs,
            onDismiss = { itemToExport = null },
            onConfirmExport = { pin, actionType ->
                itemToExport = null
                when (actionType) {
                    ExportActionType.SAVE_DOWNLOADS -> {
                        val result = ExportManager.saveToDownloadsWithPassword(
                            context = context,
                            item = item,
                            enteredPin = pin,
                            repository = repository,
                            prefs = prefs
                        )
                        when (result) {
                            is ExportManager.ExportResult.SavedToDownloads -> {
                                Toast.makeText(context, "সফলভাবে ডাউনলোড ফোল্ডারে সেভ হয়েছে: ${result.path}", Toast.LENGTH_LONG).show()
                            }
                            is ExportManager.ExportResult.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                    ExportActionType.SHARE -> {
                        val result = ExportManager.shareAudioWithPassword(
                            context = context,
                            item = item,
                            enteredPin = pin,
                            repository = repository,
                            prefs = prefs
                        )
                        when (result) {
                            is ExportManager.ExportResult.ShareIntent -> {
                                context.startActivity(result.intent)
                            }
                            is ExportManager.ExportResult.Error -> {
                                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
            }
        )
    }

    // Rename Dialog
    itemToRename?.let { item ->
        RenameDialog(
            currentTitle = item.title,
            onDismiss = { itemToRename = null },
            onConfirm = { newTitle ->
                coroutineScope.launch {
                    repository.renameRecording(item.id, newTitle)
                }
                itemToRename = null
            }
        )
    }

    // Delete Confirmation Dialog
    itemToDelete?.let { item ->
        DeleteConfirmDialog(
            title = item.title,
            onDismiss = { itemToDelete = null },
            onConfirm = {
                if (currentPlayingId == item.id) {
                    playerController.stopPlayback()
                }
                coroutineScope.launch {
                    repository.deleteRecording(item.id)
                }
                itemToDelete = null
            }
        )
    }
}

@Composable
fun ActiveRecordingCard(
    isRecording: Boolean,
    durationMs: Long,
    amplitude: Int,
    isAccessibilityActive: Boolean,
    onToggleRecording: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onSimulateVolUp: () -> Unit,
    onSimulateVolDown: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isRecording) Color(0xFF220A0E) else StealthDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.2.dp,
            if (isRecording) CrimsonRed else StealthCardBorder
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isRecording) CrimsonRed else TextDarkMuted)
                    )
                    Text(
                        text = if (isRecording) "সিক্রেট ব্যাকগ্রাউন্ড রেকর্ডিং চলছে" else "সিক্রেট রেকর্ডার প্রস্তুত",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isRecording) CrimsonRed else TextWhite
                    )
                }

                if (isRecording) {
                    val sec = (durationMs / 1000) % 60
                    val min = (durationMs / 1000) / 60
                    val hr = min / 60
                    val timeStr = if (hr > 0) "%02d:%02d:%02d".format(hr, min % 60, sec) else "%02d:%02d".format(min, sec)
                    Text(
                        text = timeStr,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isRecording) "মাইক্রোফোন সক্রিয় • অডিও সংরক্ষিত হচ্ছে" else "ফোন লক থাকলেও বাটন চেপে চালু করুন",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vol-Up ২ বার = চালু | Vol-Down ২ বার = বন্ধ",
                        fontSize = 11.sp,
                        color = CrimsonRed,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Big Record / Stop Button
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .scale(if (isRecording) pulseScale else 1f)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = if (isRecording) listOf(GlowingRed, CrimsonRed) else listOf(DarkCrimson, Color(0xFF1E0A0D))
                            )
                        )
                        .border(1.5.dp, if (isRecording) GlowingRed else CrimsonRed, CircleShape)
                        .testTag("record_toggle_button")
                        .clickable { onToggleRecording() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "Stop" else "Start",
                        tint = TextWhite,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Volume Key Quick-Test & Accessibility Assistant Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF141215))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "টেস্ট সিমুলেটর:",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    // Vol Up Simulator
                    Button(
                        onClick = onSimulateVolUp,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26181B)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = null, tint = CrimsonRed, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Vol+ ২x", fontSize = 10.sp, color = TextWhite)
                    }

                    // Vol Down Simulator
                    Button(
                        onClick = onSimulateVolDown,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26181B)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.VolumeDown, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Vol- ২x", fontSize = 10.sp, color = TextWhite)
                    }
                }

                if (!isAccessibilityActive) {
                    Text(
                        text = "এক্সেসিবিলিটি অফ",
                        fontSize = 10.sp,
                        color = CrimsonRed,
                        modifier = Modifier.clickable { onOpenAccessibility() }
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StealthGreen))
                        Text(text = "লক মোড একটিভ", fontSize = 10.sp, color = StealthGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun RecordingCardItem(
    item: AudioRecordItem,
    isActive: Boolean,
    isPlaying: Boolean,
    currentPos: Int,
    totalDuration: Int,
    speed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Int) -> Unit,
    onCycleSpeed: () -> Unit,
    onExportRequest: () -> Unit,
    onRenameRequest: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFF1E1316) else StealthDarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) CrimsonRed else StealthCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(if (isActive) DarkCrimson else Color(0xFF1B191C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = if (isActive) CrimsonRed else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = dateStr, fontSize = 11.sp, color = TextMuted)
                            Text(text = "•", fontSize = 11.sp, color = TextDarkMuted)
                            Text(text = item.formattedDuration, fontSize = 11.sp, color = TextWhite)
                            Text(text = "•", fontSize = 11.sp, color = TextDarkMuted)
                            Text(text = item.formattedSize, fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                // Encrypted badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF221114))
                        .border(1.dp, Color(0xFF381A1F), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "🔒 HIDDEN",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonRed
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player progress bar if active
            if (isActive) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = currentPos.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..(totalDuration.toFloat().coerceAtLeast(1f)),
                        colors = SliderDefaults.colors(
                            thumbColor = CrimsonRed,
                            activeTrackColor = CrimsonRed,
                            inactiveTrackColor = Color(0xFF2B1F22)
                        ),
                        modifier = Modifier.height(20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val curSec = currentPos / 1000
                        val totSec = totalDuration / 1000
                        Text(
                            text = "%02d:%02d".format(curSec / 60, curSec % 60),
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "%02d:%02d".format(totSec / 60, totSec % 60),
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            // Controls & Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause & Speed
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CrimsonRed)
                            .clickable { onPlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF26181B))
                                .clickable { onCycleSpeed() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonRed
                            )
                        }
                    }
                }

                // Password Export Button & Actions
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Password-Protected Transfer Button
                    Button(
                        onClick = onExportRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF261014)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp).testTag("export_btn_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            tint = CrimsonRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "পাসওয়ার্ড ট্রান্সফার",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextWhite
                        )
                    }

                    IconButton(
                        onClick = onRenameRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDeleteRequest,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFCF6679),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyVaultState(isRecording: Boolean) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1C1316)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CrimsonRed,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "কোনো অডিও রেকর্ড নেই",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "রেকর্ড শুরু করতে উপরের লাল বাটনে চাপ দিন অথবা ফোন লক থাকা অবস্থায় ভলিউম আপ বাটন ২ বার চাপুন।",
                fontSize = 12.sp,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun RenameDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf(currentTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthDarkSurface,
        title = { Text("রেকর্ডের নাম পরিবর্তন", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite,
                    focusedBorderColor = CrimsonRed,
                    unfocusedBorderColor = Color(0xFF332629)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank()) onConfirm(title.trim()) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("সংরক্ষণ", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextMuted)
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthDarkSurface,
        title = { Text("অডিও ডিলিট করবেন?", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Text("'$title' ফাইলটি ভল্ট থেকে চিরতরে মুছে ফেলা হবে।", color = TextMuted, fontSize = 13.sp)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)
            ) {
                Text("ডিলিট", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল", color = TextMuted)
            }
        }
    )
}
