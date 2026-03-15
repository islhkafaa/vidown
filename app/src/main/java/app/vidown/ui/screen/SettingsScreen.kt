package app.vidown.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.BuildConfig
import app.vidown.data.repository.AppTheme
import app.vidown.domain.manager.UpdateResult
import app.vidown.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(modifier: Modifier = Modifier, viewModel: SettingsViewModel = viewModel()) {
    val context = LocalContext.current
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val currentTheme by viewModel.themeState.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val isUpdatingExtractors by viewModel.isUpdatingExtractors.collectAsState()
    val downloadUriState by viewModel.downloadUriState.collectAsState()
    val concurrentDownloadsState by viewModel.concurrentDownloadsState.collectAsState()
    val defaultResolutionState by viewModel.defaultResolutionState.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val wifiOnly by viewModel.wifiOnlyState.collectAsState()
    val autoUpdateExtractors by viewModel.autoUpdateExtractorsState.collectAsState()

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
        AlertDialog(
            onDismissRequest = {
                viewModel.resetUpdateState()
            },
            title = {
                Text(
                    "Update Available",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                when (val state = updateState) {
                    is UpdateResult.UpdateAvailable -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "A new version (v${state.version}) is available.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (downloadProgress != null) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    LinearProgressIndicator(
                                        progress = { downloadProgress ?: 0f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                    )
                                    Text(
                                        text = "${(downloadProgress!! * 100).toInt()}% Downloaded",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.End,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    is UpdateResult.UpToDate -> {
                        Text("You are using the latest version of Vidown.")
                    }

                    is UpdateResult.Error -> {
                        Text(
                            "Update failed: ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    else -> {}
                }
            },
            confirmButton = {
                if (updateState is UpdateResult.UpdateAvailable) {
                    Button(
                        onClick = {
                            val state = updateState as UpdateResult.UpdateAvailable
                            viewModel.downloadUpdate(
                                state.downloadUrl,
                                "vidown-update-v${state.version}.apk"
                            )
                        },
                        enabled = downloadProgress == null,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (downloadProgress != null) "Downloading..." else "Install Update") }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.resetUpdateState()
                        }
                    ) { Text("OK") }
                }
            },
            dismissButton = {
                if (updateState is UpdateResult.UpdateAvailable) {
                    TextButton(
                        onClick = {
                            viewModel.resetUpdateState()
                        },
                        enabled = downloadProgress == null
                    ) { Text("Later", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = Color.Unspecified,
                    actionIconContentColor = Color.Unspecified
                ),
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                SettingsGroup(title = "Appearance") {
                    ThemeOptionRow(
                        title = "System Default",
                        subtitle = "Follow device setting",
                        icon = Icons.Rounded.AutoAwesome,
                        isSelected = currentTheme == AppTheme.SYSTEM,
                        onClick = { viewModel.setTheme(AppTheme.SYSTEM) }
                    )
                    SettingsDivider()
                    ThemeOptionRow(
                        title = "Light",
                        subtitle = "Bright interface",
                        icon = Icons.Rounded.LightMode,
                        isSelected = currentTheme == AppTheme.LIGHT,
                        onClick = { viewModel.setTheme(AppTheme.LIGHT) }
                    )
                    SettingsDivider()
                    ThemeOptionRow(
                        title = "Dark",
                        subtitle = "Dark interface",
                        icon = Icons.Rounded.DarkMode,
                        isSelected = currentTheme == AppTheme.DARK,
                        onClick = { viewModel.setTheme(AppTheme.DARK) }
                    )
                }
            }

            item {
                SettingsGroup(title = "Storage & Downloads") {
                    SettingsActionRow(
                        title = "Download Location",
                        subtitle = if (downloadUriState.isNullOrEmpty()) "Internal storage" else "Custom folder",
                        icon = Icons.Rounded.FolderOpen,
                        onClick = { launcher.launch(null) },
                        trailing = {
                            if (!downloadUriState.isNullOrEmpty()) {
                                IconButton(onClick = { viewModel.setDownloadUri(null) }) {
                                    Icon(Icons.Rounded.Close, null, modifier = Modifier.size(18.dp))
                                }
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                    null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    )
                    SettingsDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Concurrent Downloads",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Max parallel downloads",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        var concurrentInput by remember(concurrentDownloadsState) {
                            mutableStateOf(
                                concurrentDownloadsState.toString()
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                            border = BorderStroke(
                                1.dp, Brush.linearGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.15f),
                                        Color.White.copy(alpha = 0.02f)
                                    )
                                )
                            ),
                            modifier = Modifier.width(64.dp)
                        ) {
                            TextField(
                                value = concurrentInput,
                                onValueChange = { newValue ->
                                    if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                                        concurrentInput = newValue
                                        newValue.toIntOrNull()?.let {
                                            if (it in 1..20) viewModel.setConcurrentDownloads(it)
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )
                        }
                    }
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Download only on Wi-Fi",
                        subtitle = "Pause downloads on cellular data",
                        icon = Icons.Rounded.Wifi,
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )
                    SettingsDivider()
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Default Quality",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Always Best Video", "Always Ask").forEach { option ->
                                val selected = defaultResolutionState == option
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setDefaultResolution(option) },
                                    label = { Text(option) },
                                    shape = CircleShape,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selected,
                                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                                        selectedBorderColor = Color.Transparent
                                    )
                                )
                            }
                        }
                    }
                }
            }

            item {
                SettingsGroup(title = "Updates") {
                    SettingsActionRow(
                        title = "Update Extractors",
                        subtitle = "Update download engines",
                        icon = Icons.Rounded.Analytics,
                        onClick = { if (!isUpdatingExtractors) viewModel.updateExtractors() },
                        trailing = {
                            if (isUpdatingExtractors) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.CloudDownload,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = "Auto-Update Engines",
                        subtitle = "Keep download engines updated",
                        icon = Icons.Rounded.AutoMode,
                        checked = autoUpdateExtractors,
                        onCheckedChange = { viewModel.setAutoUpdateExtractors(it) }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = "App Update",
                        subtitle = "Version ${BuildConfig.VERSION_NAME}",
                        icon = Icons.Rounded.VerifiedUser,
                        onClick = { if (!isCheckingUpdate) viewModel.checkForUpdates(BuildConfig.VERSION_NAME) },
                        trailing = {
                            if (isCheckingUpdate) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Cached,
                                    null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = "About Vidown",
                        subtitle = "Video downloader for Android",
                        icon = Icons.Rounded.Info,
                        onClick = { /* Could open a link */ },
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Rounded.OpenInNew,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 1.2.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 8.dp)
        )
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
            border = BorderStroke(
                1.dp, Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                border = BorderStroke(
                    1.dp, Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing()
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
                border = BorderStroke(
                    1.dp, Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.02f)
                        )
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    )
}

@Composable
fun ThemeOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) else null,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.15f
                ),
                border = BorderStroke(
                    1.dp, Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
