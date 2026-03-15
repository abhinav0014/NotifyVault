package com.notifyvault.di

import android.content.Context
import androidx.room.Room
import com.notifyvault.data.db.NotificationDao
import com.notifyvault.data.db.NotifyVaultDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotifyVaultDatabase {
        return Room.databaseBuilder(
            context,
            NotifyVaultDatabase::class.java,
            NotifyVaultDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideDao(db: NotifyVaultDatabase): NotificationDao = db.notificationDao()
}
