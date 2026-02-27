package app.vidown.ui.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.data.repository.DownloadQueueRepository
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.DownloadStatus
import app.vidown.domain.models.Format
import app.vidown.domain.models.VideoInfo
import app.vidown.ui.viewmodel.HomeUiState
import app.vidown.ui.viewmodel.HomeViewModel
import app.vidown.ui.viewmodel.QueueViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
        modifier: Modifier = Modifier,
        homeViewModel: HomeViewModel = viewModel(),
        queueViewModel: QueueViewModel = viewModel()
) {
  val uiState by homeViewModel.uiState.collectAsState()
  val queue by queueViewModel.downloadQueue.collectAsState()
  val keyboardController = LocalSoftwareKeyboardController.current
  var urlInput by rememberSaveable { mutableStateOf("") }

  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()

  var showQueueSheet by rememberSaveable { mutableStateOf(false) }
  val sheetState = rememberModalBottomSheetState()

  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

  val activeCount =
          queue.count {
            it.status == DownloadStatus.Downloading || it.status == DownloadStatus.Pending
          }

  if (showQueueSheet) {
    ModalBottomSheet(
            onDismissRequest = { showQueueSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {
              Box(
                      modifier =
                              Modifier.padding(vertical = 12.dp)
                                      .width(40.dp)
                                      .height(4.dp)
                                      .clip(CircleShape)
                                      .background(
                                              MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                      alpha = 0.4f
                                              )
                                      )
              )
            }
    ) { DownloadQueueSheetContent(queue = queue) }
  }

  Scaffold(
          modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
          snackbarHost = { SnackbarHost(snackbarHostState) },
          topBar = {
            LargeTopAppBar(
                    title = {
                      Text(
                              text = "Vidown",
                              style =
                                      MaterialTheme.typography.headlineMedium.copy(
                                              fontWeight = FontWeight.Bold
                                      ),
                      )
                    },
                    actions = {
                      IconButton(onClick = { showQueueSheet = true }) {
                        BadgedBox(
                                badge = {
                                  if (activeCount > 0) {
                                    Badge {
                                      Text(
                                              text = activeCount.toString(),
                                              style =
                                                      MaterialTheme.typography.labelSmall.copy(
                                                              fontSize = 10.sp
                                                      )
                                      )
                                    }
                                  }
                                }
                        ) {
                          Icon(
                                  imageVector = Icons.Rounded.Notifications,
                                  contentDescription = "Downloads"
                          )
                        }
                      }
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
    Column(
            modifier =
                    Modifier.fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(innerPadding)
    ) {
      Box(
              modifier =
                      Modifier.fillMaxWidth()
                              .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
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
            AnimatedContent(targetState = uiState is HomeUiState.Loading, label = "search_icon") {
                    loading ->
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
                    colors =
                            TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                            ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions =
                            KeyboardActions(
                                    onSearch = {
                                      keyboardController?.hide()
                                      homeViewModel.fetchVideoInfo(urlInput)
                                    }
                            )
            )

            AnimatedVisibility(visible = urlInput.isNotEmpty()) {
              IconButton(
                      onClick = {
                        urlInput = ""
                        homeViewModel.resetState()
                      }
              ) {
                Icon(
                        Icons.Rounded.Clear,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                )
              }
            }

            var isPressed by rememberSaveable { mutableStateOf(false) }
            val scale by
                    animateFloatAsState(
                            targetValue = if (isPressed) 0.9f else 1f,
                            animationSpec =
                                    spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                    ),
                            label = "btn_scale"
                    )

            Box(
                    modifier =
                            Modifier.scale(scale)
                                    .clip(CircleShape)
                                    .background(
                                            if (urlInput.isNotEmpty() &&
                                                            uiState !is HomeUiState.Loading
                                            )
                                                    MaterialTheme.colorScheme.primary
                                            else
                                                    MaterialTheme.colorScheme.onSurface.copy(
                                                            alpha = 0.12f
                                                    )
                                    )
                                    .pointerInput(urlInput, uiState) {
                                      if (urlInput.isNotEmpty() && uiState !is HomeUiState.Loading
                                      ) {
                                        detectTapGestures(
                                                onPress = {
                                                  isPressed = true
                                                  tryAwaitRelease()
                                                  isPressed = false
                                                },
                                                onTap = {
                                                  keyboardController?.hide()
                                                  homeViewModel.fetchVideoInfo(urlInput)
                                                }
                                        )
                                      }
                                    }
                                    .padding(10.dp)
            ) {
              Icon(
                      imageVector = Icons.Rounded.ArrowForward,
                      contentDescription = "Fetch",
                      tint =
                              if (urlInput.isNotEmpty() && uiState !is HomeUiState.Loading)
                                      MaterialTheme.colorScheme.onPrimary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
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
              modifier = Modifier.fillMaxSize().weight(1f)
      ) { state ->
        when (state) {
          is HomeUiState.Idle -> {
            IdleContent()
          }
          is HomeUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              CircularProgressIndicator()
            }
          }
          is HomeUiState.Error -> {
            ErrorContent(message = state.message)
          }
          is HomeUiState.Success -> {
            VideoDetailsContent(
                    videoInfo = state.videoInfo,
                    onDownload = { format ->
                      homeViewModel.startDownload(state.videoInfo, format)
                      coroutineScope.launch { snackbarHostState.showSnackbar("Added to downloads") }
                      urlInput = ""
                      homeViewModel.resetState()
                    },
                    onDownloadPlaylist = {
                      homeViewModel.downloadPlaylist(state.videoInfo)
                      coroutineScope.launch {
                        snackbarHostState.showSnackbar("Added playlist to downloads")
                      }
                      urlInput = ""
                      homeViewModel.resetState()
                    },
                    homeViewModel = homeViewModel
            )
          }
        }
      }
    }
  }
}

@Composable
fun IdleContent() {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
    ) {
      Box(
              modifier =
                      Modifier.size(72.dp)
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

@Composable
fun ErrorContent(message: String) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Card(
            modifier = Modifier.padding(24.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                    )
    ) {
      Text(
              text = message,
              color = MaterialTheme.colorScheme.onErrorContainer,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(20.dp),
              style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@Composable
fun VideoDetailsContent(
        videoInfo: VideoInfo,
        onDownload: (Format) -> Unit,
        onDownloadPlaylist: () -> Unit,
        homeViewModel: app.vidown.ui.viewmodel.HomeViewModel
) {
  var defaultResolution by remember { mutableStateOf("Always Best Video") }

  LaunchedEffect(Unit) { defaultResolution = homeViewModel.getDefaultResolution() }

  LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp)
  ) {
    item { VideoInfoCard(videoInfo = videoInfo) }

    if (!videoInfo.entries.isNullOrEmpty()) {
      item {
        Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
                  text = "Playlist (${videoInfo.entries.size} videos)",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Button(onClick = onDownloadPlaylist, shape = RoundedCornerShape(12.dp)) {
            Icon(Icons.Rounded.PlaylistAdd, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Download All")
          }
        }
      }
      items(videoInfo.entries) { entry -> PlaylistEntryItem(entry = entry) }
    } else {
      item {
        if (defaultResolution == "Always Ask") {
          var isExpanded by rememberSaveable { mutableStateOf(true) }
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isExpanded = !isExpanded }
                                    .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                      text = "Available Formats",
                      style =
                              MaterialTheme.typography.titleMedium.copy(
                                      fontWeight = FontWeight.Bold
                              ),
                      color = MaterialTheme.colorScheme.onSurface
              )
              Icon(
                      imageVector =
                              if (isExpanded) Icons.Rounded.KeyboardArrowUp
                              else Icons.Rounded.KeyboardArrowDown,
                      contentDescription = "Toggle formats",
                      tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
            ) {
              Column(
                      modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                      verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                val validFormats =
                        videoInfo.formats
                                .filter { !it.formatId.contains("storyboard") }
                                .sortedByDescending { it.displaySize }

                validFormats.forEach { format ->
                  Card(
                          modifier =
                                  Modifier.fillMaxWidth()
                                          .clip(RoundedCornerShape(12.dp))
                                          .clickable { onDownload(format) },
                          shape = RoundedCornerShape(12.dp),
                          colors =
                                  CardDefaults.cardColors(
                                          containerColor =
                                                  MaterialTheme.colorScheme.surfaceVariant.copy(
                                                          alpha = 0.4f
                                                  )
                                  )
                  ) {
                    Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column {
                        Text(
                                text = format.resolution
                                                ?: if (format.acodec != "none") "Audio Only"
                                                else "Unknown",
                                style =
                                        MaterialTheme.typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Medium
                                        )
                        )
                        Text(
                                text =
                                        if (format.displaySize != null && format.displaySize > 0)
                                                "${format.displaySize / (1024 * 1024)} MB • ${format.ext.uppercase()}"
                                        else format.ext.uppercase(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                      Icon(
                              imageVector = Icons.Rounded.Download,
                              contentDescription = "Download",
                              modifier = Modifier.size(20.dp),
                              tint = MaterialTheme.colorScheme.primary
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      item {
        if (defaultResolution != "Always Ask") {
          Button(
                  onClick = {
                    val bestFormat =
                            videoInfo.formats
                                    .filter { it.isVideo && !it.formatId.contains("storyboard") }
                                    .maxByOrNull { it.height ?: 0 }
                                    ?: videoInfo.formats.firstOrNull()

                    bestFormat?.let { onDownload(it) }
                  },
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(16.dp),
                  contentPadding = PaddingValues(16.dp)
          ) {
            Icon(Icons.Rounded.Download, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("Download Best Resolution")
          }
        }
      }
    }
  }
}

@Composable
fun PlaylistEntryItem(entry: VideoInfo) {
  Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors =
                  CardDefaults.cardColors(
                          containerColor =
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                  )
  ) {
    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      if (!entry.thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
                model = entry.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(50.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
      }
      Column {
        Text(
                text = entry.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
        )
        Text(
                text = entry.uploader ?: "Unknown",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun VideoInfoCard(videoInfo: VideoInfo) {
  Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors =
                  CardDefaults.cardColors(
                          containerColor =
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                  ),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Box {
      if (!videoInfo.thumbnailUrl.isNullOrBlank()) {
        AsyncImage(
                model = videoInfo.thumbnailUrl,
                contentDescription = "Thumbnail",
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
        )
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                        Brush.verticalGradient(
                                                colors =
                                                        listOf(
                                                                Color.Transparent,
                                                                Color.Black.copy(alpha = 0.75f)
                                                        ),
                                                startY = 80f
                                        )
                                )
        )
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
          Text(
                  text = videoInfo.title,
                  style =
                          MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.SemiBold
                          ),
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
                  style =
                          MaterialTheme.typography.titleMedium.copy(
                                  fontWeight = FontWeight.SemiBold
                          ),
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

@Composable
fun FormatChip(format: Format, onClick: () -> Unit) {
  val scale = remember { Animatable(1f) }
  val coroutineScope = rememberCoroutineScope()

  Card(
          onClick = {
            coroutineScope.launch {
              scale.animateTo(1.05f, animationSpec = tween(100))
              scale.animateTo(
                      1f,
                      animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
              )
              onClick()
            }
          },
          modifier = Modifier.fillMaxWidth().scale(scale.value),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
          border = CardDefaults.outlinedCardBorder()
  ) {
    Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
      ) {
        val chipLabel = format.resolution ?: "Audio"
        val chipColor =
                if (format.isVideo && format.acodec != "none")
                        MaterialTheme.colorScheme.primaryContainer
                else if (format.isVideo) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.tertiaryContainer

        val chipTextColor =
                if (format.isVideo && format.acodec != "none")
                        MaterialTheme.colorScheme.onPrimaryContainer
                else if (format.isVideo) MaterialTheme.colorScheme.onSecondaryContainer
                else MaterialTheme.colorScheme.onTertiaryContainer

        SuggestionChip(
                onClick = {},
                enabled = false,
                label = {
                  Text(
                          text = chipLabel,
                          style =
                                  MaterialTheme.typography.labelMedium.copy(
                                          fontWeight = FontWeight.SemiBold
                                  )
                  )
                },
                colors =
                        SuggestionChipDefaults.suggestionChipColors(
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

@Composable
fun DownloadQueueSheetContent(queue: List<DownloadRequest>) {
  Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f).padding(horizontal = 16.dp)) {
    Text(
            text = "Active Downloads",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
    )

    if (queue.isEmpty()) {
      Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(
                text = "No active downloads",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
              modifier = Modifier.fillMaxWidth(),
              verticalArrangement = Arrangement.spacedBy(10.dp),
              contentPadding = PaddingValues(bottom = 32.dp)
      ) { items(queue, key = { it.id }) { request -> DownloadItemCard(request = request) } }
    }
  }
}

@Composable
fun DownloadItemCard(request: DownloadRequest, modifier: Modifier = Modifier) {
  val statusColor =
          when (request.status) {
            DownloadStatus.Success -> MaterialTheme.colorScheme.primary
            DownloadStatus.Failed -> MaterialTheme.colorScheme.error
            DownloadStatus.Downloading -> MaterialTheme.colorScheme.tertiary
            DownloadStatus.Paused -> MaterialTheme.colorScheme.secondary
            DownloadStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
            DownloadStatus.Cancelled -> MaterialTheme.colorScheme.outline
          }

  Card(
          modifier = modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors =
                  CardDefaults.cardColors(
                          containerColor =
                                  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                  ),
          elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
  ) {
    Row(modifier = Modifier.fillMaxWidth()) {
      Box(
              modifier =
                      Modifier.width(4.dp)
                              .height(
                                      if (request.status == DownloadStatus.Downloading ||
                                                      request.status == DownloadStatus.Paused
                                      )
                                              150.dp
                                      else 96.dp
                              )
                              .background(
                                      color = statusColor,
                                      shape =
                                              RoundedCornerShape(
                                                      topStart = 20.dp,
                                                      bottomStart = 20.dp
                                              )
                              )
      )

      Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        if (!request.thumbnailUrl.isNullOrBlank()) {
          AsyncImage(
                  model = request.thumbnailUrl,
                  contentDescription = null,
                  modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                  contentScale = ContentScale.Crop
          )
          Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
                  text = request.title,
                  style =
                          MaterialTheme.typography.titleSmall.copy(
                                  fontWeight = FontWeight.SemiBold
                          ),
                  maxLines = 2,
                  overflow = TextOverflow.Ellipsis
          )

          StatusPill(status = request.status, color = statusColor)

          if (request.status == DownloadStatus.Downloading ||
                          request.status == DownloadStatus.Paused
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                        text = "${request.progress.toInt()}%",
                        style =
                                MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold
                                ),
                        color = statusColor
                )
                Text(
                        text =
                                if (request.totalBytes > 0)
                                        "${request.downloadedBytes / 1024 / 1024} / ${request.totalBytes / 1024 / 1024} MB"
                                else "Calculating...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }

              if (request.totalBytes > 0) {
                val animatedProgress by
                        animateFloatAsState(
                                targetValue = request.progress / 100f,
                                animationSpec = tween(500),
                                label = "progress_bar"
                        )
                LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f)
                )
              } else {
                LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.2f)
                )
              }
            }
          }
        }

        if (request.status == DownloadStatus.Downloading ||
                        request.status == DownloadStatus.Paused ||
                        request.status == DownloadStatus.Pending
        ) {
          Column(
                  modifier = Modifier.padding(start = 8.dp),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
          ) {
            IconButton(
                    onClick = {
                      if (request.status == DownloadStatus.Paused) {
                        DownloadQueueRepository.resumeDownload(request.id)
                      } else {
                        DownloadQueueRepository.pauseDownload(request.id)
                      }
                    }
            ) {
              Icon(
                      imageVector =
                              if (request.status == DownloadStatus.Paused) Icons.Rounded.PlayArrow
                              else Icons.Rounded.Pause,
                      contentDescription =
                              if (request.status == DownloadStatus.Paused) "Resume" else "Pause",
              )
            }
            IconButton(onClick = { DownloadQueueRepository.removeDownload(request.id) }) {
              Icon(
                      imageVector = Icons.Rounded.Close,
                      contentDescription = "Cancel/Remove",
                      tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun StatusPill(status: DownloadStatus, color: Color) {
  val label =
          when (status) {
            DownloadStatus.Downloading -> "Downloading"
            DownloadStatus.Paused -> "Paused"
            DownloadStatus.Pending -> "Pending"
            DownloadStatus.Success -> "Done"
            DownloadStatus.Failed -> "Failed"
            DownloadStatus.Cancelled -> "Cancelled"
          }

  Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          modifier =
                  Modifier.clip(CircleShape)
                          .background(color.copy(alpha = 0.12f))
                          .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
    )
  }
}
