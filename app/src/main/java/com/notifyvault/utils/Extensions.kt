package com.notifyvault.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationPriority
import java.text.SimpleDateFormat
import java.util.*

fun AppCategory.displayName(): String = when (this) {
    AppCategory.SOCIAL -> "Social"
    AppCategory.MESSAGING -> "Messages"
    AppCategory.EMAIL -> "Email"
    AppCategory.SHOPPING -> "Shopping"
    AppCategory.FINANCE -> "Finance"
    AppCategory.PRODUCTIVITY -> "Work"
    AppCategory.ENTERTAINMENT -> "Media"
    AppCategory.NEWS -> "News"
    AppCategory.HEALTH -> "Health"
    AppCategory.TRAVEL -> "Travel"
    AppCategory.FOOD -> "Food"
    AppCategory.SYSTEM -> "System"
    AppCategory.OTHER -> "Other"
}

fun AppCategory.icon(): ImageVector = when (this) {
    AppCategory.SOCIAL -> Icons.Default.People
    AppCategory.MESSAGING -> Icons.Default.Chat
    AppCategory.EMAIL -> Icons.Default.Email
    AppCategory.SHOPPING -> Icons.Default.ShoppingBag
    AppCategory.FINANCE -> Icons.Default.AccountBalance
    AppCategory.PRODUCTIVITY -> Icons.Default.Work
    AppCategory.ENTERTAINMENT -> Icons.Default.PlayArrow
    AppCategory.NEWS -> Icons.Default.Article
    AppCategory.HEALTH -> Icons.Default.FitnessCenter
    AppCategory.TRAVEL -> Icons.Default.Flight
    AppCategory.FOOD -> Icons.Default.Restaurant
    AppCategory.SYSTEM -> Icons.Default.Settings
    AppCategory.OTHER -> Icons.Default.Notifications
}

fun AppCategory.color(): Color = when (this) {
    AppCategory.SOCIAL -> Color(0xFF3B82F6)
    AppCategory.MESSAGING -> Color(0xFF10B981)
    AppCategory.EMAIL -> Color(0xFF6366F1)
    AppCategory.SHOPPING -> Color(0xFFF97316)
    AppCategory.FINANCE -> Color(0xFFF59E0B)
    AppCategory.PRODUCTIVITY -> Color(0xFF8B5CF6)
    AppCategory.ENTERTAINMENT -> Color(0xFFEC4899)
    AppCategory.NEWS -> Color(0xFF14B8A6)
    AppCategory.HEALTH -> Color(0xFFEF4444)
    AppCategory.TRAVEL -> Color(0xFF06B6D4)
    AppCategory.FOOD -> Color(0xFFD97706)
    AppCategory.SYSTEM -> Color(0xFF6B7280)
    AppCategory.OTHER -> Color(0xFF9CA3AF)
}

fun NotificationPriority.color(): Color = when (this) {
    NotificationPriority.LOW -> Color(0xFF9CA3AF)
    NotificationPriority.NORMAL -> Color(0xFF3B82F6)
    NotificationPriority.HIGH -> Color(0xFFF59E0B)
    NotificationPriority.URGENT -> Color(0xFFEF4444)
}

fun Long.toRelativeTime(): String {
    val diff = System.currentTimeMillis() - this
    return when {
        diff < 60_000L -> "Just now"
        diff < 3_600_000L -> "${diff / 60_000}m ago"
        diff < 86_400_000L -> "${diff / 3_600_000}h ago"
        diff < 604_800_000L -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(this))
    }
}

fun Long.toFullDateTime(): String =
    SimpleDateFormat("MMM d, yyyy  HH:mm", Locale.getDefault()).format(Date(this))
