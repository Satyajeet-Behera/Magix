package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssistantState
import com.example.ui.theme.MagixTheme
import kotlinx.coroutines.delay

/**
 * Live Presence Presentation Mode.
 */
enum class LivePresenceMode {
    COMPACT,
    EXPANDED
}

/**
 * MAGIX Live Presence Component.
 * The persistent, top-of-screen presence providing real-time Jarvis-style interaction.
 * Accounts for WindowInsets and camera cutouts without hardcoding coordinates.
 * Supports: IDLE, LISTENING, THINKING, SPEAKING, EXECUTING, SUCCESS, ERROR, EXPANDED.
 */
@Composable
fun MagixLivePresence(
    assistantState: AssistantState,
    onMicToggle: () -> Unit,
    onInterrupt: () -> Unit,
    modifier: Modifier = Modifier,
    activeTranscript: String? = null,
    isOverlayGranted: Boolean = false,
    onRequestOverlay: (() -> Unit)? = null,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit = {},
    hasMicPermission: Boolean = true,
    onRequestMicPermission: () -> Unit = {},
    onLaunchSpeechIntent: () -> Unit = {},
    onSubmitDirective: (String) -> Unit = {}
) {
    // Dynamic top inset calculation accounting for status bars and display cutouts
    val statusBarInsets = WindowInsets.statusBars.asPaddingValues()
    val cutoutInsets = WindowInsets.displayCutout.asPaddingValues()
    val safeTopPadding = maxOf(statusBarInsets.calculateTopPadding(), cutoutInsets.calculateTopPadding(), 8.dp)

    var presenceMode by remember { mutableStateOf(if (isExpanded) LivePresenceMode.EXPANDED else LivePresenceMode.COMPACT) }
    var directiveInput by remember { mutableStateOf("") }

    // Synchronize with external expansion state
    LaunchedEffect(isExpanded) {
        presenceMode = if (isExpanded) LivePresenceMode.EXPANDED else LivePresenceMode.COMPACT
    }

    // Automatically expand when assistant is actively working, then collapse when done
    LaunchedEffect(assistantState) {
        when (assistantState) {
            AssistantState.LISTENING,
            AssistantState.THINKING,
            AssistantState.SPEAKING,
            AssistantState.EXECUTING -> {
                presenceMode = LivePresenceMode.EXPANDED
                onExpandedChange(true)
            }
            AssistantState.SUCCESS -> {
                presenceMode = LivePresenceMode.EXPANDED
                delay(2200)
                presenceMode = LivePresenceMode.COMPACT
                onExpandedChange(false)
            }
            AssistantState.ERROR -> {
                presenceMode = LivePresenceMode.EXPANDED
                delay(3000)
                presenceMode = LivePresenceMode.COMPACT
                onExpandedChange(false)
            }
            AssistantState.IDLE -> {
                if (isExpanded) {
                    presenceMode = LivePresenceMode.EXPANDED
                }
            }
        }
    }

    val stateColor by animateColorAsState(
        targetValue = when (assistantState) {
            AssistantState.IDLE -> MagixTheme.colors.cyan
            AssistantState.LISTENING -> MagixTheme.colors.cyanGlow
            AssistantState.THINKING -> MagixTheme.colors.cyan
            AssistantState.SPEAKING -> MagixTheme.colors.cyanGlow
            AssistantState.EXECUTING -> MagixTheme.colors.purpleExec
            AssistantState.SUCCESS -> MagixTheme.colors.greenSuccess
            AssistantState.ERROR -> MagixTheme.colors.redError
        },
        animationSpec = tween(durationMillis = 350),
        label = "presenceStateColor"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "presenceHalo")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = safeTopPadding)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        val shape = RoundedCornerShape(if (presenceMode == LivePresenceMode.EXPANDED) 24.dp else 20.dp)

        Box(
            modifier = Modifier
                .shadow(
                    elevation = if (presenceMode == LivePresenceMode.EXPANDED) 16.dp else 8.dp,
                    shape = shape,
                    ambientColor = stateColor.copy(alpha = 0.25f),
                    spotColor = stateColor.copy(alpha = 0.40f)
                )
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MagixTheme.colors.surface0.copy(alpha = 0.96f),
                            MagixTheme.colors.surface1.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    color = stateColor.copy(alpha = if (presenceMode == LivePresenceMode.EXPANDED) 0.65f else 0.40f),
                    shape = shape
                )
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .testTag("magix_live_presence_bar")
        ) {
            if (presenceMode == LivePresenceMode.COMPACT) {
                // -------------------------------------------------------------
                // COMPACT PRESENCE PILL
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier
                        .clickable {
                            presenceMode = LivePresenceMode.EXPANDED
                            onExpandedChange(true)
                        }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("presence_compact_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    MagixCore(
                        state = assistantState,
                        coreSize = 20.dp
                    )

                    Text(
                        text = "MAGIX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = MagixTheme.colors.textPrimary
                    )

                    Text(
                        text = when (assistantState) {
                            AssistantState.IDLE -> "READY"
                            AssistantState.LISTENING -> "LISTENING"
                            AssistantState.THINKING -> "THINKING"
                            AssistantState.SPEAKING -> "SPEAKING"
                            AssistantState.EXECUTING -> "EXECUTING"
                            AssistantState.SUCCESS -> "DONE"
                            AssistantState.ERROR -> "ALERT"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stateColor
                    )

                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(stateColor.copy(alpha = pulseAlpha))
                    )
                }
            } else {
                // -------------------------------------------------------------
                // EXPANDED INTERACTIVE CONTROL PANEL (POPPED UP)
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                        .padding(16.dp)
                        .testTag("presence_expanded_panel")
                ) {
                    // Header Row: Miniature Core, State Title & Collapse Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MagixCore(
                                state = assistantState,
                                coreSize = 36.dp,
                                onClick = {
                                    if (!hasMicPermission) {
                                        onRequestMicPermission()
                                    } else {
                                        onMicToggle()
                                    }
                                }
                            )

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "MAGIX",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.2.sp,
                                        color = MagixTheme.colors.cyan
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(stateColor)
                                    )
                                    Text(
                                        text = assistantState.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = stateColor
                                    )
                                }

                                Text(
                                    text = when (assistantState) {
                                        AssistantState.LISTENING -> "Listening for your voice..."
                                        AssistantState.SPEAKING -> "Magix speaking..."
                                        AssistantState.THINKING -> "Synthesizing plan..."
                                        AssistantState.EXECUTING -> "Executing directive..."
                                        else -> "Ready when you are"
                                    },
                                    fontSize = 12.sp,
                                    color = MagixTheme.colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Collapse Pill Button (matches screenshot)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MagixTheme.colors.surface2)
                                .clickable {
                                    presenceMode = LivePresenceMode.COMPACT
                                    onExpandedChange(false)
                                }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("presence_collapse_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowUp,
                                contentDescription = "Collapse Presence",
                                tint = MagixTheme.colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Transcript or Subtitle Banner
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MagixTheme.colors.surface2.copy(alpha = 0.7f))
                            .border(1.dp, stateColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when {
                                    !activeTranscript.isNullOrBlank() -> activeTranscript
                                    assistantState == AssistantState.LISTENING -> "Listening... Speak your directive now"
                                    assistantState == AssistantState.SPEAKING -> "Magix active..."
                                    !hasMicPermission -> "Microphone permission needed to hear you"
                                    else -> "Standby mode • Ambient monitoring"
                                },
                                fontSize = 12.sp,
                                color = MagixTheme.colors.textPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            if (assistantState == AssistantState.LISTENING) {
                                AudioWaveVisualizer(
                                    color = stateColor,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }

                    // Interactive Directive Quick Input & Suggestions
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MagixTheme.colors.surface2)
                            .border(1.dp, MagixTheme.colors.borderSubtle, RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = directiveInput,
                            onValueChange = { directiveInput = it },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                                .testTag("presence_directive_text_input"),
                            textStyle = TextStyle(
                                color = MagixTheme.colors.textPrimary,
                                fontSize = 12.sp
                            ),
                            cursorBrush = SolidColor(MagixTheme.colors.cyan),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (directiveInput.isNotBlank()) {
                                        onSubmitDirective(directiveInput)
                                        directiveInput = ""
                                    }
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (directiveInput.isEmpty()) {
                                    Text(
                                        text = if (assistantState == AssistantState.LISTENING) "Listening or type directive..." else "Type or speak directive...",
                                        fontSize = 11.sp,
                                        color = MagixTheme.colors.textSecondary
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (directiveInput.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    onSubmitDirective(directiveInput)
                                    directiveInput = ""
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("presence_send_directive_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Directive",
                                    tint = MagixTheme.colors.cyan,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Quick Suggested Directives
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "Open YouTube and search lo-fi",
                            "Triage unread emails",
                            "Turn on flashlight",
                            "Take meeting note"
                        ).forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MagixTheme.colors.surface3.copy(alpha = 0.6f))
                                    .border(1.dp, MagixTheme.colors.borderSubtle, RoundedCornerShape(8.dp))
                                    .clickable {
                                        onSubmitDirective(prompt)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 10.sp,
                                    color = MagixTheme.colors.cyanGlow
                                )
                            }
                        }
                    }

                    // Action Dock: Interruption / Cancel, Speak Toggle
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Interruption Button ("Magix, stop")
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MagixTheme.colors.redError.copy(alpha = 0.15f))
                                .border(1.dp, MagixTheme.colors.redError.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    onInterrupt()
                                    presenceMode = LivePresenceMode.COMPACT
                                    onExpandedChange(false)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("presence_interrupt_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Interrupt MAGIX",
                                tint = MagixTheme.colors.redError,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Interrupt / Stop",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MagixTheme.colors.redError
                            )
                        }

                        // Right: Mic Direct Toggle Button (matches screenshot)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(stateColor.copy(alpha = 0.20f))
                                .border(1.dp, stateColor.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable {
                                if (!hasMicPermission) {
                                    onRequestMicPermission()
                                } else {
                                    onMicToggle()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("presence_mic_toggle_btn"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Toggle Mic",
                                tint = stateColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = when {
                                    !hasMicPermission -> "Grant Mic Permission"
                                    assistantState == AssistantState.LISTENING -> "Finish Speech"
                                    else -> "Speak Directive"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = stateColor
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Animated audio waveform visualizer for the live listening state.
 */
@Composable
private fun AudioWaveVisualizer(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val h1 by infiniteTransition.animateFloat(
        initialValue = 4f, targetValue = 20f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 16f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(450, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 8f, targetValue = 22f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 18f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )

    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(h1, h2, h3, h4).forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
        }
    }
}
