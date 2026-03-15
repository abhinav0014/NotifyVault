package com.notifyvault.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import com.notifyvault.data.model.NotificationPriority

class Converters {
    @TypeConverter
    fun fromAppCategory(value: AppCategory): String = value.name

    @TypeConverter
    fun toAppCategory(value: String): AppCategory =
        try { AppCategory.valueOf(value) } catch (e: Exception) { AppCategory.OTHER }

    @TypeConverter
    fun fromPriority(value: NotificationPriority): String = value.name

    @TypeConverter
    fun toPriority(value: String): NotificationPriority =
        try { NotificationPriority.valueOf(value) } catch (e: Exception) { NotificationPriority.NORMAL }
}

@Database(
    entities = [NotificationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NotifyVaultDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao

    companion object {
        const val DATABASE_NAME = "notifyvault_db"
    }
}
