package com.notifyvault.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifyvault.data.model.AppCategory
import com.notifyvault.ui.MainViewModel
import com.notifyvault.utils.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val notifications by viewModel.notifications.collectAsState()
    val appSummary by viewModel.appSummary.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    val categoryBreakdown = notifications
        .groupBy { it.category }
        .map { (cat, items) -> cat to items.size }
        .sortedByDescending { it.second }

    val starredCount = notifications.count { it.isStarred }
    val reminderCount = notifications.count { it.isReminderSet }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 32.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard(Modifier.weight(1f), "Total", totalCount.toString(), Icons.Default.Inbox, MaterialTheme.colorScheme.primary)
                    StatCard(Modifier.weight(1f), "Unread", unreadCount.toString(), Icons.Default.FiberManualRecord, Color(0xFF3B82F6))
                    StatCard(Modifier.weight(1f), "Starred", starredCount.toString(), Icons.Default.Star, Color(0xFFF59E0B))
                    StatCard(Modifier.weight(1f), "Alerts", reminderCount.toString(), Icons.Default.Alarm, MaterialTheme.colorScheme.tertiary)
                }
            }

            if (categoryBreakdown.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("By Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    CategoryDonutCard(breakdown = categoryBreakdown, total = totalCount)
                }
                items(categoryBreakdown) { (cat, count) ->
                    CategoryBarRow(category = cat, count = count, total = totalCount)
                }
            }

            if (appSummary.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Top Apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                }
                items(appSummary.take(10)) { summary ->
                    AppStatRow(
                        appName = summary.appName,
                        category = summary.category,
                        count = summary.count,
                        maxCount = appSummary.firstOrNull()?.count ?: 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CategoryDonutCard(breakdown: List<Pair<AppCategory, Int>>, total: Int) {
    if (total == 0) return
    val animProgress by animateFloatAsState(targetValue = 1f, animationSpec = tween(1200, easing = EaseOutCubic), label = "donut")
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round)
                    var startAngle = -90f
                    breakdown.forEach { (cat, count) ->
                        val sweep = (count.toFloat() / total) * 360f * animProgress
                        drawArc(color = cat.color(), startAngle = startAngle, sweepAngle = (sweep - 2f).coerceAtLeast(0f), useCenter = false, style = stroke)
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(total.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                breakdown.take(6).forEach { (cat, count) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(cat.color(), CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(cat.displayName(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                        Text(count.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBarRow(category: AppCategory, count: Int, total: Int) {
    val animFraction by animateFloatAsState(
        targetValue = if (total > 0) count.toFloat() / total else 0f,
        animationSpec = tween(800, easing = EaseOutCubic), label = "bar"
    )
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(category.icon(), null, tint = category.color(), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(category.displayName(), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(90.dp))
        Box(modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animFraction).clip(RoundedCornerShape(4.dp))
                .background(Brush.horizontalGradient(listOf(category.color(), category.color().copy(alpha = 0.6f)))))
        }
        Spacer(Modifier.width(10.dp))
        Text(count.toString(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = category.color(), modifier = Modifier.width(32.dp))
    }
}

@Composable
private fun AppStatRow(appName: String, category: AppCategory, count: Int, maxCount: Int) {
    val animFraction by animateFloatAsState(
        targetValue = if (maxCount > 0) count.toFloat() / maxCount else 0f,
        animationSpec = tween(800, easing = EaseOutCubic), label = "appBar"
    )
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).background(category.color().copy(alpha = 0.15f), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                        Icon(category.icon(), null, tint = category.color(), modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(category.displayName(), style = MaterialTheme.typography.labelSmall, color = category.color())
                    }
                }
                Text("$count", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(animFraction).clip(RoundedCornerShape(2.dp)).background(category.color()))
            }
        }
    }
}
