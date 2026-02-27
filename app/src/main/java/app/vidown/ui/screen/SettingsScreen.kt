package app.vidown.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.HighQuality
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.BuildConfig
import app.vidown.data.repository.AppTheme
import app.vidown.domain.manager.UpdateResult
import app.vidown.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
  val context = LocalContext.current
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val currentTheme by viewModel.themeState.collectAsState()
  val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
  val updateState by viewModel.updateState.collectAsState()
  val extractorUpdateResult by viewModel.extractorUpdateResult.collectAsState()
  val isUpdatingExtractors by viewModel.isUpdatingExtractors.collectAsState()
  val downloadUriState by viewModel.downloadUriState.collectAsState()
  val concurrentDownloadsState by viewModel.concurrentDownloadsState.collectAsState()
  val defaultResolutionState by viewModel.defaultResolutionState.collectAsState()
  val downloadProgress by viewModel.downloadProgress.collectAsState()

  val launcher =
          rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
              val contentResolver = context.contentResolver
              val takeFlags: Int =
                      android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                              android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
              contentResolver.takePersistableUriPermission(uri, takeFlags)
              viewModel.setDownloadUri(uri.toString())
            }
          }

  var showUpdateDialog by remember { mutableStateOf(false) }

  LaunchedEffect(updateState) {
    if (updateState != null) {
      showUpdateDialog = true
    }
  }

  if (showUpdateDialog && updateState != null) {
    androidx.compose.material3.AlertDialog(
            onDismissRequest = {
              showUpdateDialog = false
              viewModel.resetUpdateState()
            },
            title = { Text("Update Check") },
            text = {
              when (val state = updateState) {
                is UpdateResult.UpdateAvailable -> {
                  Column {
                    Text(
                            "A new version (v${state.version}) is available. Would you like to download and install it?"
                    )
                    if (downloadProgress != null) {
                      Spacer(modifier = Modifier.height(16.dp))
                      androidx.compose.material3.LinearProgressIndicator(
                              progress = { downloadProgress ?: 0f },
                              modifier = Modifier.fillMaxWidth(),
                      )
                      Text(
                              text = "Downloading: ${(downloadProgress!! * 100).toInt()}%",
                              style = MaterialTheme.typography.bodySmall,
                              modifier = Modifier.padding(top = 4.dp)
                      )
                    }
                  }
                }
                is UpdateResult.UpToDate -> {
                  Text("You are already on the latest version!")
                }
                is UpdateResult.Error -> {
                  Text("Failed to check for updates: ${state.message}")
                }
                null -> {}
              }
            },
            confirmButton = {
              if (updateState is UpdateResult.UpdateAvailable) {
                androidx.compose.material3.TextButton(
                        onClick = {
                          val state = updateState as UpdateResult.UpdateAvailable
                          viewModel.downloadUpdate(
                                  state.downloadUrl,
                                  "vidown-update-v${state.version}.apk"
                          )
                        },
                        enabled = downloadProgress == null
                ) { Text(if (downloadProgress != null) "Downloading..." else "Download") }
              } else {
                androidx.compose.material3.TextButton(
                        onClick = {
                          showUpdateDialog = false
                          viewModel.resetUpdateState()
                        }
                ) { Text("OK") }
              }
            },
            dismissButton = {
              if (updateState is UpdateResult.UpdateAvailable) {
                androidx.compose.material3.TextButton(
                        onClick = {
                          showUpdateDialog = false
                          viewModel.resetUpdateState()
                        },
                        enabled = downloadProgress == null
                ) { Text("Cancel") }
              }
            }
    )
  }

  Scaffold(
          modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
          topBar = {
            LargeTopAppBar(
                    title = {
                      Text(
                              text = "Settings",
                              style =
                                      MaterialTheme.typography.headlineMedium.copy(
                                              fontWeight = FontWeight.Bold
                                      ),
                      )
                    },
                    scrollBehavior = scrollBehavior,
                    colors =
                            TopAppBarDefaults.largeTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                            ),
            )
          }
  ) { innerPadding ->
    LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item { SettingsSectionHeader(title = "Appearance", icon = Icons.Rounded.Palette) }

      item {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                elevation = CardDefaults.cardElevation(0.dp)
        ) {
          Column {
            ThemeOptionRow(
                    title = "System Default",
                    subtitle = "Follow device setting",
                    icon = Icons.Rounded.PhoneAndroid,
                    isSelected = currentTheme == AppTheme.SYSTEM,
                    onClick = { viewModel.setTheme(AppTheme.SYSTEM) }
            )
            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ThemeOptionRow(
                    title = "Light",
                    subtitle = "Bright, clean interface",
                    icon = Icons.Rounded.LightMode,
                    isSelected = currentTheme == AppTheme.LIGHT,
                    onClick = { viewModel.setTheme(AppTheme.LIGHT) }
            )
            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            ThemeOptionRow(
                    title = "Dark",
                    subtitle = "Easy on the eyes",
                    icon = Icons.Rounded.DarkMode,
                    isSelected = currentTheme == AppTheme.DARK,
                    onClick = { viewModel.setTheme(AppTheme.DARK) }
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
        SettingsSectionHeader(title = "Downloads", icon = Icons.Rounded.Folder)
      }

      item {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
          Column {
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clickable { launcher.launch(null) }
                                    .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = "Custom Download Location",
                        style =
                                MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                )
                )
                Text(
                        text =
                                if (downloadUriState.isNullOrEmpty())
                                        "Default (Movies/Music -> Vidown)"
                                else "Custom Directory Selected",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (!downloadUriState.isNullOrEmpty()) {
                IconButton(
                        onClick = { viewModel.setDownloadUri(null) },
                        modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                          imageVector = Icons.Rounded.Close,
                          contentDescription = "Clear Custom Location",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            var concurrentInput by
                    remember(concurrentDownloadsState) {
                      mutableStateOf(concurrentDownloadsState.toString())
                    }

            Row(
                    modifier =
                            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                        text = "Concurrent Downloads",
                        style =
                                MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium
                                )
                )
                Text(
                        text = "Simultaneous active files",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              OutlinedTextField(
                      value = concurrentInput,
                      onValueChange = { newValue ->
                        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                          concurrentInput = newValue
                          newValue.toIntOrNull()?.let { limit ->
                            if (limit in 1..20) {
                              viewModel.setConcurrentDownloads(limit)
                            }
                          }
                        }
                      },
                      modifier = Modifier.width(80.dp),
                      singleLine = true,
                      textStyle = MaterialTheme.typography.bodyMedium,
                      keyboardOptions =
                              androidx.compose.foundation.text.KeyboardOptions(
                                      keyboardType =
                                              androidx.compose.ui.text.input.KeyboardType.Number
                              ),
                      shape = RoundedCornerShape(12.dp)
              )
            }

            HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
              Row(
                      modifier =
                              Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                          text = "Resolution Preference",
                          style =
                                  MaterialTheme.typography.bodyLarge.copy(
                                          fontWeight = FontWeight.Medium
                                  )
                  )
                }
                Icon(
                        imageVector = Icons.Rounded.HighQuality,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              listOf("Always Best Video", "Always Ask").forEach { option ->
                Row(
                        modifier =
                                Modifier.fillMaxWidth()
                                        .clickable { viewModel.setDefaultResolution(option) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                          selected = defaultResolutionState == option,
                          onClick = { viewModel.setDefaultResolution(option) }
                  )
                  Text(
                          text = option,
                          style = MaterialTheme.typography.bodyMedium,
                          modifier = Modifier.padding(start = 8.dp)
                  )
                }
              }
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(8.dp))
        SettingsSectionHeader(title = "About", icon = Icons.Rounded.PhoneAndroid)
      }

      item {
        Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors =
                        CardDefaults.cardColors(
                                containerColor =
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                elevation = CardDefaults.cardElevation(0.dp)
        ) {
          Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                      text = "Vidown",
                      style =
                              MaterialTheme.typography.titleMedium.copy(
                                      fontWeight = FontWeight.SemiBold
                              )
              )
              Text(
                      text = "Video downloader for Android",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Box(
                    modifier =
                            Modifier.clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
              Text(
                      text = "v${BuildConfig.VERSION_NAME}",
                      style =
                              MaterialTheme.typography.labelMedium.copy(
                                      fontWeight = FontWeight.Bold
                              ),
                      color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          HorizontalDivider(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )

          Row(
                  modifier =
                          Modifier.fillMaxWidth()
                                  .clickable(enabled = !isCheckingUpdate) {
                                    viewModel.checkForUpdates(BuildConfig.VERSION_NAME)
                                  }
                                  .padding(horizontal = 20.dp, vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
                    text = "Check for updates",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
            if (isCheckingUpdate) {
              androidx.compose.material3.CircularProgressIndicator(
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp
              )
            } else {
              Icon(
                      imageVector = Icons.Default.Refresh,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary
              )
            }
          }

          HorizontalDivider(
                  modifier = Modifier.padding(horizontal = 16.dp),
                  color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
          )

          Row(
                  modifier =
                          Modifier.fillMaxWidth()
                                  .clickable(enabled = !isUpdatingExtractors) {
                                    viewModel.updateExtractors()
                                  }
                                  .padding(horizontal = 20.dp, vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                      text = "Update Download Extractors",
                      style =
                              MaterialTheme.typography.bodyLarge.copy(
                                      fontWeight = FontWeight.Medium
                              )
              )
              if (extractorUpdateResult != null) {
                val msg =
                        if (extractorUpdateResult?.isSuccess == true) {
                          val status = extractorUpdateResult?.getOrNull()
                          if (status ==
                                          com.yausername.youtubedl_android.YoutubeDL.UpdateStatus
                                                  .ALREADY_UP_TO_DATE
                          )
                                  "Up to date"
                          else "Updated successfully"
                        } else {
                          "Update failed"
                        }
                val color =
                        if (extractorUpdateResult?.isSuccess == true)
                                MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                Text(text = msg, style = MaterialTheme.typography.bodySmall, color = color)
              }
            }
            if (isUpdatingExtractors) {
              androidx.compose.material3.CircularProgressIndicator(
                      modifier = Modifier.size(20.dp),
                      strokeWidth = 2.dp
              )
            } else {
              Icon(
                      imageVector = Icons.Default.Refresh,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingsSectionHeader(title: String, icon: ImageVector) {
  Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
  ) {
    Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
    )
    Text(
            text = title,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
    )
  }
}

@Composable
fun ThemeOptionRow(
        title: String,
        subtitle: String,
        icon: ImageVector,
        isSelected: Boolean,
        onClick: () -> Unit
) {
  Row(
          modifier =
                  Modifier.fillMaxWidth()
                          .clickable(onClick = onClick)
                          .padding(horizontal = 16.dp, vertical = 14.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Box(
            modifier =
                    Modifier.size(40.dp)
                            .clip(CircleShape)
                            .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                            ),
            contentAlignment = Alignment.Center
    ) {
      Icon(
              imageVector = icon,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
              tint =
                      if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                      else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    Column(modifier = Modifier.weight(1f)) {
      Text(
              text = title,
              style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
      )
      Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
    )
  }
}
