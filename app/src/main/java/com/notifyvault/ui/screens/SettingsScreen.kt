package com.notifyvault.ui.screens


import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifyvault.ui.MainViewModel
import com.notifyvault.utils.NotificationPermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val darkTheme by viewModel.darkTheme.collectAsState()
    val retentionDays by viewModel.retentionDays.collectAsState()
    val showOngoing by viewModel.showOngoing.collectAsState()
    val isListenerEnabled = NotificationPermissionHelper.isNotificationListenerEnabled(context)

    var showClearConfirm by remember { mutableStateOf(false) }
    var showRetentionDialog by remember { mutableStateOf(false) }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Clear Old Notifications") },
            text = { Text("Delete all notifications older than $retentionDays days? Starred and reminders are preserved.") },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAll(); showClearConfirm = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showClearConfirm = false }) { Text("Cancel") } }
        )
    }

    if (showRetentionDialog) {
        val options = listOf(7, 14, 30, 60, 90, 365, -1)
        AlertDialog(
            onDismissRequest = { showRetentionDialog = false },
            title = { Text("Keep notifications for") },
            text = {
                Column {
                    options.forEach { days ->
                        val label = if (days == -1) "Forever" else "$days days"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setRetentionDays(days); showRetentionDialog = false }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(selected = retentionDays == days, onClick = {
                                viewModel.setRetentionDays(days); showRetentionDialog = false
                            })
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showRetentionDialog = false }) { Text("Close") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = 40.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Permission status
            item {
                SettingsSectionHeader("Permissions")
                SettingsCard {
                    PermissionRow(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notification Access",
                        subtitle = if (isListenerEnabled) "Active — capturing notifications" else "Not granted — tap to fix",
                        statusColor = if (isListenerEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.error,
                        onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsClickRow(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Disable battery optimization",
                        subtitle = if (NotificationPermissionHelper.isIgnoringBatteryOptimizations(context)) "Already excluded from battery optimization" else "Open battery optimization settings",
                        onClick = { context.startActivity(NotificationPermissionHelper.getRequestIgnoreBatteryOptimizationsIntent(context)) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsClickRow(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Enable auto-start",
                        subtitle = "Allow NotifyVault to restart after reboot",
                        onClick = { context.startActivity(NotificationPermissionHelper.getAutoStartIntent(context)) }
                    )
                }
            }

            // Appearance
            item {
                SettingsSectionHeader("Appearance")
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark colour scheme",
                        checked = darkTheme,
                        onCheckedChange = viewModel::setDarkTheme
                    )
                }
            }

            // Notifications
            item {
                SettingsSectionHeader("Notifications")
                SettingsCard {
                    SettingsToggleRow(
                        icon = Icons.Default.NotificationsPaused,
                        title = "Show Ongoing",
                        subtitle = "Include persistent/foreground notifications",
                        checked = showOngoing,
                        onCheckedChange = viewModel::setShowOngoing
                    )
                }
            }

            // Data
            item {
                SettingsSectionHeader("Data")
                SettingsCard {
                    SettingsClickRow(
                        icon = Icons.Default.DateRange,
                        title = "Retention Period",
                        subtitle = if (retentionDays == -1) "Keep forever" else "Keep for $retentionDays days",
                        onClick = { showRetentionDialog = true }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsClickRow(
                        icon = Icons.Default.DeleteSweep,
                        title = "Clear Old Notifications",
                        subtitle = "Remove notifications older than $retentionDays days",
                        onClick = { showClearConfirm = true },
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // About
            item {
                SettingsSectionHeader("About")
                SettingsCard {
                    SettingsInfoRow(
                        icon = Icons.Default.Info,
                        title = "Version",
                        value = "1.0.0"
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingsInfoRow(
                        icon = Icons.Default.Shield,
                        title = "Privacy",
                        value = "All data stays on device"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp, top = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    statusColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = statusColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = statusColor)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsClickRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, color = tint)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsInfoRow(icon: ImageVector, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
