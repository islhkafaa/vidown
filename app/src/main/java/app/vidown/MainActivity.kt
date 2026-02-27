package app.vidown

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.data.repository.AppTheme
import app.vidown.domain.manager.UpdateResult
import app.vidown.ui.theme.VidownTheme
import app.vidown.ui.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    val splashScreen = installSplashScreen()
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val settingsViewModel: SettingsViewModel = viewModel()
      val appTheme by settingsViewModel.themeState.collectAsState()

      val isDarkTheme =
              when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
              }

      VidownTheme(darkTheme = isDarkTheme) {
        var showStartupUpdateDialog by remember { mutableStateOf(false) }
        var sharedUrl by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
          val intent = this@MainActivity.intent
          if (intent?.action == android.content.Intent.ACTION_SEND && intent.type == "text/plain") {
            intent.getStringExtra(android.content.Intent.EXTRA_TEXT)?.let { sharedUrl = it }
          }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          val notificationPermissionLauncher =
                  rememberLauncherForActivityResult(
                          contract = ActivityResultContracts.RequestPermission()
                  ) {}

          LaunchedEffect(Unit) {
            val granted =
                    ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
              notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
          }
        }

        LaunchedEffect(Unit) { settingsViewModel.checkForUpdates(BuildConfig.VERSION_NAME) }

        val updateState by settingsViewModel.updateState.collectAsState()
        val downloadProgress by settingsViewModel.downloadProgress.collectAsState()

        LaunchedEffect(updateState) {
          if (updateState is UpdateResult.UpdateAvailable) {
            showStartupUpdateDialog = true
          }
        }

        if (showStartupUpdateDialog && updateState is UpdateResult.UpdateAvailable) {
          val state = updateState as UpdateResult.UpdateAvailable
          AlertDialog(
                  onDismissRequest = {
                    showStartupUpdateDialog = false
                    settingsViewModel.resetUpdateState()
                  },
                  title = { Text("Update Available") },
                  text = {
                    androidx.compose.foundation.layout.Column {
                      Text(
                              "A new version of Vidown (v${state.version}) is available. Would you like to install it now?"
                      )
                      if (downloadProgress != null) {
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.LinearProgressIndicator(
                                progress = { downloadProgress ?: 0f },
                                modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                                text = "Downloading: ${(downloadProgress!! * 100).toInt()}%",
                                style =
                                        androidx.compose.material3.MaterialTheme.typography
                                                .bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                        )
                      }
                    }
                  },
                  confirmButton = {
                    TextButton(
                            onClick = {
                              settingsViewModel.downloadUpdate(
                                      state.downloadUrl,
                                      "vidown-update-v${state.version}.apk"
                              )
                            },
                            enabled = downloadProgress == null
                    ) { Text(if (downloadProgress != null) "Downloading..." else "Update") }
                  },
                  dismissButton = {
                    TextButton(
                            onClick = {
                              showStartupUpdateDialog = false
                              settingsViewModel.resetUpdateState()
                            },
                            enabled = downloadProgress == null
                    ) { Text("Later") }
                  }
          )
        }

        app.vidown.ui.navigation.MainNavigation(initialUrl = sharedUrl)
      }
    }
  }
}
