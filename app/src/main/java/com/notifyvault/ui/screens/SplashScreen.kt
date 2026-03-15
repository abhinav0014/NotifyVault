package com.notifyvault.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifyvault.ui.theme.VaultCyan
import com.notifyvault.ui.theme.VaultPurple
import com.notifyvault.ui.theme.VaultPurpleLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var phase by remember { mutableStateOf(0) }

    val logoScale by animateFloatAsState(
        targetValue = when (phase) { 0 -> 0.3f; 4 -> 1.15f; else -> 1f },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val logoAlpha by animateFloatAsState(targetValue = if (phase >= 1) 1f else 0f, animationSpec = tween(600), label = "logoAlpha")
    val textAlpha by animateFloatAsState(targetValue = if (phase >= 2) 1f else 0f, animationSpec = tween(500), label = "textAlpha")
    val taglineAlpha by animateFloatAsState(targetValue = if (phase >= 3) 1f else 0f, animationSpec = tween(500), label = "taglineAlpha")
    val screenAlpha by animateFloatAsState(
        targetValue = if (phase == 4) 0f else 1f,
        animationSpec = tween(400),
        finishedListener = { if (phase == 4) onFinished() },
        label = "screenAlpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "inf")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.28f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulse"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1200, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulseAlpha"
    )
    val shimmer by infiniteTransition.animateFloat(
        initialValue = -1f, targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing)),
        label = "shimmer"
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "rotation"
    )

    LaunchedEffect(Unit) {
        delay(80);  phase = 1
        delay(420); phase = 2
        delay(380); phase = 3
        delay(1800); phase = 4
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(screenAlpha)
            .background(Brush.radialGradient(listOf(Color(0xFF1A0A2E), Color(0xFF0F0A1A)), radius = 1200f)),
        contentAlignment = Alignment.Center
    ) {
        // Ambient blobs
        Box(Modifier.offset((-80).dp, (-120).dp).size(280.dp).blur(80.dp).background(VaultPurple.copy(alpha = 0.22f), CircleShape))
        Box(Modifier.offset(100.dp, 140.dp).size(220.dp).blur(70.dp).background(VaultCyan.copy(alpha = 0.13f), CircleShape))

        // Rotating halo
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale)
                .alpha(pulseAlpha * logoAlpha)
                .graphicsLayer { rotationZ = rotation }
                .background(
                    Brush.sweepGradient(listOf(VaultPurple.copy(0f), VaultPurpleLight.copy(0.7f), VaultCyan.copy(0.3f), VaultPurple.copy(0f))),
                    CircleShape
                )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            // Logo
            Box(modifier = Modifier.scale(logoScale).alpha(logoAlpha), contentAlignment = Alignment.Center) {
                // Outer ring
                Box(
                    modifier = Modifier.size(130.dp).background(
                        Brush.linearGradient(listOf(VaultPurple.copy(0.3f), VaultCyan.copy(0.15f))), CircleShape
                    )
                )
                // Logo card
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF2D1F4A), Color(0xFF1A0A2E)), Offset(0f, 0f), Offset(200f, 200f)),
                            RoundedCornerShape(28.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Shimmer sweep
                    Box(
                        modifier = Modifier.size(96.dp).clip(RoundedCornerShape(28.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color.Transparent, Color.White.copy(0.08f), Color.Transparent),
                                    Offset(shimmer * 200f - 100f, 0f), Offset(shimmer * 200f + 100f, 200f)
                                )
                            )
                    )
                    Icon(Icons.Default.Shield, "NotifyVault", tint = VaultPurpleLight, modifier = Modifier.size(52.dp))
                    // Cyan badge
                    Box(
                        modifier = Modifier.align(Alignment.TopEnd).offset((-14).dp, 14.dp)
                            .size(20.dp).background(VaultCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("N", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(36.dp))

            Box(modifier = Modifier.alpha(textAlpha)) {
                Text("NotifyVault", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.ExtraBold, color = Color.White, letterSpacing = (-1).sp)
            }

            Spacer(Modifier.height(10.dp))

            Box(modifier = Modifier.alpha(taglineAlpha)) {
                Text(
                    "Every notification, forever",
                    style = MaterialTheme.typography.bodyMedium,
                    color = VaultPurpleLight.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(80.dp))

            if (phase == 3) { LoadingDots() }
        }
    }
}

@Composable
private fun LoadingDots() {
    val infinite = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (0..2).forEach { i ->
            val a by infinite.animateFloat(
                initialValue = 0.2f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse, StartOffset(i * 200)),
                label = "dot$i"
            )
            Box(Modifier.size(6.dp).alpha(a).background(VaultPurpleLight, CircleShape))
        }
    }
}
