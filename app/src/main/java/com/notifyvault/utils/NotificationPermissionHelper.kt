package com.notifyvault.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat

object NotificationPermissionHelper {
    fun isNotificationListenerEnabled(context: Context): Boolean {
        val enabledListeners = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledListeners.contains(context.packageName)
    }
}
