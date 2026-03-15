package com.notifyvault.ui

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifyvault.data.db.AppSummary
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import com.notifyvault.data.repository.NotificationRepository
import com.notifyvault.service.ReminderReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PrefsKeys {
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val RETENTION_DAYS = intPreferencesKey("retention_days")
    val SHOW_ONGOING = booleanPreferencesKey("show_ongoing")
}

enum class FilterMode { ALL, UNREAD, STARRED, REMINDERS }
enum class SortMode { NEWEST, OLDEST, APP_NAME, PRIORITY }

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val darkTheme: StateFlow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.DARK_THEME] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val retentionDays: StateFlow<Int> = context.dataStore.data
        .map { it[PrefsKeys.RETENTION_DAYS] ?: 30 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 30)

    val showOngoing: StateFlow<Boolean> = context.dataStore.data
        .map { it[PrefsKeys.SHOW_ONGOING] ?: false }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setDarkTheme(enabled: Boolean) = viewModelScope.launch {
        context.dataStore.edit { it[PrefsKeys.DARK_THEME] = enabled }
    }
    fun setRetentionDays(days: Int) = viewModelScope.launch {
        context.dataStore.edit { it[PrefsKeys.RETENTION_DAYS] = days }
    }
    fun setShowOngoing(show: Boolean) = viewModelScope.launch {
        context.dataStore.edit { it[PrefsKeys.SHOW_ONGOING] = show }
    }

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode = _filterMode.asStateFlow()

    private val _selectedCategory = MutableStateFlow<AppCategory?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _sortMode = MutableStateFlow(SortMode.NEWEST)
    val sortMode = _sortMode.asStateFlow()

    private val _selectedPackage = MutableStateFlow<String?>(null)
    val selectedPackage = _selectedPackage.asStateFlow()

    val unreadCount: StateFlow<Int> = repository.getUnreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = repository.getTotalCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val appSummary: StateFlow<List<AppSummary>> = repository.getAppSummary()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = combine(
        _filterMode, _selectedCategory, _searchQuery.debounce(300), _sortMode, _selectedPackage
    ) { filter, category, query, sort, pkg -> Tuple5(filter, category, query, sort, pkg) }
    .flatMapLatest { (filter, category, query, sort, pkg) ->
        val base = when {
            query.isNotBlank() -> repository.search(query)
            pkg != null -> repository.getByPackage(pkg)
            category != null -> repository.getByCategory(category)
            filter == FilterMode.UNREAD -> repository.getUnread()
            filter == FilterMode.STARRED -> repository.getStarred()
            filter == FilterMode.REMINDERS -> repository.getWithReminders()
            else -> repository.getAllNotifications()
        }
        base.map { list ->
            when (sort) {
                SortMode.NEWEST -> list.sortedByDescending { it.timestamp }
                SortMode.OLDEST -> list.sortedBy { it.timestamp }
                SortMode.APP_NAME -> list.sortedBy { it.appName }
                SortMode.PRIORITY -> list.sortedByDescending { it.priority.ordinal }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(mode: FilterMode) { _filterMode.value = mode; _selectedCategory.value = null; _selectedPackage.value = null }
    fun setCategory(category: AppCategory?) { _selectedCategory.value = category; _filterMode.value = FilterMode.ALL; _selectedPackage.value = null }
    fun setPackageFilter(pkg: String?) { _selectedPackage.value = pkg; _selectedCategory.value = null }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortMode(mode: SortMode) { _sortMode.value = mode }
    fun markAsRead(id: Long) = viewModelScope.launch { repository.markAsRead(id) }
    fun markAllAsRead() = viewModelScope.launch { repository.markAllAsRead() }
    fun toggleStar(notification: NotificationEntity) = viewModelScope.launch { repository.setStarred(notification.id, !notification.isStarred) }
    fun delete(notification: NotificationEntity) = viewModelScope.launch {
        if (notification.isReminderSet) ReminderReceiver.cancelReminder(context, notification.id)
        repository.delete(notification)
    }
    fun setReminder(notificationId: Long, title: String, text: String, triggerMillis: Long) = viewModelScope.launch {
        repository.setReminder(notificationId, true, triggerMillis)
        ReminderReceiver.scheduleReminder(context, notificationId, title, text, triggerMillis)
    }
    fun cancelReminder(notificationId: Long) = viewModelScope.launch {
        repository.setReminder(notificationId, false, null)
        ReminderReceiver.cancelReminder(context, notificationId)
    }
    fun clearAll() = viewModelScope.launch {
        val days = retentionDays.value
        if (days == -1) return@launch
        repository.deleteOlderThan(System.currentTimeMillis() - days.toLong() * 86_400_000L)
    }
}

data class Tuple5<A,B,C,D,E>(val a:A,val b:B,val c:C,val d:D,val e:E)
operator fun <A,B,C,D,E> Tuple5<A,B,C,D,E>.component1()=a
operator fun <A,B,C,D,E> Tuple5<A,B,C,D,E>.component2()=b
operator fun <A,B,C,D,E> Tuple5<A,B,C,D,E>.component3()=c
operator fun <A,B,C,D,E> Tuple5<A,B,C,D,E>.component4()=d
operator fun <A,B,C,D,E> Tuple5<A,B,C,D,E>.component5()=e
