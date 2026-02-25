package app.vidown.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.data.worker.DownloadWorker
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.Format
import app.vidown.domain.models.VideoInfo
import app.vidown.ui.viewmodel.HomeUiState
import app.vidown.ui.viewmodel.HomeViewModel
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var urlInput by rememberSaveable { mutableStateOf("") }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = "Vidown",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedContent(
                            targetState = uiState is HomeUiState.Loading,
                            label = "search_icon"
                        ) { loading ->
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        TextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text(
                                    text = "Paste a video URL...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = {
                                keyboardController?.hide()
                                viewModel.fetchVideoInfo(urlInput)
                            })
                        )

                        AnimatedVisibility(visible = urlInput.isNotEmpty()) {
                            IconButton(onClick = {
                                urlInput = ""
                                viewModel.resetState()
                            }) {
                                Icon(
                                    Icons.Rounded.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        var isPressed by rememberSaveable { mutableStateOf(false) }
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.9f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "btn_scale"
                        )

                        Box(
                            modifier = Modifier
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    if (urlInput.isNotEmpty() && uiState !is HomeUiState.Loading)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                )
                                .pointerInput(urlInput, uiState) {
                                    if (urlInput.isNotEmpty() && uiState !is HomeUiState.Loading) {
                                        detectTapGestures(
                                            onPress = {
                                                isPressed = true
                                                tryAwaitRelease()
                                                isPressed = false
                                            },
                                            onTap = {
                                                keyboardController?.hide()
                                                viewModel.fetchVideoInfo(urlInput)
                                            }
                                        )
                                    }
                                }
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ArrowForward,
                                contentDescription = "Fetch",
                                tint = if (urlInput.isNotEmpty() && uiState !is HomeUiState.Loading)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = uiState,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "home_state",
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) { state ->
            when (state) {
                is HomeUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Text(
                                text = "Ready to download",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Paste any video URL in the search bar above to fetch available formats",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                is HomeUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is HomeUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Card(
                            modifier = Modifier.padding(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                is HomeUiState.Success -> {
                    VideoDetailsContent(
                        videoInfo = state.videoInfo,
                        onFormatSelected = { format ->
                            val downloadFormatId = if (format.isVideo && format.acodec == "none") {
                                "${format.formatId}+bestaudio"
                            } else {
                                format.formatId
                            }

                            val request = DownloadRequest(
                                url = urlInput,
                                title = state.videoInfo.title,
                                thumbnailUrl = state.videoInfo.thumbnailUrl,
                                formatId = downloadFormatId
                            )

                            DownloadQueueRepository.addDownload(request)

                            val inputData = Data.Builder()
                                .putString(DownloadWorker.KEY_URL, urlInput)
                                .putString(DownloadWorker.KEY_FORMAT_ID, downloadFormatId)
                                .putString(DownloadWorker.KEY_REQUEST_ID, request.id.toString())
                                .putString(DownloadWorker.KEY_TITLE, state.videoInfo.title)
                                .putLong(DownloadWorker.KEY_TOTAL_BYTES, format.displaySize)
                                .build()

                            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                                .setInputData(inputData)
                                .build()

                            WorkManager.getInstance(context).enqueue(workRequest)
                        }
                    )
                }
            }
        }
    }
}
}

@Composable
fun VideoDetailsContent(
    videoInfo: VideoInfo,
    onFormatSelected: (Format) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box {
                    if (!videoInfo.thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = videoInfo.thumbnailUrl,
                            contentDescription = "Thumbnail",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.75f)
                                        ),
                                        startY = 80f
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = videoInfo.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!videoInfo.uploader.isNullOrBlank()) {
                                Text(
                                    text = videoInfo.uploader,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = videoInfo.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (!videoInfo.uploader.isNullOrBlank()) {
                                Text(
                                    text = videoInfo.uploader,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Available Formats",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        val validFormats = videoInfo.formats
            .filter { !it.formatId.contains("storyboard") }
            .sortedByDescending { it.displaySize }

        items(validFormats) { format ->
            FormatChip(format = format, onClick = { onFormatSelected(format) })
        }
    }
}

@Composable
fun FormatChip(
    format: Format,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                val isAudioOnly = !format.isVideo || format.acodec == "none" && format.resolution == null
                val chipLabel = format.resolution ?: "Audio"
                val chipColor = if (format.isVideo && format.acodec != "none")
                    MaterialTheme.colorScheme.primaryContainer
                else if (format.isVideo)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.tertiaryContainer

                val chipTextColor = if (format.isVideo && format.acodec != "none")
                    MaterialTheme.colorScheme.onPrimaryContainer
                else if (format.isVideo)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onTertiaryContainer

                SuggestionChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            text = chipLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        disabledContainerColor = chipColor,
                        disabledLabelColor = chipTextColor
                    ),
                    border = null
                )

                Text(
                    text = format.ext.uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!format.acodec.isNullOrBlank() && format.acodec != "none" && format.isVideo) {
                    Text(
                        text = "+ Audio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            val mb = format.displaySize / (1024 * 1024).toDouble()
            if (mb > 0) {
                Text(
                    text = String.format("%.1f MB", mb),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
