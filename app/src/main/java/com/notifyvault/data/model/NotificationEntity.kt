package com.notifyvault.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AppCategory {
    SOCIAL,
    MESSAGING,
    EMAIL,
    SHOPPING,
    FINANCE,
    PRODUCTIVITY,
    ENTERTAINMENT,
    NEWS,
    HEALTH,
    TRAVEL,
    FOOD,
    SYSTEM,
    OTHER
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, URGENT
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val subText: String? = null,
    val bigText: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val category: AppCategory = AppCategory.OTHER,
    val priority: NotificationPriority = NotificationPriority.NORMAL,
    val isRead: Boolean = false,
    val isStarred: Boolean = false,
    val isReminderSet: Boolean = false,
    val reminderTime: Long? = null,
    val groupKey: String? = null,
    val notificationId: Int = 0,
    val tag: String? = null,
    val color: Int? = null,
    val actions: String? = null, // JSON array of action titles
    val isOngoing: Boolean = false,
    val channelId: String? = null,
    val sortKey: String? = null,
    val when_: Long? = null
)
