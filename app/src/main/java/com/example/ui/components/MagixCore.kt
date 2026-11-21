package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.domain.model.AssistantState
import com.example.ui.theme.MagixTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * MAGIX Core Visual Component.
 * The signature interactive identity of the MAGIX assistant.
 * Rendered with high-efficiency Jetpack Compose Canvas, engineered for low battery consumption
 * and silky-smooth 60fps+ transitions across all seven unified states.
 */
@Composable
fun MagixCore(
    state: AssistantState,
    modifier: Modifier = Modifier,
    coreSize: Dp = 190.dp,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "MagixCoreContinuousMotion")

    // State color interpolation for seamless morphing between states
    val activeAccentColor by animateColorAsState(
        targetValue = when (state) {
            AssistantState.IDLE -> MagixTheme.colors.cyan
            AssistantState.LISTENING -> MagixTheme.colors.cyanGlow
            AssistantState.THINKING -> MagixTheme.colors.cyan
            AssistantState.SPEAKING -> MagixTheme.colors.cyanGlow
            AssistantState.EXECUTING -> MagixTheme.colors.purpleExec
            AssistantState.SUCCESS -> MagixTheme.colors.greenSuccess
            AssistantState.ERROR -> MagixTheme.colors.redError
        },
        animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing),
        label = "coreAccentColor"
    )

    // Dynamic scale spring on state transitions
    val targetScale = when (state) {
        AssistantState.LISTENING -> 1.04f
        AssistantState.THINKING -> 0.98f
        AssistantState.SPEAKING -> 1.02f
        AssistantState.EXECUTING -> 1.03f
        AssistantState.SUCCESS -> 1.05f
        AssistantState.ERROR -> 0.96f
        AssistantState.IDLE -> 1.0f
    }
    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "coreScaleSpring"
    )

    // 1. IDLE: Calm, harmonic breathing pulse (3200ms cycle, low power consumption)
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )

    // 2. IDLE & AMBIENT: Slow orbital rotation (18s per rotation)
    val slowOrbitalDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "slowOrbitalDeg"
    )

    // 3. LISTENING: Acoustic ripple propagation (1300ms cycle)
    val ripplePhase by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripplePhase"
    )

    // 4. THINKING & EXECUTING: Quantum orbital rotation
    val quantumRotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "quantumRotationDeg"
    )

    // 5. SPEAKING: Waveform undulation
    val acousticWave by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "acousticWave"
    )

    // 6. ERROR: Micro-vibration dissonance
    val errorJitter by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 75, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "errorJitter"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isDarkTheme = MagixTheme.colors.isDark

    Box(
        modifier = modifier
            .size(coreSize)
            .scale(animatedScale)
            .clip(CircleShape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .testTag("magix_core_component"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.width / 2f) * 0.72f

            when (state) {
                AssistantState.IDLE -> {
                    drawIdleCore(
                        center = center,
                        baseRadius = baseRadius * breathScale,
                        orbitalDeg = slowOrbitalDeg,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.LISTENING -> {
                    drawListeningCore(
                        center = center,
                        baseRadius = baseRadius,
                        ripplePhase = ripplePhase,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.THINKING -> {
                    drawThinkingCore(
                        center = center,
                        baseRadius = baseRadius,
                        rotationDeg = quantumRotationDeg,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.SPEAKING -> {
                    drawSpeakingCore(
                        center = center,
                        baseRadius = baseRadius,
                        waveAmplitude = acousticWave,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.EXECUTING -> {
                    drawExecutingCore(
                        center = center,
                        baseRadius = baseRadius,
                        rotationDeg = quantumRotationDeg,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.SUCCESS -> {
                    drawSuccessCore(
                        center = center,
                        baseRadius = baseRadius * breathScale,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
                AssistantState.ERROR -> {
                    drawErrorCore(
                        center = center + Offset(errorJitter, 0f),
                        baseRadius = baseRadius,
                        accentColor = activeAccentColor,
                        isDark = isDarkTheme
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// IDLE: Calm, dignified standby state with orbiting satellites and gentle ambient breathing
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawIdleCore(
    center: Offset,
    baseRadius: Float,
    orbitalDeg: Float,
    accentColor: Color,
    isDark: Boolean
) {
    val glowAlpha = if (isDark) 0.18f else 0.10f

    // Ambient radial aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = glowAlpha), Color.Transparent),
            center = center,
            radius = baseRadius * 1.55f
        ),
        radius = baseRadius * 1.55f,
        center = center
    )

    // Outer precision ring with cardinal tick marks
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.28f else 0.22f),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 1.5f)
    )

    for (i in 0 until 4) {
        val tickAngle = (i * 90.0) * (PI / 180.0)
        val outerTick = Offset(
            center.x + baseRadius * cos(tickAngle).toFloat(),
            center.y + baseRadius * sin(tickAngle).toFloat()
        )
        val innerTick = Offset(
            center.x + (baseRadius - 7f) * cos(tickAngle).toFloat(),
            center.y + (baseRadius - 7f) * sin(tickAngle).toFloat()
        )
        drawLine(
            color = accentColor.copy(alpha = 0.5f),
            start = innerTick,
            end = outerTick,
            strokeWidth = 2f
        )
    }

    // Intermediate satellite track
    val midRadius = baseRadius * 0.72f
    drawCircle(
        color = accentColor.copy(alpha = if (isDark) 0.35f else 0.25f),
        radius = midRadius,
        center = center,
        style = Stroke(
            width = 1.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), orbitalDeg * 0.5f)
        )
    )

    // 4 micro-satellites orbiting along the middle track
    for (i in 0 until 4) {
        val angle = (orbitalDeg + i * 90) * (PI / 180.0)
        val nodeX = center.x + midRadius * cos(angle).toFloat()
        val nodeY = center.y + midRadius * sin(angle).toFloat()

        // Satellite glow halo
        drawCircle(
            color = accentColor.copy(alpha = 0.3f),
            radius = 6.5f,
            center = Offset(nodeX, nodeY)
        )
        // Satellite core
        drawCircle(
            color = if (isDark) Color.White else accentColor,
            radius = 3f,
            center = Offset(nodeX, nodeY)
        )
    }

    // Center dual-layered luminous nucleus
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                if (isDark) Color.White.copy(alpha = 0.95f) else accentColor,
                accentColor.copy(alpha = 0.7f),
                accentColor.copy(alpha = 0.25f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 0.42f
        ),
        radius = baseRadius * 0.42f,
        center = center
    )
}

// -----------------------------------------------------------------------------------------
// LISTENING: Expanding acoustic intake ripples and glowing responsive microphone diaphragm
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawListeningCore(
    center: Offset,
    baseRadius: Float,
    ripplePhase: Float,
    accentColor: Color,
    isDark: Boolean
) {
    // Primary expanding wave
    val wave1Alpha = (1f - (ripplePhase - 0.65f) / 0.7f).coerceIn(0f, 0.75f)
    drawCircle(
        color = accentColor.copy(alpha = wave1Alpha * 0.6f),
        radius = baseRadius * ripplePhase,
        center = center,
        style = Stroke(width = 2.5f)
    )

    // Secondary offset trailing wave
    val wave2Phase = if (ripplePhase > 1.0f) ripplePhase - 0.35f else ripplePhase + 0.35f
    val wave2Alpha = (1f - (wave2Phase - 0.65f) / 0.7f).coerceIn(0f, 0.65f)
    drawCircle(
        color = accentColor.copy(alpha = wave2Alpha * 0.45f),
        radius = baseRadius * wave2Phase,
        center = center,
        style = Stroke(width = 1.8f)
    )

    // Boundary acoustic receiver ring
    drawCircle(
        color = accentColor.copy(alpha = 0.4f),
        radius = baseRadius * 0.85f,
        center = center,
        style = Stroke(
            width = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 8f), 0f)
        )
    )

    // Active audio-intake diaphragm at center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                if (isDark) Color.White else accentColor,
                accentColor,
                accentColor.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 0.60f
        ),
        radius = baseRadius * 0.60f,
        center = center
    )

    // Microphone intake crosshairs
    val crossLen = baseRadius * 0.22f
    drawLine(
        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.White,
        start = Offset(center.x - crossLen, center.y),
        end = Offset(center.x + crossLen, center.y),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = if (isDark) Color.White.copy(alpha = 0.8f) else Color.White,
        start = Offset(center.x, center.y - crossLen),
        end = Offset(center.x, center.y + crossLen),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

// -----------------------------------------------------------------------------------------
// THINKING: Dual counter-rotating gyroscopic orbits with 3-phase calculation particles
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawThinkingCore(
    center: Offset,
    baseRadius: Float,
    rotationDeg: Float,
    accentColor: Color,
    isDark: Boolean
) {
    val rotOuterRad = rotationDeg * (PI / 180.0)
    val rotInnerRad = -rotationDeg * 1.4 * (PI / 180.0)

    // Outer quantum orbit (clockwise)
    drawCircle(
        color = accentColor.copy(alpha = 0.4f),
        radius = baseRadius,
        center = center,
        style = Stroke(
            width = 2.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(28f, 18f), rotationDeg)
        )
    )

    // Inner counter-orbit (counter-clockwise)
    val innerRadius = baseRadius * 0.72f
    drawCircle(
        color = accentColor.copy(alpha = 0.6f),
        radius = innerRadius,
        center = center,
        style = Stroke(
            width = 2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 14f), -rotationDeg * 1.4f)
        )
    )

    // 3 calculation pips at 120-degree offsets on the inner orbit
    for (i in 0 until 3) {
        val angle = rotInnerRad + i * (2 * PI / 3)
        val px = center.x + innerRadius * cos(angle).toFloat()
        val py = center.y + innerRadius * sin(angle).toFloat()

        drawCircle(
            color = accentColor.copy(alpha = 0.35f),
            radius = 7f,
            center = Offset(px, py)
        )
        drawCircle(
            color = if (isDark) Color.White else accentColor,
            radius = 3.5f,
            center = Offset(px, py)
        )
    }

    // Outer orbiting leader pip
    val leadX = center.x + baseRadius * cos(rotOuterRad).toFloat()
    val leadY = center.y + baseRadius * sin(rotOuterRad).toFloat()
    drawCircle(
        color = if (isDark) Color.White else accentColor,
        radius = 4.5f,
        center = Offset(leadX, leadY)
    )

    // Pulsing neural processor nucleus
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                if (isDark) Color.White else accentColor,
                accentColor.copy(alpha = 0.8f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 0.38f
        ),
        radius = baseRadius * 0.38f,
        center = center
    )
}

// -----------------------------------------------------------------------------------------
// SPEAKING: Symmetrical 7-channel neural frequency visualizer waveform bars
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawSpeakingCore(
    center: Offset,
    baseRadius: Float,
    waveAmplitude: Float,
    accentColor: Color,
    isDark: Boolean
) {
    // Outer boundary ring
    drawCircle(
        color = accentColor.copy(alpha = 0.32f),
        radius = baseRadius,
        center = center,
        style = Stroke(width = 1.6f)
    )

    // Harmonic voice spectrum bars across core (7 bars)
    val barCount = 7
    val barSpacing = (baseRadius * 1.25f) / (barCount - 1)
    val startX = center.x - (baseRadius * 0.625f)

    for (i in 0 until barCount) {
        val x = startX + i * barSpacing
        val normalizedIndex = i.toFloat() / (barCount - 1)
        val envelope = sin(normalizedIndex * PI).toFloat()
        val barHalfHeight = (baseRadius * 0.68f) * (0.28f + 0.72f * waveAmplitude * envelope)

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.9f),
                    if (isDark) Color.White else accentColor,
                    accentColor.copy(alpha = 0.9f),
                    Color.Transparent
                ),
                startY = center.y - barHalfHeight,
                endY = center.y + barHalfHeight
            ),
            start = Offset(x, center.y - barHalfHeight),
            end = Offset(x, center.y + barHalfHeight),
            strokeWidth = 4.2f,
            cap = StrokeCap.Round
        )
    }

    // Voice acoustic aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.45f), Color.Transparent),
            center = center,
            radius = baseRadius * 0.50f
        ),
        radius = baseRadius * 0.50f,
        center = center
    )
}

