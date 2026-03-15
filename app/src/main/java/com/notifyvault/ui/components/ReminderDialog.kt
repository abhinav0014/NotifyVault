package com.notifyvault.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.*

@Composable
fun ReminderDialog(
    notificationTitle: String,
    existingReminder: Long?,
    onDismiss: () -> Unit,
    onSetReminder: (Long) -> Unit,
    onCancelReminder: () -> Unit
) {
    var selectedOption by remember { mutableStateOf(0) }
    val options = listOf("In 30 minutes", "In 1 hour", "In 3 hours", "Tonight at 8 PM", "Tomorrow at 9 AM")

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Alarm, null) },
        title = { Text("Set Reminder") },
        text = {
            Column {
                Text(
                    text = "\"${notificationTitle.take(60)}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(Modifier.height(16.dp))
                options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOption = index }
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(selected = selectedOption == index, onClick = { selectedOption = index })
                        Spacer(Modifier.width(8.dp))
                        Text(option, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (existingReminder != null) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onCancelReminder(); onDismiss() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.AlarmOff, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Cancel Existing Reminder")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val now = System.currentTimeMillis()
                val trigger = when (selectedOption) {
                    0 -> now + 30 * 60_000L
                    1 -> now + 60 * 60_000L
                    2 -> now + 3 * 60 * 60_000L
                    3 -> Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                        if (timeInMillis < now) add(Calendar.DAY_OF_YEAR, 1)
                    }.timeInMillis
                    4 -> Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_YEAR, 1)
                        set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
                    }.timeInMillis
                    else -> now + 60 * 60_000L
                }
                onSetReminder(trigger)
                onDismiss()
            }) { Text("Set Reminder") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
