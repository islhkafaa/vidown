package app.vidown.ui.screen

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.BuildConfig
import app.vidown.R
import app.vidown.data.repository.AppTheme
import app.vidown.domain.manager.UpdateResult
import app.vidown.ui.component.*
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
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val wifiOnly by viewModel.wifiOnlyState.collectAsState()
    val autoUpdateExtractors by viewModel.autoUpdateExtractorsState.collectAsState()
    val concurrentFragments by viewModel.concurrentFragmentsState.collectAsState()
    val bufferSize by viewModel.bufferSizeState.collectAsState()
    val forceIpv4 by viewModel.forceIpv4State.collectAsState()
    val currentLanguage by viewModel.languageState.collectAsState()

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
                    stringResource(R.string.update_available),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                when (val state = updateState) {
                    is UpdateResult.UpdateAvailable -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                stringResource(R.string.new_version_available, state.version),
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
                                        text = stringResource(
                                            R.string.download_percent,
                                            (downloadProgress!! * 100).toInt()
                                        ),
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
                        Text(stringResource(R.string.up_to_date))
                    }

                    is UpdateResult.Error -> {
                        Text(
                            stringResource(R.string.update_failed, state.message),
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
                    ) {
                        Text(
                            if (downloadProgress != null) stringResource(R.string.downloading) else stringResource(
                                R.string.install_update
                            )
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            viewModel.resetUpdateState()
                        }
                    ) { Text(stringResource(R.string.ok)) }
                }
            },
            dismissButton = {
                if (updateState is UpdateResult.UpdateAvailable) {
                    TextButton(
                        onClick = {
                            viewModel.resetUpdateState()
                        },
                        enabled = downloadProgress == null
                    ) {
                        Text(
                            stringResource(R.string.later),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(bottom = 96.dp)
            )
        },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
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
                SettingsGroup(title = stringResource(R.string.appearance)) {
                    ThemeOptionRow(
                        title = stringResource(R.string.system_default),
                        subtitle = stringResource(R.string.follow_device_setting),
                        icon = Icons.Rounded.AutoAwesome,
                        isSelected = currentTheme == AppTheme.SYSTEM,
                        onClick = { viewModel.setTheme(AppTheme.SYSTEM) }
                    )
                    SettingsDivider()
                    ThemeOptionRow(
                        title = stringResource(R.string.light),
                        subtitle = stringResource(R.string.bright_interface),
                        icon = Icons.Rounded.LightMode,
                        isSelected = currentTheme == AppTheme.LIGHT,
                        onClick = { viewModel.setTheme(AppTheme.LIGHT) }
                    )
                    SettingsDivider()
                    ThemeOptionRow(
                        title = stringResource(R.string.dark),
                        subtitle = stringResource(R.string.dark_interface),
                        icon = Icons.Rounded.DarkMode,
                        isSelected = currentTheme == AppTheme.DARK,
                        onClick = { viewModel.setTheme(AppTheme.DARK) }
                    )
                    SettingsDivider()
                    ThemeOptionRow(
                        title = stringResource(R.string.language),
                        subtitle = if (currentLanguage == "in") stringResource(R.string.indonesian) else stringResource(R.string.english),
                        icon = Icons.Rounded.Language,
                        isSelected = false,
                        onClick = {
                            val next = if (currentLanguage == "en") "in" else "en"
                            viewModel.setLanguage(next)
                        }
                    )
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.storage_downloads)) {
                    SettingsActionRow(
                        title = stringResource(R.string.download_location),
                        subtitle = if (downloadUriState.isNullOrEmpty()) stringResource(R.string.internal_storage) else stringResource(
                            R.string.custom_folder
                        ),
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
                                stringResource(R.string.concurrent_downloads),
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                stringResource(R.string.max_parallel_downloads),
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
                        title = stringResource(R.string.download_only_on_wifi),
                        subtitle = stringResource(R.string.pause_on_cellular),
                        icon = Icons.Rounded.Wifi,
                        checked = wifiOnly,
                        onCheckedChange = { viewModel.setWifiOnly(it) }
                    )
                    SettingsDivider()
                }
            }

            item {
                SettingsGroup(title = stringResource(R.string.updates)) {
                    SettingsActionRow(
                        title = stringResource(R.string.update_extractors),
                        subtitle = stringResource(R.string.update_engines),
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
                        title = stringResource(R.string.auto_update_engines),
                        subtitle = stringResource(R.string.keep_engines_updated),
                        icon = Icons.Rounded.AutoMode,
                        checked = autoUpdateExtractors,
                        onCheckedChange = { viewModel.setAutoUpdateExtractors(it) }
                    )
                    SettingsDivider()
                    SettingsActionRow(
                        title = stringResource(R.string.app_update),
                        subtitle = stringResource(
                            R.string.version_format,
                            BuildConfig.VERSION_NAME
                        ),
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
                        title = stringResource(R.string.about_vidown),
                        subtitle = stringResource(R.string.about_subtitle),
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

            item {
                SettingsGroup(title = stringResource(R.string.engine_performance)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            stringResource(R.string.concurrent_fragments),
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        GlassSurface(
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1, 4, 8, 16).forEach { count ->
                                    val isSelected = concurrentFragments == count
                                    Surface(
                                        onClick = { viewModel.setConcurrentFragments(count) },
                                        shape = CircleShape,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = count.toString(),
                                            modifier = Modifier.padding(vertical = 10.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            stringResource(R.string.fragments_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    SettingsDivider()
                    SettingsActionRow(
                        title = stringResource(R.string.buffer_size),
                        subtitle = when (bufferSize) {
                            "Standard" -> stringResource(R.string.standard)
                            "High" -> stringResource(R.string.high)
                            "Extreme" -> stringResource(R.string.extreme)
                            else -> bufferSize
                        },
                        icon = Icons.Rounded.Storage,
                        onClick = {
                            val next = when (bufferSize) {
                                "Standard" -> "High"
                                "High" -> "Extreme"
                                else -> "Standard"
                            }
                            viewModel.setBufferSize(next)
                        },
                        trailing = {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForwardIos,
                                null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    )
                    SettingsDivider()
                    SettingsToggleRow(
                        title = stringResource(R.string.force_ipv4),
                        subtitle = stringResource(R.string.ipv4_description),
                        icon = Icons.Rounded.NetworkCheck,
                        checked = forceIpv4,
                        onCheckedChange = { viewModel.setForceIpv4(it) }
                    )
                }
            }
        }
    }
}
