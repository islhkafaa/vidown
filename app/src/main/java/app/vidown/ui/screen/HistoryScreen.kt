package app.vidown.ui.screen

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.vidown.data.local.HistoryEntity
import app.vidown.domain.models.DownloadStatus
import app.vidown.ui.viewmodel.HistoryViewModel
import coil.compose.AsyncImage
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
        modifier: Modifier = Modifier,
        viewModel: HistoryViewModel = viewModel(),
        onPlayEvent: (String) -> Unit = {}
) {
  val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
  val historyList by viewModel.historyState.collectAsState()
  val context = LocalContext.current
  var pendingDelete by remember { mutableStateOf<HistoryEntity?>(null) }

  if (pendingDelete != null) {
    AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove from history") },
            text = { Text("This will remove the record but not delete the file.") },
            confirmButton = {
              TextButton(
                      onClick = {
                        viewModel.deleteRecord(pendingDelete!!, context)
                        pendingDelete = null
                      }
              ) { Text("Remove") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } }
    )
  }

  Scaffold(
          modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
          topBar = {
            val searchQuery by viewModel.searchQuery.collectAsState()
            Column {
              LargeTopAppBar(
                      title = {
                        Text(
                                text = "History",
                                style =
                                        MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold
                                        ),
                        )
                      },
                      actions = {
                        if (historyList.isNotEmpty() || searchQuery.isNotEmpty()) {
                          IconButton(onClick = { viewModel.clearHistory() }) {
                            Icon(Icons.Rounded.Delete, contentDescription = "Clear History")
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
              OutlinedTextField(
                      value = searchQuery,
                      onValueChange = { viewModel.updateSearchQuery(it) },
                      modifier =
                              Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                      placeholder = { Text("Search your downloads...") },
                      leadingIcon = { Icon(Icons.Default.Search, null) },
                      trailingIcon =
                              if (searchQuery.isNotEmpty()) {
                                {
                                  IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Rounded.Close, null)
                                  }
                                }
                              } else null,
                      singleLine = true,
                      shape = RoundedCornerShape(12.dp),
                      colors =
                              OutlinedTextFieldDefaults.colors(
                                      focusedBorderColor =
                                              MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                      unfocusedBorderColor =
                                              MaterialTheme.colorScheme.outlineVariant
                              )
              )
            }
          }
  ) { innerPadding ->
    if (historyList.isEmpty()) {
      Box(
              modifier = Modifier.fillMaxSize().padding(innerPadding),
              contentAlignment = Alignment.Center,
      ) {
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
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Text(
                  text = "No downloads yet",
                  style =
                          MaterialTheme.typography.titleLarge.copy(
                                  fontWeight = FontWeight.SemiBold
                          ),
                  color = MaterialTheme.colorScheme.onSurface
          )
          Text(
                  text = "Completed downloads will appear here",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  textAlign = TextAlign.Center
          )
        }
      }
    } else {
      LazyVerticalStaggeredGrid(
              columns = StaggeredGridCells.Fixed(2),
              modifier = Modifier.fillMaxSize().padding(innerPadding),
              contentPadding = PaddingValues(12.dp),
              verticalItemSpacing = 8.dp,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(historyList, key = { it.id }) { record ->
          GalleryHistoryItem(
                  record = record,
                  onTap = {
                    if (record.status == DownloadStatus.Success && record.fileUri != null) {
                      val encodedUri = URLEncoder.encode(record.fileUri, "UTF-8")
                      onPlayEvent(encodedUri)
                    } else if (record.status == DownloadStatus.Failed) {
                      Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    } else {
                      Toast.makeText(context, "File unavailable", Toast.LENGTH_SHORT).show()
                    }
                  },
                  onLongPress = { pendingDelete = record },
                  onRetry = {
                    val request =
                            app.vidown.domain.models.DownloadRequest(
                                    url = record.url,
                                    title = record.title,
                                    thumbnailUrl = record.thumbnailUrl,
                                    formatId = record.formatId,
                                    totalBytes = record.totalBytes
                            )
                    app.vidown.data.repository.DownloadQueueRepository.enqueueDownload(
                            context,
                            request
                    )
                    Toast.makeText(context, "Retrying download...", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier
          )
        }
      }
    }
  }
}

@Composable
fun GalleryHistoryItem(
        record: HistoryEntity,
        onTap: () -> Unit,
        onLongPress: () -> Unit,
        onRetry: () -> Unit,
        modifier: Modifier = Modifier
) {
  val isFailed = record.status == DownloadStatus.Failed
  val isAudio =
          record.formatId.contains("audio") ||
                  record.formatId.contains("m4a") ||
                  record.formatId.contains("mp3")

  var pressed by remember { mutableStateOf(false) }
  val scale by
          animateFloatAsState(
                  targetValue = if (pressed) 0.95f else 1f,
                  animationSpec = tween(100),
                  label = "gallery_press"
          )
  val overlayAlpha by
          animateColorAsState(
                  targetValue = if (pressed) Color.White.copy(alpha = 0.08f) else Color.Transparent,
                  animationSpec = tween(100),
                  label = "gallery_overlay"
          )

  Box(
          modifier =
                  modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).scale(scale).pointerInput(
                                  onTap,
                                  onLongPress
                          ) {
                    detectTapGestures(
                            onPress = {
                              pressed = true
                              try {
                                awaitRelease()
                              } finally {
                                pressed = false
                              }
                            },
                            onTap = { onTap() },
                            onLongPress = { onLongPress() }
                    )
                  }
  ) {
    if (!record.thumbnailUrl.isNullOrBlank() && !isAudio) {
      AsyncImage(
              model = record.thumbnailUrl,
              contentDescription = null,
              modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f),
              contentScale = ContentScale.Crop
      )
    } else {
      Box(
              modifier =
                      Modifier.fillMaxWidth()
                              .aspectRatio(1f)
                              .background(MaterialTheme.colorScheme.surfaceVariant),
              contentAlignment = Alignment.Center
      ) {
        Icon(
                imageVector = if (isAudio) Icons.Rounded.MusicNote else Icons.Rounded.BrokenImage,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
      }
    }

    Box(modifier = Modifier.matchParentSize().background(overlayAlpha))

    Box(
            modifier =
                    Modifier.matchParentSize()
                            .background(
                                    Brush.verticalGradient(
                                            colors =
                                                    listOf(
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.72f)
                                                    ),
                                            startY = 40f
                                    )
                            )
    )

    Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
      Text(
              text = record.title,
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = Color.White,
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
      )
    }

    if (isFailed) {
      Row(
              modifier =
                      Modifier.align(Alignment.TopEnd)
                              .padding(6.dp)
                              .clip(RoundedCornerShape(8.dp))
                              .background(MaterialTheme.colorScheme.errorContainer)
                              .clickable { onRetry() }
                              .padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Retry",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
                text = "Retry",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer
        )
      }
    }
  }
}
