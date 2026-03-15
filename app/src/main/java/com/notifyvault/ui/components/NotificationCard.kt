package com.notifyvault.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.notifyvault.data.model.NotificationEntity
import com.notifyvault.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: NotificationEntity,
    onClick: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit,
    onReminder: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val appIcon = remember(notification.packageName) {
        try {
            context.packageManager.getApplicationIcon(notification.packageName).toBitmap().asImageBitmap()
        } catch (e: Exception) { null }
    }

    val priorityColor = notification.priority.color()
    val categoryColor = notification.category.color()
    val cardAlpha = if (notification.isRead) 0.72f else 1f

    Card(
        onClick = { onClick(); expanded = !expanded },
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!notification.isRead) 4.dp else 1.dp),
        border = if (!notification.isRead) BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer) else null
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Priority bar
                Box(
                    modifier = Modifier.width(3.dp).height(42.dp)
                        .clip(RoundedCornerShape(2.dp)).background(priorityColor)
                )
                Spacer(Modifier.width(10.dp))
                // App icon
                Box(
                    modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (appIcon != null) {
                        Image(bitmap = appIcon, contentDescription = notification.appName, modifier = Modifier.size(32.dp))
                    } else {
                        Icon(notification.category.icon(), null, tint = categoryColor, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.appName,
                            style = MaterialTheme.typography.labelMedium,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (notification.isReminderSet) {
                                Icon(Icons.Default.Alarm, "Reminder", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                            }
                            if (!notification.isRead) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            }
                            Text(
                                text = notification.timestamp.toRelativeTime(),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (notification.title.isNotBlank()) {
                        Text(
                            text = notification.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (!notification.isRead) FontWeight.Bold else FontWeight.Normal,
                            maxLines = if (expanded) Int.MAX_VALUE else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = notification.text,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (expanded) Int.MAX_VALUE else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    if (!notification.bigText.isNullOrBlank() && notification.bigText != notification.text) {
                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(8.dp))
                        Text(notification.bigText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(
                            onClick = {},
                            label = { Text(notification.category.displayName(), style = MaterialTheme.typography.labelSmall) },
                            leadingIcon = { Icon(notification.category.icon(), null, modifier = Modifier.size(14.dp)) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = categoryColor.copy(alpha = 0.15f),
                                labelColor = categoryColor,
                                leadingIconContentColor = categoryColor
                            ),
                            border = null
                        )
                        Text(
                            text = notification.timestamp.toFullDateTime(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        IconButton(onClick = onReminder, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (notification.isReminderSet) Icons.Default.AlarmOff else Icons.Default.AddAlarm,
                                "Reminder",
                                tint = if (notification.isReminderSet) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onStar, modifier = Modifier.size(36.dp)) {
                            Icon(
                                if (notification.isStarred) Icons.Default.Star else Icons.Default.StarOutline,
                                "Star",
                                tint = if (notification.isStarred) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
