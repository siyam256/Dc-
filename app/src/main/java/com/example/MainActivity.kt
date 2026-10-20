package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.data.RecordingRepository
import com.example.data.VaultPreferences
import com.example.service.StealthRecorderService
import com.example.ui.decoy.DecoyScreen
import com.example.ui.dialogs.VaultPinDialog
import com.example.ui.player.AudioPlayerController
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StealthBlack
import com.example.ui.vault.VaultMainScreen

enum class AppScreen {
    VAULT,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    private lateinit var prefs: VaultPreferences
    private lateinit var repository: RecordingRepository
    private lateinit var playerController: AudioPlayerController

    private var lastVolUpPress = 0L
    private var lastVolDownPress = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        prefs = VaultPreferences(this)
        repository = RecordingRepository(this)
        playerController = AudioPlayerController(this, repository)

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StealthBlack
                ) {
                    MainAppContent(
                        prefs = prefs,
                        repository = repository,
                        playerController = playerController
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val now = System.currentTimeMillis()
        val window = prefs.doublePressWindowMs

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            if (now - lastVolUpPress <= window) {
                lastVolUpPress = 0L
                StealthRecorderService.startRecording(this)
                return true
            } else {
                lastVolUpPress = now
            }
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            if (now - lastVolDownPress <= window) {
                lastVolDownPress = 0L
                StealthRecorderService.stopRecording(this)
                return true
            } else {
                lastVolDownPress = now
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        playerController.release()
    }
}

@Composable
fun MainAppContent(
    prefs: VaultPreferences,
    repository: RecordingRepository,
    playerController: AudioPlayerController
) {
    var isVaultUnlocked by remember { mutableStateOf(!prefs.isDecoyEnabled) }
    var currentScreen by remember { mutableStateOf(AppScreen.VAULT) }
    var showPinDialog by remember { mutableStateOf(false) }

    // Request Audio & Notification permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle result
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    if (!isVaultUnlocked) {
        // Camouflage Decoy Screen (Custom Image, Calculator, or Notes)
        DecoyScreen(
            prefs = prefs,
            onUnlockRequest = {
                showPinDialog = true
            },
            onDirectUnlockSuccess = {
                isVaultUnlocked = true
                currentScreen = AppScreen.VAULT
            }
        )

        if (showPinDialog) {
            VaultPinDialog(
                prefs = prefs,
                onDismiss = { showPinDialog = false },
                onSuccess = {
                    showPinDialog = false
                    isVaultUnlocked = true
                    currentScreen = AppScreen.VAULT
                }
            )
        }
    } else {
        // Vault Unlocked
        when (currentScreen) {
            AppScreen.VAULT -> {
                BackHandler {
                    // Back locks the vault and returns to camouflage decoy
                    playerController.stopPlayback()
                    isVaultUnlocked = false
                }

                VaultMainScreen(
                    repository = repository,
                    playerController = playerController,
                    prefs = prefs,
                    onOpenSettings = { currentScreen = AppScreen.SETTINGS },
                    onRelock = {
                        playerController.stopPlayback()
                        isVaultUnlocked = false
                    }
                )
            }
            AppScreen.SETTINGS -> {
                BackHandler {
                    currentScreen = AppScreen.VAULT
                }

                SettingsScreen(
                    prefs = prefs,
                    onBack = { currentScreen = AppScreen.VAULT }
                )
            }
        }
    }
}
