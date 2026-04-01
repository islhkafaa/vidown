package app.vidown.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.vidown.domain.models.DownloadRequest
import app.vidown.R
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun DownloadQueueSheetContent(
    queue: List<DownloadRequest>,
    onPauseResume: (DownloadRequest) -> Unit,
    onRemove: (DownloadRequest) -> Unit,
    onReorder: (Int, Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.7f)
            .padding(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.downloads),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 20.dp, top = 8.dp)
        )

        if (queue.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.no_active_downloads),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        } else {
            val lazyListState = rememberLazyListState()
            val state = rememberReorderableLazyListState(lazyListState) { from, to ->
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onReorder(from.index, to.index)
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                items(queue, key = { it.id }) { request ->
                    ReorderableItem(state, key = request.id) { isDragging ->
                        DownloadItemCard(
                            request = request,
                            onPauseResume = { onPauseResume(request) },
                            onRemove = { onRemove(request) },
                            modifier = Modifier.draggableHandle(
                                onDragStarted = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            )
                        )
                    }
                }
            }
        }
    }
}
