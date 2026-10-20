package com.example.ui.settings

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Visibility
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecoyType
import com.example.data.DisguiseAlias
import com.example.data.NotificationCamouflage
import com.example.data.VaultPreferences
import com.example.security.DisguiseManager
import com.example.service.VolumeKeyAccessibilityService
import com.example.ui.theme.CrimsonRed
import com.example.ui.theme.DarkCrimson
import com.example.ui.theme.StealthBlack
import com.example.ui.theme.StealthCardBorder
import com.example.ui.theme.StealthDarkSurface
import com.example.ui.theme.StealthGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import java.io.File
import java.io.FileOutputStream

@Composable
fun SettingsScreen(
    prefs: VaultPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showPinChangeDialog by remember { mutableStateOf(false) }
    var selectedAlias by remember { mutableStateOf(prefs.disguiseAlias) }
    var selectedDecoy by remember { mutableStateOf(prefs.decoyType) }
    var selectedNotif by remember { mutableStateOf(prefs.notificationCamouflage) }
    var hapticEnabled by remember { mutableStateOf(prefs.isHapticEnabled) }
    var highQuality by remember { mutableStateOf(prefs.isHighQualityAudio) }
    var doublePressWindow by remember { mutableFloatStateOf(prefs.doublePressWindowMs.toFloat()) }
    var isAccessibilityEnabled by remember { mutableStateOf(VolumeKeyAccessibilityService.isEnabled(context)) }

    // Photo picker for custom decoy cover image
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val input = context.contentResolver.openInputStream(it)
                val targetFile = File(context.filesDir, "custom_cover.jpg")
                input?.use { inStream ->
                    FileOutputStream(targetFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
                prefs.customImagePath = targetFile.absolutePath
                selectedDecoy = DecoyType.CUSTOM_IMAGE
                prefs.decoyType = DecoyType.CUSTOM_IMAGE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(StealthBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E1B1D))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "সিকিউরিটি ও ক্যামোফ্লেজ সেটিংস",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
                Text(
                    text = "আইকন, ডিকয় ইমেজ ও ভলিউম শর্টকাট",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Accessibility Service & Volume Key Shortcuts
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ভলিউম কী সিক্রেট ট্রিগার",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "ফোন লক অবস্থায় Vol-Up ২বার চাপলে রেকর্ড চালু, Vol-Down ২বার চাপলে অফ",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Status Badge
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isAccessibilityEnabled) Color(0xFF122818) else Color(0xFF281416))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAccessibilityEnabled) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (isAccessibilityEnabled) StealthGreen else CrimsonRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isAccessibilityEnabled) "লক স্ক্রিন ডিটেক্টর একটিভ" else "এক্সেসিবিলিটি পারমিশন প্রয়োজন",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAccessibilityEnabled) StealthGreen else CrimsonRed
                                )
                            }

                            if (!isAccessibilityEnabled) {
                                Button(
                                    onClick = {
                                        VolumeKeyAccessibilityService.openSettings(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text("অন করুন", fontSize = 11.sp, color = TextWhite)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Double press speed slider
                        Text(
                            text = "ডাবল-প্রেস টাইমিং উইন্ডো: ${doublePressWindow.toInt()} ms",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Slider(
                            value = doublePressWindow,
                            onValueChange = {
                                doublePressWindow = it
                                prefs.doublePressWindowMs = it.toLong()
                            },
                            valueRange = 400f..1200f,
                            colors = SliderDefaults.colors(
                                thumbColor = CrimsonRed,
                                activeTrackColor = CrimsonRed,
                                inactiveTrackColor = Color(0xFF2C2224)
                            )
                        )
                    }
                }
            }

            // 2. Custom App Icon & Name Disguise
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "অ্যাপ আইকন ও নাম ক্যামোফ্লেজ",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "যাতে বাইরে থেকে কেউ বুঝতে না পারে এটি অডিও রেকর্ডার",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val disguises = listOf(
                            Triple(DisguiseAlias.DEFAULT, "Stealth Voice", "ভল্ট আইকন"),
                            Triple(DisguiseAlias.CALCULATOR, "Calculator", "ক্যালকুলেটর"),
                            Triple(DisguiseAlias.NOTES, "Notes", "নোট প্যাড"),
                            Triple(DisguiseAlias.CLOCK, "Clock", "ঘড়ি / টাইমার")
                        )

                        disguises.forEach { (alias, name, subtitle) ->
                            val isSelected = selectedAlias == alias
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkCrimson else Color(0xFF19171A))
                                    .border(1.dp, if (isSelected) CrimsonRed else Color(0xFF262022), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedAlias = alias
                                        val ok = DisguiseManager.applyDisguise(context, alias)
                                        if (ok) {
                                            android.widget.Toast.makeText(
                                                context,
                                                "অ্যাপ আইকন ও নাম '$name'-এ পরিবর্তন হয়েছে!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                    Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
                                }
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Decoy Screen & Custom Image Cover
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "হোম স্ক্রিন ডিকয় (কভার ইমেজ)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "অ্যাপে ঢুকলে নির্দোষ ছবি বা ক্যালকুলেটর দেখাবে",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Image Picker Button
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26181B)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonRed),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = CrimsonRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (prefs.customImagePath != null) "কাস্টম কভার ইমেজ পরিবর্তন করুন" else "গ্যালারি থেকে কভার ইমেজ সিলেক্ট করুন",
                                color = TextWhite,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Decoy Type Selection
                        val decoys = listOf(
                            Pair(DecoyType.CUSTOM_IMAGE, "কাস্টম ইমেজ / গ্যালারি ফটো"),
                            Pair(DecoyType.CALCULATOR, "বাস্তবসম্মত ক্যালকুলেটর"),
                            Pair(DecoyType.NOTES, "সহজ নোটপ্যাড")
                        )

                        decoys.forEach { (type, label) ->
                            val isSel = selectedDecoy == type
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) DarkCrimson else Color(0xFF19171A))
                                    .border(1.dp, if (isSel) CrimsonRed else Color(0xFF262022), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedDecoy = type
                                        prefs.decoyType = type
                                    }
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = label, fontSize = 13.sp, color = TextWhite)
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Notification Camouflage (Zero Mic Icon)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "নোটিফিকেশন ছদ্মবেশ (মাইক আইকন মুক্ত)",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextWhite
                                )
                                Text(
                                    text = "স্ট্যাটাস বারে কোনো মাইক আইকন থাকবে না",
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val notifOptions = listOf(
                            Triple(NotificationCamouflage.BATTERY, "Battery Saver Active", "ব্যাটারি আইকন (প্রস্তাবিত)"),
                            Triple(NotificationCamouflage.SYSTEM, "System Health Service", "সিস্টেম শিল্ড আইকন"),
                            Triple(NotificationCamouflage.AUDIO, "Sound Enhancer", "সাউন্ড ওয়েভ আইকন"),
                            Triple(NotificationCamouflage.MINIMAL, "Background Service", "ক্ষুদ্র ডট আইকন")
                        )

                        notifOptions.forEach { (option, title, desc) ->
                            val isSel = selectedNotif == option
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) DarkCrimson else Color(0xFF19171A))
                                    .border(1.dp, if (isSel) CrimsonRed else Color(0xFF262022), RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedNotif = option
                                        prefs.notificationCamouflage = option
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextWhite)
                                    Text(text = desc, fontSize = 11.sp, color = TextMuted)
                                }
                                if (isSel) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = CrimsonRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Battery, Audio & Vibration
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "ব্যাটারি ও পারফরম্যান্স অপ্টিমাইজেশন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Battery saving audio mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "কম ব্যাটারি খরচ মোড (64kbps AAC)",
                                    fontSize = 13.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = if (!highQuality) "সক্রিয়: ন্যূনতম ব্যাটারি ব্যবহার" else "উচ্চ কোয়ালিটি: 128kbps AAC",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = !highQuality,
                                onCheckedChange = {
                                    highQuality = !it
                                    prefs.isHighQualityAudio = !it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = CrimsonRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Haptic feedback
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "পকেট ভাইব্রেশন ফিডব্যাক",
                                    fontSize = 13.sp,
                                    color = TextWhite
                                )
                                Text(
                                    text = "রেকর্ড শুরু হলে ১ বার, শেষ হলে ২ বার ভাইব্রেট",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Switch(
                                checked = hapticEnabled,
                                onCheckedChange = {
                                    hapticEnabled = it
                                    prefs.isHapticEnabled = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = TextWhite,
                                    checkedTrackColor = CrimsonRed
                                )
                            )
                        }
                    }
                }
            }

            // 6. Security PIN
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StealthDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StealthCardBorder),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPinChangeDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkCrimson),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = CrimsonRed,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "ভল্ট মাস্টার পাসওয়ার্ড পরিবর্তন",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                                Text(
                                    text = "বর্তমান ডিফল্ট: 1234",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Text(text = "পরিবর্তন", fontSize = 12.sp, color = CrimsonRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showPinChangeDialog) {
        ChangePinDialog(
            prefs = prefs,
            onDismiss = { showPinChangeDialog = false }
        )
    }
}

