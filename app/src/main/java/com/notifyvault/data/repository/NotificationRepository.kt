package com.notifyvault.data.repository

import com.notifyvault.data.db.AppSummary
import com.notifyvault.data.db.NotificationDao
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val dao: NotificationDao
) {
    fun getAllNotifications(): Flow<List<NotificationEntity>> = dao.getAllNotifications()
    fun getByCategory(category: AppCategory): Flow<List<NotificationEntity>> = dao.getByCategory(category)
    fun getByPackage(packageName: String): Flow<List<NotificationEntity>> = dao.getByPackage(packageName)
    fun getStarred(): Flow<List<NotificationEntity>> = dao.getStarred()
    fun getUnread(): Flow<List<NotificationEntity>> = dao.getUnread()
    fun getWithReminders(): Flow<List<NotificationEntity>> = dao.getWithReminders()
    fun getUnreadCount(): Flow<Int> = dao.getUnreadCount()
    fun search(query: String): Flow<List<NotificationEntity>> = dao.search(query)
    fun getAppSummary(): Flow<List<AppSummary>> = dao.getAppSummary()
    fun getTotalCount(): Flow<Int> = dao.getTotalCount()
    fun getByDateRange(from: Long, to: Long): Flow<List<NotificationEntity>> = dao.getByDateRange(from, to)

    suspend fun insert(notification: NotificationEntity): Long = dao.insert(notification)
    suspend fun update(notification: NotificationEntity) = dao.update(notification)
    suspend fun delete(notification: NotificationEntity) = dao.delete(notification)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun markAsRead(id: Long) = dao.markAsRead(id)
    suspend fun markAllAsRead() = dao.markAllAsRead()
    suspend fun setStarred(id: Long, starred: Boolean) = dao.setStarred(id, starred)
    suspend fun setReminder(id: Long, set: Boolean, time: Long?) = dao.setReminder(id, set, time)
    suspend fun getDueReminders(time: Long): List<NotificationEntity> = dao.getDueReminders(time)
    suspend fun deleteOlderThan(timestamp: Long) = dao.deleteOlderThan(timestamp)
}
