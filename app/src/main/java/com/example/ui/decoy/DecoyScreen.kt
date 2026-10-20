package com.example.ui.decoy

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DecoyType
import com.example.data.VaultPreferences
import java.io.File

@Composable
fun DecoyScreen(
    prefs: VaultPreferences,
    onUnlockRequest: () -> Unit,
    onDirectUnlockSuccess: () -> Unit
) {
    when (prefs.decoyType) {
        DecoyType.CUSTOM_IMAGE -> CustomImageDecoy(
            imagePath = prefs.customImagePath,
            onSecretTrigger = onUnlockRequest
        )
        DecoyType.CALCULATOR -> CalculatorDecoy(
            prefs = prefs,
            onSecretTrigger = onUnlockRequest,
            onDirectUnlockSuccess = onDirectUnlockSuccess
        )
        DecoyType.NOTES -> NotesDecoy(
            onSecretTrigger = onUnlockRequest
        )
    }
}

/**
 * Custom Image Decoy screen.
 * Displays user's chosen custom image as a camouflage cover.
 * Anyone opening the app just sees this innocent image/gallery photo.
 */
@Composable
fun CustomImageDecoy(
    imagePath: String?,
    onSecretTrigger: () -> Unit
) {
    val context = LocalContext.current
    val customBitmap = remember(imagePath) {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D10))
    ) {
        if (customBitmap != null) {
            Image(
                bitmap = customBitmap,
                contentDescription = "Cover Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Elegant Default Cover Art (Minimalist Note & Nature Art)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2B2B36)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFFA1A1AA),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "My Gallery & Notes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFEDEDF0)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Daily checklist and saved documents.\nTap top-right corner to open vault.",
                            fontSize = 13.sp,
                            color = Color(0xFF8E8E93),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Secret Invisible Touch Zones
        // Long pressing or double tapping the top-right trigger zone opens the PIN unlock prompt
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
                .size(64.dp)
                .clip(CircleShape)
                .testTag("secret_unlock_trigger")
                .clickable {
                    onSecretTrigger()
                },
            contentAlignment = Alignment.Center
        ) {
            // Subtle faint hint dot
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f))
            )
        }

        // Discreet emergency unlock chip at bottom for ease of use
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
                .clickable { onSecretTrigger() },
            colors = CardDefaults.cardColors(containerColor = Color(0x66000000)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Tap to Unlock",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Functional Calculator Decoy screen.
 * Works as a genuine calculator. If the user enters their PIN and hits '=',
 * it secretly unlocks the Vault!
 */
@Composable
fun CalculatorDecoy(
    prefs: VaultPreferences,
    onSecretTrigger: () -> Unit,
    onDirectUnlockSuccess: () -> Unit
) {
    var display by remember { mutableStateOf("0") }
    var operand1 by remember { mutableStateOf<Double?>(null) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var clearOnNext by remember { mutableStateOf(false) }

    fun onDigit(d: String) {
        if (display == "0" || clearOnNext) {
            display = d
            clearOnNext = false
        } else {
            if (display.length < 10) display += d
        }
    }

    fun onOperator(op: String) {
        operand1 = display.toDoubleOrNull()
        pendingOp = op
        clearOnNext = true
    }

    fun onEquals() {
        // First check secret PIN!
        if (prefs.verifyPin(display)) {
            onDirectUnlockSuccess()
            return
        }

        val op1 = operand1
        val op = pendingOp
        val op2 = display.toDoubleOrNull()
        if (op1 != null && op != null && op2 != null) {
            val res = when (op) {
                "+" -> op1 + op2
                "-" -> op1 - op2
                "×" -> op1 * op2
                "÷" -> if (op2 != 0.0) op1 / op2 else 0.0
                else -> op2
            }
            display = if (res % 1.0 == 0.0) res.toLong().toString() else "%.4f".format(res)
            operand1 = null
            pendingOp = null
            clearOnNext = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111114))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // Discreet Top Bar with Calculator title and secret long-press unlock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculator",
                color = Color(0xFF8E8E93),
                fontSize = 14.sp,
                modifier = Modifier.clickable { onSecretTrigger() }
            )
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = null,
                tint = Color(0xFF555560),
                modifier = Modifier.size(18.dp)
            )
        }

        // Display
        Text(
            text = display,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
                .testTag("calculator_display")
        )

        // Keypad Grid
        val buttonMatrix = listOf(
            listOf("C", "±", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "=")
        )

        buttonMatrix.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    val isZero = label == "0"
                    val isOp = label in listOf("÷", "×", "-", "+", "=")
                    val isSpecial = label in listOf("C", "±", "%")

                    val btnColor = when {
                        isOp -> Color(0xFFFF9500)
                        isSpecial -> Color(0xFF3A3A3C)
                        else -> Color(0xFF2C2C2E)
                    }

                    Box(
                        modifier = Modifier
                            .weight(if (isZero) 2f else 1f)
                            .aspectRatio(if (isZero) 2f else 1f)
                            .clip(CircleShape)
                            .background(btnColor)
                            .clickable {
                                when (label) {
                                    "C" -> {
                                        display = "0"
                                        operand1 = null
                                        pendingOp = null
                                    }
                                    "±" -> {
                                        if (display != "0") {
                                            display = if (display.startsWith("-")) display.drop(1) else "-$display"
                                        }
                                    }
                                    "%" -> {
                                        val v = display.toDoubleOrNull() ?: 0.0
                                        display = (v / 100.0).toString()
                                    }
                                    "÷", "×", "-", "+" -> onOperator(label)
                                    "=" -> onEquals()
                                    else -> onDigit(label)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Realistic Notes Decoy screen.
 */
@Composable
fun NotesDecoy(
    onSecretTrigger: () -> Unit
) {
    var noteText by remember {
        mutableStateOf(
            "Meeting Agenda:\n" +
            "1. Budget review for Q4\n" +
            "2. Hardware procurement timeline\n" +
            "3. Client deliverables checklist\n\n" +
            "Personal reminders:\n" +
            "- Buy groceries\n" +
            "- Call dentist appointment"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141416))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onSecretTrigger() }
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color(0xFFF4B400)
                )
                Text(
                    text = "Quick Notes",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            TextButton(onClick = { onSecretTrigger() }) {
                Text("Done", color = Color(0xFFF4B400))
            }
        }

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF1C1C20),
                unfocusedContainerColor = Color(0xFF1C1C20),
                focusedBorderColor = Color(0xFF2A2A30),
                unfocusedBorderColor = Color(0xFF222228),
                focusedTextColor = Color(0xFFEDEDF0),
                unfocusedTextColor = Color(0xFFEDEDF0)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}