@Composable
fun ChangePinDialog(
    prefs: VaultPreferences,
    onDismiss: () -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var successMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = StealthDarkSurface,
        title = {
            Text("মাস্টার PIN পরিবর্তন", color = TextWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { oldPin = it },
                    label = { Text("পুরাতন PIN", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = Color(0xFF332629)
                    )
                )

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { newPin = it },
                    label = { Text("নতুন ৪-সংখ্যার PIN", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = Color(0xFF332629)
                    )
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { confirmPin = it },
                    label = { Text("নতুন PIN নিশ্চিত করুন", color = TextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = CrimsonRed,
                        unfocusedBorderColor = Color(0xFF332629)
                    )
                )

                if (errorMsg != null) {
                    Text(errorMsg ?: "", color = CrimsonRed, fontSize = 12.sp)
                }
                if (successMsg != null) {
                    Text(successMsg ?: "", color = StealthGreen, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPin != confirmPin) {
                        errorMsg = "নতুন PIN দুটি মেলেনি!"
                    } else if (newPin.length < 4) {
                        errorMsg = "PIN কমপক্ষে ৪ সংখ্যা হতে হবে।"
                    } else {
                        val ok = prefs.updatePin(oldPin, newPin)
                        if (ok) {
                            successMsg = "সফলভাবে PIN পরিবর্তন হয়েছে!"
                            errorMsg = null
                        } else {
                            errorMsg = "পুরাতন PIN সঠিক নয়।"
                        }
                    }
                },
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
