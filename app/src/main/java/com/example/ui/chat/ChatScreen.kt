package com.example.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.AssistantState
import com.example.domain.model.ChatMessage
import com.example.domain.model.ChatSender
import com.example.domain.model.MagixVoiceProfile
import com.example.ui.MagixViewModel
import com.example.ui.components.DevMockBadge
import com.example.ui.theme.MagixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatScreen(
    viewModel: MagixViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val activeTaskProgress by viewModel.activeTaskProgress.collectAsState()
    val isGeminiOnline by viewModel.isGeminiOnline.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val recognizedSpeechText by viewModel.recognizedSpeechText.collectAsState()
    val speechError by viewModel.speechError.collectAsState()
    val speechRmsLevel by viewModel.speechRmsLevel.collectAsState()
    val colors = MagixTheme.colors
    val activeVoice = MagixVoiceProfile.findByIdOrDefault(userSettings.selectedVoiceId)

    var inputPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Sensitive Confirmation Dialog
    pendingConfirmation?.let { confirmResult ->
        com.example.ui.components.ConfirmationDialog(
            actionResult = confirmResult,
            onConfirm = { viewModel.confirmPendingAction(true) },
            onCancel = { viewModel.confirmPendingAction(false) }
        )
    }

    // Auto-scroll to latest message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface0)
            .statusBarsPadding()
            .imePadding()
            .testTag("magix_chat_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Conversational Stream",
                        style = MagixTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Active Voice: ${activeVoice.name} (${activeVoice.internalId})",
                        fontSize = 12.sp,
                        color = colors.cyan
                    )
                }

                if (messages.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.clearChat() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Clear History",
                            tint = colors.textSecondary
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
                DevMockBadge(text = if (isGeminiOnline) "Gemini Neural Core & Controlled Android Tools" else "Offline Autonomous Engine & Controlled Tools")
            }

            // Autonomous Task Progress Banner
            androidx.compose.animation.AnimatedVisibility(visible = activeTaskProgress != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface2)
                        .border(1.dp, colors.purpleExec.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "AUTONOMOUS SUBROUTINE",
                            style = MagixTheme.typography.labelSmall,
                            color = colors.purpleExec,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.redError.copy(alpha = 0.2f))
                                .clickable { viewModel.emergencyHalt() }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "HALT",
                                style = MagixTheme.typography.labelSmall,
                                color = colors.redError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeTaskProgress ?: "",
                        style = MagixTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                }
            }

            // Assistant State Indicator Bar (if Thinking or Speaking)
            AnimatedVisibility(visible = assistantState != AssistantState.IDLE) {
                val stateColor = when (assistantState) {
                    AssistantState.IDLE -> colors.cyan
                    AssistantState.LISTENING -> colors.cyanGlow
                    AssistantState.THINKING -> colors.cyan
                    AssistantState.SPEAKING -> colors.cyanGlow
                    AssistantState.EXECUTING -> colors.purpleExec
                    AssistantState.SUCCESS -> colors.greenSuccess
                    AssistantState.ERROR -> colors.redError
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface2)
                        .border(1.dp, stateColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (assistantState == AssistantState.THINKING) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.cyan
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.GraphicEq,
                                contentDescription = null,
                                tint = stateColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${assistantState.title}: ${assistantState.detail}",
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            // Message Stream or Empty State
            if (messages.isEmpty()) {
                EmptyChatState(
                    onSamplePromptSelected = { prompt ->
                        viewModel.sendUserMessage(prompt)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }

                    items(messages, key = { it.id }) { message ->
                        ChatMessageItem(
                            message = message,
                            onPlayVoicePlaceholder = {
                                viewModel.auditionVoice(activeVoice)
                            }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // Floating Input Dock (Seamless Voice-First Directive HUD)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .padding(bottom = 60.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isListening) {
                        // --- LIVE SPEECH RECOGNITION HUD (VOICE-TO-TEXT COMMANDS) ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(colors.surface2)
                                .border(1.5.dp, colors.cyan, RoundedCornerShape(24.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Pulsing Listening Indicator
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyan)
                                    .clickable { viewModel.triggerMicToggle() }
                                    .testTag("chat_mic_active_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Stop Listening",
                                    tint = colors.void,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (recognizedSpeechText.isNotBlank()) "\"$recognizedSpeechText\"" else "Listening for command...",
                                    fontSize = 13.sp,
                                    fontWeight = if (recognizedSpeechText.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (recognizedSpeechText.isNotBlank()) colors.cyanGlow else colors.textSecondary,
                                    maxLines = 2
                                )
                                Text(
                                    text = "Speak naturally · Android SpeechRecognizer active",
                                    fontSize = 10.sp,
                                    color = colors.textTertiary
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Finish / Send Speech Command
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyan)
                                    .clickable {
                                        if (recognizedSpeechText.isNotBlank()) {
                                            viewModel.submitDirectiveDirectly(recognizedSpeechText)
                                        } else {
                                            viewModel.triggerMicToggle()
                                        }
                                    }
                                    .testTag("chat_voice_done_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Execute Spoken Directive",
                                    tint = colors.void,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // --- STANDARD DUAL INPUT: INSTANT VOICE-TO-TEXT OR MANUAL FALLBACK ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(28.dp))
                                .background(colors.surface1)
                                .border(1.dp, colors.border, RoundedCornerShape(28.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Voice-to-Text Primary Mic Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyan.copy(alpha = 0.2f))
                                    .border(1.dp, colors.cyan.copy(alpha = 0.6f), CircleShape)
                                    .clickable {
                                        if (!viewModel.isMicrophonePermissionGranted) {
                                            viewModel.requestMicrophonePermission()
                                        } else {
                                            viewModel.triggerMicListening()
                                        }
                                    }
                                    .testTag("chat_mic_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "Tap to Speak Directive",
                                    tint = colors.cyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            TextField(
                                value = inputPrompt,
                                onValueChange = { inputPrompt = it },
                                placeholder = {
                                    Text(
                                        text = "Tap mic to speak, or type command...",
                                        fontSize = 13.sp,
                                        color = colors.textTertiary
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = colors.textPrimary,
                                    unfocusedTextColor = colors.textPrimary
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(onSend = {
                                    if (inputPrompt.isNotBlank()) {
                                        viewModel.sendUserMessage(inputPrompt)
                                        inputPrompt = ""
                                    }
                                }),
                                maxLines = 3,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_textfield")
                            )

                            // Send Button
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (inputPrompt.isNotBlank()) colors.cyan else colors.surface2)
                                    .clickable(enabled = inputPrompt.isNotBlank()) {
                                        viewModel.sendUserMessage(inputPrompt)
                                        inputPrompt = ""
                                    }
                                    .testTag("chat_send_button"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (inputPrompt.isNotBlank()) colors.void else colors.textTertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: ChatMessage,
    onPlayVoicePlaceholder: () -> Unit
) {
    val isUser = message.sender == ChatSender.USER
    val colors = MagixTheme.colors
    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(if (isUser) colors.surface2 else colors.surface1)
                .border(
                    1.dp,
                    if (isUser) colors.border else colors.border.copy(alpha = 0.6f),
                    RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(colors.cyan)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MAGIX CORE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.cyan
                            )
                        }

                        // Voice synthesis simulated playback trigger
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.surface3)
                                .clickable(onClick = onPlayVoicePlaceholder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                                    contentDescription = "Play acoustic synthesis placeholder",
                                    tint = colors.textAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Preview Audio",
                                    fontSize = 9.sp,
                                    color = colors.textAccent
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = colors.textTertiary,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    onSamplePromptSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MagixTheme.colors
    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surface1)
                .border(1.dp, colors.cyan.copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.GraphicEq,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Dialogue Channel Ready",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Speak or enter a directive below. MAGIX will execute with controlled development responses.",
            fontSize = 13.sp,
            color = colors.textSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SUGGESTED DIRECTIVES",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textTertiary
        )

        Spacer(modifier = Modifier.height(10.dp))

        val prompts = listOf(
            "Check MAGIX system telemetry and readiness",
            "What automations are currently configured?",
            "Store preferred browser in memory repository",
            "Perform audio visualizer diagnostic test"
        )

        prompts.forEach { prompt ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(14.dp))
                    .clickable { onSamplePromptSelected(prompt) }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = prompt,
                    fontSize = 12.sp,
                    color = colors.textPrimary
                )
            }
        }
    }
}
