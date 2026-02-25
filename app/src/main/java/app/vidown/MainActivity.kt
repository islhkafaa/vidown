package app.vidown

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.data.repository.AppTheme
import app.vidown.domain.manager.UpdateResult
import app.vidown.ui.screen.HomeScreen
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

            val isDarkTheme = when (appTheme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> isSystemInDarkTheme()
            }

            VidownTheme(darkTheme = isDarkTheme) {
                var showStartupUpdateDialog by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    settingsViewModel.checkForUpdates("0.4.0")
                }

                val updateState by settingsViewModel.updateState.collectAsState()

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
                        text = { Text("A new version of Vidown (v${state.version}) is available. Would you like to install it now?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    settingsViewModel.downloadUpdate(state.downloadUrl, "vidown-update-v${state.version}.apk")
                                    showStartupUpdateDialog = false
                                    settingsViewModel.resetUpdateState()
                                }
                            ) {
                                Text("Update")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showStartupUpdateDialog = false
                                    settingsViewModel.resetUpdateState()
                                }
                            ) {
                                Text("Later")
                            }
                        }
                    )
                }

                app.vidown.ui.navigation.MainNavigation()
            }
        }
    }
}
