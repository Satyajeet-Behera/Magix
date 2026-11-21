package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssistantState
import com.example.ui.theme.MagixTheme

/**
 * Top-of-Screen MAGIX Brand Header.
 * Anchors the prominent MAGIX Logo at the top of the interface for a luxurious,
 * futuristic Jarvis feel, with direct status indicators and one-tap controls
 * for the system-wide top-of-screen floating Dynamic Island logo.
 */
@Composable
fun TopMagixBrandHeader(
    assistantState: AssistantState,
    isOverlayGranted: Boolean,
    onRequestOverlay: () -> Unit,
    onTestFloatPill: () -> Unit,
    onTriggerJarvis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MagixTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "TopHeaderPulse")

    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloScale"
    )

    val haloAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "HaloAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_magix_brand_header"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- PROMINENT TOP MAGIX LOGO & BRAND IDENTITY ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.surface1.copy(alpha = 0.92f),
                            colors.surface2.copy(alpha = 0.80f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            colors.cyan.copy(alpha = haloAlpha),
                            colors.purpleExec.copy(alpha = 0.4f),
                            colors.cyan.copy(alpha = haloAlpha)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Glowing MAGIX Logo Core & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Futuristic Glowing Quantum Logo Emblem
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF070B14))
                            .border(1.5.dp, colors.cyan.copy(alpha = haloAlpha), CircleShape)
                            .clickable { onTriggerJarvis() },
                        contentAlignment = Alignment.Center
                    ) {
                        // Ambient radial glow
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .scale(haloPulse)
                                .clip(CircleShape)
                                .background(colors.cyan.copy(alpha = 0.18f))
                        )
                        // Inner Core Ring
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(1.2.dp, colors.cyanGlow, CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(colors.cyanGlow, colors.cyan.copy(alpha = 0.3f), Color.Transparent)
                                    )
                                )
                        )
                        // Center Quantum Spark
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "M A G I X",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.5.sp,
                                color = colors.textPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors.cyan.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "v3.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.cyanGlow
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AUTONOMOUS SYSTEM · JARVIS CORE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp,
                            color = colors.cyan.copy(alpha = 0.85f)
                        )
                    }
                }

                // Right: "Hey Magix!" voice invocation trigger button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface3)
                        .border(1.dp, colors.cyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { onTriggerJarvis() }
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                        .testTag("top_hey_magix_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Hey Magix Voice Trigger",
                            tint = colors.cyan,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Hey Magix!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- TOP-OF-SCREEN FLOATING PRESENCE BANNER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface1.copy(alpha = 0.7f))
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isOverlayGranted) colors.greenSuccess else colors.amberWarm)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOverlayGranted)
                            "Top-of-Screen Floating Logo: Ready (Minimizing shows pill)"
                        else
                            "Top-of-Screen Floating Logo: Permission needed",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (isOverlayGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.cyan.copy(alpha = 0.15f))
                            .border(1.dp, colors.cyan.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                            .clickable { onTestFloatPill() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("test_floating_logo_pill")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = "Test Floating Logo",
                                tint = colors.cyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Test Pill",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.cyanGlow
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.amberWarm.copy(alpha = 0.15f))
                            .border(1.dp, colors.amberWarm.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onRequestOverlay() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("enable_floating_logo_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Layers,
                                contentDescription = "Enable Floating Logo",
                                tint = colors.amberWarm,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Enable",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.amberWarm
                            )
                        }
                    }
                }
            }
        }
    }
}
