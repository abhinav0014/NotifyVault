package com.notifyvault.service

import android.app.Notification
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.google.gson.Gson
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import com.notifyvault.data.model.NotificationPriority
import com.notifyvault.data.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {

    @Inject
    lateinit var repository: NotificationRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    // Known package -> category mappings
    private val categoryMap = mapOf(
        // Social
        "com.facebook.katana" to AppCategory.SOCIAL,
        "com.instagram.android" to AppCategory.SOCIAL,
        "com.twitter.android" to AppCategory.SOCIAL,
        "com.snapchat.android" to AppCategory.SOCIAL,
        "com.linkedin.android" to AppCategory.SOCIAL,
        "com.pinterest" to AppCategory.SOCIAL,
        "com.reddit.frontpage" to AppCategory.SOCIAL,
        "com.zhiliaoapp.musically" to AppCategory.SOCIAL,
        // Messaging
        "com.whatsapp" to AppCategory.MESSAGING,
        "com.facebook.orca" to AppCategory.MESSAGING,
        "org.telegram.messenger" to AppCategory.MESSAGING,
        "com.viber.voip" to AppCategory.MESSAGING,
        "com.discord" to AppCategory.MESSAGING,
        "com.google.android.apps.messaging" to AppCategory.MESSAGING,
        "com.samsung.android.messaging" to AppCategory.MESSAGING,
        "com.microsoft.teams" to AppCategory.MESSAGING,
        "com.slack" to AppCategory.MESSAGING,
        // Email
        "com.google.android.gm" to AppCategory.EMAIL,
        "com.microsoft.office.outlook" to AppCategory.EMAIL,
        "com.yahoo.mobile.client.android.mail" to AppCategory.EMAIL,
        "me.proton.android.mail" to AppCategory.EMAIL,
        // Shopping
        "com.amazon.mShop.android.shopping" to AppCategory.SHOPPING,
        "com.flipkart.android" to AppCategory.SHOPPING,
        "com.ebay.mobile" to AppCategory.SHOPPING,
        "com.myntra.android" to AppCategory.SHOPPING,
        // Finance
        "com.google.android.apps.nbu.paisa.user" to AppCategory.FINANCE,
        "net.one97.paytm" to AppCategory.FINANCE,
        "com.phonepe.app" to AppCategory.FINANCE,
        "com.bankofamerica.examobile" to AppCategory.FINANCE,
        "com.chase.sig.android" to AppCategory.FINANCE,
        // Entertainment
        "com.netflix.mediaclient" to AppCategory.ENTERTAINMENT,
        "com.spotify.music" to AppCategory.ENTERTAINMENT,
        "com.google.android.youtube" to AppCategory.ENTERTAINMENT,
        "com.amazon.avod.thirdpartyclient" to AppCategory.ENTERTAINMENT,
        "com.hotstar.android" to AppCategory.ENTERTAINMENT,
        // News
        "com.google.android.apps.searchlite" to AppCategory.NEWS,
        "com.flipboard.app" to AppCategory.NEWS,
        "in.dailyhunt" to AppCategory.NEWS,
        "com.bbc.mobile.news.ww" to AppCategory.NEWS,
        // Health
        "com.google.android.apps.fitness" to AppCategory.HEALTH,
        "com.samsung.android.shealth" to AppCategory.HEALTH,
        "com.myfitnesspal.android" to AppCategory.HEALTH,
        // Travel
        "com.makemytrip" to AppCategory.TRAVEL,
        "com.goibibo" to AppCategory.TRAVEL,
        "com.uber.client" to AppCategory.TRAVEL,
        "com.olacabs.customer" to AppCategory.TRAVEL,
        "com.rapido.passenger" to AppCategory.TRAVEL,
        // Food
        "in.swiggy.android" to AppCategory.FOOD,
        "com.application.zomato" to AppCategory.FOOD,
        "com.ubercab.eats" to AppCategory.FOOD,
        // System
        "com.android.systemui" to AppCategory.SYSTEM,
        "com.google.android.gms" to AppCategory.SYSTEM,
        "com.android.vending" to AppCategory.SYSTEM,
    )

    // Ignored packages (noisy system apps)
    private val ignoredPackages = setOf(
        "android",
        "com.android.launcher3",
        "com.android.launcher",
        "com.android.settings"
    )

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        if (sbn.packageName in ignoredPackages) return
        if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0 &&
            sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE != 0) return

        val notification = sbn.notification
        val extras: Bundle = notification.extras

        val title = extras.getString(Notification.EXTRA_TITLE)
            ?: extras.getString(Notification.EXTRA_CONVERSATION_TITLE)
            ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()

        if (title.isBlank() && text.isBlank()) return

        val appName = getAppName(sbn.packageName)
        val category = categorizeApp(sbn.packageName, notification.category)
        val priority = getPriority(notification.priority)

        // Extract action titles
        val actions = notification.actions?.map { it.title?.toString() ?: "" }
        val actionsJson = if (!actions.isNullOrEmpty()) gson.toJson(actions) else null

        val entity = NotificationEntity(
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            subText = subText,
            bigText = bigText,
            timestamp = sbn.postTime,
            category = category,
            priority = priority,
            groupKey = sbn.groupKey,
            notificationId = sbn.id,
            tag = sbn.tag,
            color = if (notification.color != 0) notification.color else null,
            actions = actionsJson,
            isOngoing = sbn.isOngoing,
            channelId = notification.channelId,
            sortKey = notification.sortKey,
            when_ = notification.`when`
        )

        serviceScope.launch {
            repository.insert(entity)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // Notification is removed from status bar but we keep it in DB — that's the whole point!
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(info).toString()
        } catch (e: Exception) {
            packageName.substringAfterLast(".")
                .replaceFirstChar { it.uppercase() }
        }
    }

    private fun categorizeApp(packageName: String, notifCategory: String?): AppCategory {
        categoryMap[packageName]?.let { return it }

        // Heuristic from notification category
        return when (notifCategory) {
            Notification.CATEGORY_MESSAGE -> AppCategory.MESSAGING
            Notification.CATEGORY_EMAIL -> AppCategory.EMAIL
            Notification.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            Notification.CATEGORY_PROMO -> AppCategory.SHOPPING
            Notification.CATEGORY_REMINDER,
            Notification.CATEGORY_EVENT -> AppCategory.PRODUCTIVITY
            Notification.CATEGORY_TRANSPORT -> AppCategory.TRAVEL
            Notification.CATEGORY_SYSTEM,
            Notification.CATEGORY_SERVICE -> AppCategory.SYSTEM
            Notification.CATEGORY_RECOMMENDATION -> AppCategory.NEWS
            else -> guessFromPackageName(packageName)
        }
    }

    private fun guessFromPackageName(pkg: String): AppCategory {
        return when {
            pkg.contains("mail") || pkg.contains("email") -> AppCategory.EMAIL
            pkg.contains("chat") || pkg.contains("message") || pkg.contains("sms") -> AppCategory.MESSAGING
            pkg.contains("social") || pkg.contains("friend") -> AppCategory.SOCIAL
            pkg.contains("shop") || pkg.contains("store") || pkg.contains("buy") -> AppCategory.SHOPPING
            pkg.contains("bank") || pkg.contains("pay") || pkg.contains("finance") || pkg.contains("money") -> AppCategory.FINANCE
            pkg.contains("music") || pkg.contains("video") || pkg.contains("stream") || pkg.contains("play") -> AppCategory.ENTERTAINMENT
            pkg.contains("news") || pkg.contains("feed") -> AppCategory.NEWS
            pkg.contains("health") || pkg.contains("fit") || pkg.contains("workout") -> AppCategory.HEALTH
            pkg.contains("food") || pkg.contains("eat") || pkg.contains("delivery") -> AppCategory.FOOD
            pkg.contains("travel") || pkg.contains("flight") || pkg.contains("hotel") || pkg.contains("ride") -> AppCategory.TRAVEL
            else -> AppCategory.OTHER
        }
    }

    private fun getPriority(priority: Int): NotificationPriority {
        return when (priority) {
            Notification.PRIORITY_MAX, Notification.PRIORITY_HIGH -> NotificationPriority.HIGH
            Notification.PRIORITY_LOW -> NotificationPriority.LOW
            Notification.PRIORITY_MIN -> NotificationPriority.LOW
            else -> NotificationPriority.NORMAL
        }
    }
}
