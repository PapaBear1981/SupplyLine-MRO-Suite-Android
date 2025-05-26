package com.example.supplyline_mro_suite.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supplyline_mro_suite.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun PulsingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_card")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier.scale(scale),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    if (isLoading) {
        val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
        val shimmerTranslateAnim by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )
        
        Box(
            modifier = modifier
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.LightGray.copy(alpha = 0.3f),
                            Color.LightGray.copy(alpha = 0.5f),
                            Color.LightGray.copy(alpha = 0.3f)
                        ),
                        startX = shimmerTranslateAnim - 300f,
                        endX = shimmerTranslateAnim
                    )
                )
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
fun AnimatedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = AerospacePrimary,
    backgroundColor: Color = Color.LightGray.copy(alpha = 0.3f),
    strokeWidth: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "progress_animation"
    )
    
    Canvas(
        modifier = modifier.size(120.dp)
    ) {
        val strokeWidthPx = strokeWidth.toPx()
        val radius = (size.minDimension - strokeWidthPx) / 2
        val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
        
        // Background circle
        drawCircle(
            color = backgroundColor,
            radius = radius,
            center = center,
            style = Stroke(strokeWidthPx, cap = StrokeCap.Round)
        )
        
        // Progress arc
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * animatedProgress,
            useCenter = false,
            style = Stroke(strokeWidthPx, cap = StrokeCap.Round),
            topLeft = androidx.compose.ui.geometry.Offset(
                center.x - radius,
                center.y - radius
            ),
            size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
        )
    }
}

@Composable
fun FloatingActionButtonWithAnimation(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AerospacePrimary,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_scale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 15f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "fab_rotation"
    )
    
    FloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .rotate(rotation),
        containerColor = containerColor
    ) {
        content()
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}

@Composable
fun WaveLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = AerospacePrimary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_loading")
    
    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave1"
    )
    
    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, 200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave2"
    )
    
    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, 400, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave3"
    )
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val scale = when (index) {
                0 -> wave1
                1 -> wave2
                else -> wave3
            }
            
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .scale(0.5f + scale * 0.5f)
                    .background(
                        color = color.copy(alpha = 0.5f + scale * 0.5f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    gradient: List<Color> = listOf(GradientStart, GradientMiddle, GradientEnd)
) {
    val scale by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "button_scale"
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .scale(scale)
            .background(
                brush = Brush.horizontalGradient(gradient),
                shape = RoundedCornerShape(12.dp)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(gradient),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnimatedCounter(
    targetValue: Int,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = LocalTextStyle.current,
    animationDuration: Int = 1000
) {
    var currentValue by remember { mutableStateOf(0) }
    
    LaunchedEffect(targetValue) {
        val startValue = currentValue
        val valueRange = targetValue - startValue
        val startTime = System.currentTimeMillis()
        
        while (currentValue != targetValue) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / animationDuration).coerceAtMost(1f)
            
            currentValue = (startValue + valueRange * progress).toInt()
            
            if (progress >= 1f) break
            delay(16) // ~60 FPS
        }
    }
    
    Text(
        text = currentValue.toString(),
        modifier = modifier,
        style = textStyle
    )
}

@Composable
fun BouncyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bouncy_card_scale"
    )
    
    Card(
        modifier = modifier.scale(scale),
        onClick = {
            isPressed = true
            onClick?.invoke()
        },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 6.dp
        )
    ) {
        content()
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}
