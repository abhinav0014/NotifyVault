package com.notifyvault.data.db

import androidx.room.*
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE category = :category ORDER BY timestamp DESC")
    fun getByCategory(category: AppCategory): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getByPackage(packageName: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarred(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnread(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE isReminderSet = 1 ORDER BY timestamp DESC")
    fun getWithReminders(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE reminderTime IS NOT NULL AND reminderTime <= :time AND isReminderSet = 1")
    suspend fun getDueReminders(time: Long): List<NotificationEntity>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("UPDATE notifications SET isStarred = :starred WHERE id = :id")
    suspend fun setStarred(id: Long, starred: Boolean)

    @Query("UPDATE notifications SET isReminderSet = :set, reminderTime = :time WHERE id = :id")
    suspend fun setReminder(id: Long, set: Boolean, time: Long?)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :query || '%' OR text LIKE '%' || :query || '%' OR appName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<NotificationEntity>>

    @Query("SELECT DISTINCT packageName, appName, category, COUNT(*) as count FROM notifications GROUP BY packageName ORDER BY count DESC")
    fun getAppSummary(): Flow<List<AppSummary>>

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp AND isStarred = 0 AND isReminderSet = 0")
    suspend fun deleteOlderThan(timestamp: Long)

    @Query("SELECT * FROM notifications WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC")
    fun getByDateRange(from: Long, to: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    fun getTotalCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE packageName = :pkg ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestForPackage(pkg: String): NotificationEntity?
}

data class AppSummary(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val count: Int
)