// -----------------------------------------------------------------------------------------
// EXECUTING: High-velocity amethyst accelerator with directional energy streaks
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawExecutingCore(
    center: Offset,
    baseRadius: Float,
    rotationDeg: Float,
    accentColor: Color,
    isDark: Boolean
) {
    // Amethyst outer accelerator
    drawCircle(
        color = accentColor.copy(alpha = 0.55f),
        radius = baseRadius,
        center = center,
        style = Stroke(
            width = 3.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(36f, 12f), rotationDeg * 2.2f)
        )
    )

    // Inner reverse-cycle stabilizer
    val innerRadius = baseRadius * 0.65f
    drawCircle(
        color = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.7f) else accentColor.copy(alpha = 0.8f),
        radius = innerRadius,
        center = center,
        style = Stroke(
            width = 2.2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), -rotationDeg * 1.5f)
        )
    )

    // High-speed orbit particles
    val angle1 = (rotationDeg * 2.2) * (PI / 180.0)
    val px = center.x + baseRadius * cos(angle1).toFloat()
    val py = center.y + baseRadius * sin(angle1).toFloat()
    drawCircle(color = if (isDark) Color.White else accentColor, radius = 5.5f, center = Offset(px, py))

    // Dynamic execution reactor core
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                if (isDark) Color.White else Color(0xFFF3E8FF),
                accentColor,
                accentColor.copy(alpha = 0.4f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 0.45f
        ),
        radius = baseRadius * 0.45f,
        center = center
    )
}

