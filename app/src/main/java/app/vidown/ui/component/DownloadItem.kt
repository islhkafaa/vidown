package app.vidown.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.vidown.domain.models.DownloadRequest
import app.vidown.domain.models.DownloadStatus
import app.vidown.R
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun DownloadItemCard(
    request: DownloadRequest,
    onPauseResume: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (request.status) {
        DownloadStatus.Success -> MaterialTheme.colorScheme.primary
        DownloadStatus.Failed -> MaterialTheme.colorScheme.error
        DownloadStatus.Downloading -> MaterialTheme.colorScheme.tertiary
        DownloadStatus.Paused -> MaterialTheme.colorScheme.secondary
        DownloadStatus.Pending -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        DownloadStatus.Cancelled -> MaterialTheme.colorScheme.outline
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
        border = BorderStroke(
            1.dp, Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.15f),
                    Color.White.copy(alpha = 0.02f)
                )
            )
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (!request.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(request.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = request.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusPill(status = request.status, color = statusColor)
                    if (request.status == DownloadStatus.Downloading && request.speed != null) {
                        Text(
                            text = request.speed,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (request.status == DownloadStatus.Downloading || request.status == DownloadStatus.Paused) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(
                            progress = { request.progress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = statusColor,
                            trackColor = statusColor.copy(alpha = 0.1f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${request.progress.toInt()}%",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = statusColor
                            )
                            if (request.eta != null) {
                                Text(
                                    text = stringResource(R.string.eta_remaining, request.eta),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (request.status == DownloadStatus.Downloading || request.status == DownloadStatus.Paused || request.status == DownloadStatus.Pending) {
                IconButton(
                    onClick = onPauseResume,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = if (request.status == DownloadStatus.Paused) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                        contentDescription = if (request.status == DownloadStatus.Paused) stringResource(
                            R.string.resume
                        ) else stringResource(R.string.pause),
                        tint = statusColor
                    )
                }
            } else {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.remove),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun StatusPill(status: DownloadStatus, color: Color) {
    val label = when (status) {
        DownloadStatus.Downloading -> stringResource(R.string.status_downloading)
        DownloadStatus.Paused -> stringResource(R.string.status_paused)
        DownloadStatus.Pending -> stringResource(R.string.status_pending)
        DownloadStatus.Success -> stringResource(R.string.status_done)
        DownloadStatus.Failed -> stringResource(R.string.status_failed)
        DownloadStatus.Cancelled -> stringResource(R.string.status_cancelled)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                shape = CircleShape
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = color
        )
    }
}
