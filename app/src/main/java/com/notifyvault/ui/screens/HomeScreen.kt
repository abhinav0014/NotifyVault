package com.notifyvault.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifyvault.data.model.AppCategory
import com.notifyvault.data.model.NotificationEntity
import com.notifyvault.ui.FilterMode
import com.notifyvault.ui.MainViewModel
import com.notifyvault.ui.SortMode
import com.notifyvault.ui.components.NotificationCard
import com.notifyvault.ui.components.ReminderDialog
import com.notifyvault.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToStats: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val filterMode by viewModel.filterMode.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sortMode by viewModel.sortMode.collectAsState()

    var showSearch by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var reminderNotification by remember { mutableStateOf<NotificationEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    reminderNotification?.let { notif ->
        ReminderDialog(
            notificationTitle = notif.title.ifBlank { notif.appName },
            existingReminder = if (notif.isReminderSet) notif.reminderTime else null,
            onDismiss = { reminderNotification = null },
            onSetReminder = { time ->
                viewModel.setReminder(notif.id, notif.title.ifBlank { notif.appName }, notif.text, time)
            },
            onCancelReminder = { viewModel.cancelReminder(notif.id) }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Old Notifications") },
            text = { Text("Remove notifications older than 30 days? Starred and reminders are kept.") },
            confirmButton = {
                Button(onClick = { viewModel.clearAll(); showClearDialog = false }) { Text("Clear") }
            },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        if (showSearch) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = viewModel::setSearchQuery,
                                placeholder = { Text("Search notifications...") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        } else {
                            Column {
                                Text(
                                    "NotifyVault",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (unreadCount > 0) {
                                    Text(
                                        "$unreadCount unread · $totalCount total",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch }) {
                            Icon(
                                if (showSearch) Icons.Default.SearchOff else Icons.Default.Search,
                                "Search"
                            )
                        }
                        Box {
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(Icons.Default.Sort, "Sort")
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false }
                            ) {
                                SortMode.values().forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.label()) },
                                        onClick = { viewModel.setSortMode(mode); showSortMenu = false },
                                        leadingIcon = {
                                            if (sortMode == mode) {
                                                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    )
                                }
                            }
                        }
                        IconButton(onClick = onNavigateToStats) {
                            Icon(Icons.Default.BarChart, "Stats")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    filterMode = filterMode,
                    onCategorySelected = viewModel::setCategory,
                    onFilterSelected = viewModel::setFilter
                )
            }
        },
        floatingActionButton = {
            if (unreadCount > 0) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.markAllAsRead() },
                    icon = { Icon(Icons.Default.DoneAll, null) },
                    text = { Text("Mark all read") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            EmptyState(filterMode = filterMode, searchQuery = searchQuery)
        } else {
            // Group into sections without stickyHeader (avoids experimental API)
            val grouped = notifications.groupBy { notif ->
                val diff = System.currentTimeMillis() - notif.timestamp
                when {
                    diff < 86_400_000L    -> "Today"
                    diff < 172_800_000L   -> "Yesterday"
                    diff < 604_800_000L   -> "This Week"
                    else                  -> "Older"
                }
            }
            // Flatten into a single list with separator items
            val flatList = buildList {
                grouped.forEach { (label, items) ->
                    add(SectionHeader(label, items.size))
                    items.forEach { add(it) }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = flatList,
                    key = { item ->
                        when (item) {
                            is SectionHeader     -> "header_${item.label}"
                            is NotificationEntity -> item.id
                            else                 -> item.hashCode()
                        }
                    }
                ) { item ->
                    when (item) {
                        is SectionHeader -> DateGroupHeader(
                            label = item.label,
                            count = item.count
                        )
                        is NotificationEntity -> NotificationCard(
                            notification = item,
                            onClick = { viewModel.markAsRead(item.id) },
                            onStar = { viewModel.toggleStar(item) },
                            onDelete = { viewModel.delete(item) },
                            onReminder = { reminderNotification = item },
                            modifier = Modifier.animateContentSize()
                        )
                    }
                }
            }
        }
    }
}

/** Lightweight wrapper used as a section-header sentinel in the flat list. */
private data class SectionHeader(val label: String, val count: Int)

@Composable
private fun DateGroupHeader(label: String, count: Int) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: AppCategory?,
    filterMode: FilterMode,
    onCategorySelected: (AppCategory?) -> Unit,
    onFilterSelected: (FilterMode) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = 0,
        edgePadding = 16.dp,
        divider = {},
        indicator = {},
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        FilterChipItem(
            label = "All",
            selected = filterMode == FilterMode.ALL && selectedCategory == null,
            icon = Icons.Default.GridView
        ) { onFilterSelected(FilterMode.ALL); onCategorySelected(null) }

        FilterChipItem("Unread", filterMode == FilterMode.UNREAD, Icons.Default.FiberManualRecord) {
            onFilterSelected(FilterMode.UNREAD)
        }
        FilterChipItem("Starred", filterMode == FilterMode.STARRED, Icons.Default.Star) {
            onFilterSelected(FilterMode.STARRED)
        }
        FilterChipItem("Reminders", filterMode == FilterMode.REMINDERS, Icons.Default.Alarm) {
            onFilterSelected(FilterMode.REMINDERS)
        }
        AppCategory.values().forEach { cat ->
            FilterChipItem(
                label = cat.displayName(),
                selected = selectedCategory == cat,
                icon = cat.icon(),
                color = cat.color()
            ) { onCategorySelected(if (selectedCategory == cat) null else cat) }
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        leadingIcon = if (selected) ({ Icon(icon, null, modifier = Modifier.size(14.dp)) }) else null,
        modifier = Modifier.padding(horizontal = 3.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color,
            selectedLeadingIconColor = color
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (selected) color.copy(alpha = 0.5f)
                          else MaterialTheme.colorScheme.outlineVariant,
            selectedBorderColor = color.copy(alpha = 0.5f),
            enabled = true,
            selected = selected
        )
    )
}

@Composable
private fun EmptyState(filterMode: FilterMode, searchQuery: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = if (searchQuery.isNotBlank()) Icons.Default.SearchOff
                              else Icons.Default.NotificationsNone,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (searchQuery.isNotBlank()) "No results for \"$searchQuery\""
                       else when (filterMode) {
                           FilterMode.STARRED   -> "No starred notifications"
                           FilterMode.UNREAD    -> "All caught up! 🎉"
                           FilterMode.REMINDERS -> "No reminders set"
                           else                 -> "No notifications yet"
                       },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Notifications will appear here once received",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

fun SortMode.label(): String = when (this) {
    SortMode.NEWEST   -> "Newest first"
    SortMode.OLDEST   -> "Oldest first"
    SortMode.APP_NAME -> "By app"
    SortMode.PRIORITY -> "By priority"
}