// -----------------------------------------------------------------------------------------
// SUCCESS: Radiant emerald bloom with precision vector checkmark
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawSuccessCore(
    center: Offset,
    baseRadius: Float,
    accentColor: Color,
    isDark: Boolean
) {
    // Outer emerald bloom
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.45f
        ),
        radius = baseRadius * 1.45f,
        center = center
    )

    // Solid verified perimeter
    drawCircle(
        color = accentColor,
        radius = baseRadius * 0.88f,
        center = center,
        style = Stroke(width = 3.2f)
    )

    // Precision Vector Checkmark
    val checkPath = Path().apply {
        moveTo(center.x - baseRadius * 0.24f, center.y + baseRadius * 0.02f)
        lineTo(center.x - baseRadius * 0.04f, center.y + baseRadius * 0.22f)
        lineTo(center.x + baseRadius * 0.28f, center.y - baseRadius * 0.16f)
    }

    drawPath(
        path = checkPath,
        color = accentColor,
        style = Stroke(
            width = 5.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

// -----------------------------------------------------------------------------------------
// ERROR: High-frequency dissonance jitter with caution perimeter and diagonal indicator
// -----------------------------------------------------------------------------------------
private fun DrawScope.drawErrorCore(
    center: Offset,
    baseRadius: Float,
    accentColor: Color,
    isDark: Boolean
) {
    // Soft alert beacon
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(accentColor.copy(alpha = 0.35f), Color.Transparent),
            center = center,
            radius = baseRadius * 1.35f
        ),
        radius = baseRadius * 1.35f,
        center = center
    )

    // Dashed warning ring
    drawCircle(
        color = accentColor,
        radius = baseRadius * 0.82f,
        center = center,
        style = Stroke(
            width = 2.8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
        )
    )

    // Centered diagonal alert indicator
    val offsetLen = baseRadius * 0.20f
    drawLine(
        color = accentColor,
        start = Offset(center.x - offsetLen, center.y - offsetLen),
        end = Offset(center.x + offsetLen, center.y + offsetLen),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = accentColor,
        start = Offset(center.x + offsetLen, center.y - offsetLen),
        end = Offset(center.x - offsetLen, center.y + offsetLen),
        strokeWidth = 4.5f,
        cap = StrokeCap.Round
    )
}
