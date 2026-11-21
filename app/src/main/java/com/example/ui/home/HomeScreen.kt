package com.example.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.example.domain.model.AssistantState
import com.example.domain.model.CapabilityState
import com.example.domain.model.MagixVoiceProfile
import com.example.ui.MagixViewModel
import com.example.ui.components.AuditLogSheet
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.MagixCore
import com.example.ui.components.MagixScreen
import com.example.ui.components.PermissionCenterDialog
import com.example.ui.theme.MagixTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MagixViewModel,
    onNavigate: (MagixScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val assistantState by viewModel.assistantState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val automations by viewModel.automations.collectAsState()
    val capabilities by viewModel.capabilities.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val pendingConfirmation by viewModel.pendingConfirmation.collectAsState()
    val activeTaskProgress by viewModel.activeTaskProgress.collectAsState()
    val isGeminiOnline by viewModel.isGeminiOnline.collectAsState()
    val isAccessActive by viewModel.isAccessibilityActive.collectAsState()
    val scrollState = rememberScrollState()

    var showPermissionCenter by remember { mutableStateOf(false) }
    var showAuditLogSheet by remember { mutableStateOf(false) }

    val colors = MagixTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val headerGreeting = viewModel.greetingEngine.generateHeaderGreeting(userSettings.displayName)
    val activeVoice = MagixVoiceProfile.findByIdOrDefault(userSettings.selectedVoiceId)

    val grantedCount = capabilities.count { it.state == CapabilityState.CONNECTED }

    // Sensitive Confirmation Dialog
    pendingConfirmation?.let { confirmResult ->
        ConfirmationDialog(
            actionResult = confirmResult,
            onConfirm = { viewModel.confirmPendingAction(true) },
            onCancel = { viewModel.confirmPendingAction(false) }
        )
    }

    // Permission Center Dialog
    if (showPermissionCenter) {
        PermissionCenterDialog(
            capabilities = capabilities,
            onDismiss = { showPermissionCenter = false }
        )
    }

    // Action Audit Log Sheet
    if (showAuditLogSheet) {
        AuditLogSheet(
            logs = auditLogs,
            onClearLogs = { viewModel.clearAuditLogs() },
            onDismiss = { showAuditLogSheet = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.surface0, colors.void)
                )
            )
            .statusBarsPadding()
            .testTag("magix_home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP MAGIX LOGO & FLOATING ISLAND BRAND HEADER ---
            com.example.ui.components.TopMagixBrandHeader(
                assistantState = assistantState,
                isOverlayGranted = viewModel.appEngine.canDrawOverlays(),
                onRequestOverlay = {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        "package:${context.packageName}".toUri()
                    ).apply { addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK) }
                    context.startActivity(intent)
                },
                onTestFloatPill = {
                    com.example.service.MagixOverlayService.start(context)
                },
                onTriggerJarvis = {
                    viewModel.speakToMagixAtHomeScreen()
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Header: Display Name & Greeting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = headerGreeting,
                        style = MagixTheme.typography.displayMedium,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = assistantState.statusPhrase,
                        style = MagixTheme.typography.bodyMedium,
                        color = colors.cyan
                    )
                }

                // Voice status indicator chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.surface1)
                        .border(1.dp, colors.border, RoundedCornerShape(20.dp))
                        .clickable { onNavigate(MagixScreen.SETTINGS) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.greenSuccess)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = activeVoice.internalId,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subsystem Telemetry Badges (Intelligence Mode & Permission Status)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Brain Mode Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isGeminiOnline) colors.cyan.copy(alpha = 0.12f) else colors.textMuted.copy(alpha = 0.12f))
                        .border(1.dp, if (isGeminiOnline) colors.cyan.copy(alpha = 0.3f) else colors.borderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isGeminiOnline) colors.cyan else colors.textMuted)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isGeminiOnline) "GEMINI NEURAL BRAIN" else "OFFLINE LOCAL ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isGeminiOnline) colors.cyan else colors.textMuted
                        )
                    }
                }

                // Permission Center Access Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface1)
                        .border(1.dp, if (isAccessActive) colors.success.copy(alpha = 0.4f) else colors.warning.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .clickable { showPermissionCenter = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("home_permission_center_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Security,
                            contentDescription = null,
                            tint = if (isAccessActive) colors.success else colors.warning,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PERMISSIONS ($grantedCount/${capabilities.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                    }
                }

                // Action History Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface1)
                        .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                        .clickable { showAuditLogSheet = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("home_audit_log_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AUDIT (${auditLogs.size})",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }
                }
            }

            // Live Task Execution Banner
            AnimatedVisibility(
                visible = activeTaskProgress != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surface2)
                        .border(1.dp, colors.cyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.purpleExec)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AUTONOMOUS TASK IN PROGRESS",
                                style = MagixTheme.typography.labelSmall,
                                color = colors.purpleExec,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Stop Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.redError.copy(alpha = 0.2f))
                                .clickable { viewModel.emergencyHalt() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "STOP",
                                style = MagixTheme.typography.labelSmall,
                                color = colors.redError,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = activeTaskProgress ?: "",
                        style = MagixTheme.typography.bodySmall,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = colors.cyan,
                        trackColor = colors.surface0
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Central MAGIX Core Component
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(220.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    when (assistantState) {
                                        AssistantState.IDLE -> colors.cyan.copy(alpha = if (colors.isDark) 0.12f else 0.08f)
                                        AssistantState.LISTENING -> colors.cyanGlow.copy(alpha = if (colors.isDark) 0.20f else 0.12f)
                                        AssistantState.THINKING -> colors.cyan.copy(alpha = if (colors.isDark) 0.18f else 0.10f)
                                        AssistantState.SPEAKING -> colors.cyanGlow.copy(alpha = if (colors.isDark) 0.18f else 0.10f)
                                        AssistantState.EXECUTING -> colors.purpleExec.copy(alpha = if (colors.isDark) 0.22f else 0.14f)
                                        AssistantState.SUCCESS -> colors.greenSuccess.copy(alpha = if (colors.isDark) 0.22f else 0.14f)
                                        AssistantState.ERROR -> colors.redError.copy(alpha = if (colors.isDark) 0.22f else 0.14f)
                                    },
                                    Color.Transparent
                                )
                            )
                        )
                )

                MagixCore(
                    state = assistantState,
                    coreSize = 190.dp,
                    onClick = { viewModel.speakToMagixAtHomeScreen() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // State Title & Detail Pill
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
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface1)
                    .border(1.dp, stateColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(stateColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${assistantState.title} · ${assistantState.detail}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Invocation & Interruption Control Dock
            Text(
                text = "INVOCATION & INTERRUPTIONS",
                style = MagixTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // "Hey, Magix!" Invocation Trigger
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .border(1.dp, colors.cyan.copy(alpha = 0.45f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.handleInvocationDetected() }
                        .padding(14.dp)
                        .testTag("home_hey_magix_invocation_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.cyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.RecordVoiceOver,
                                contentDescription = "Say Hey Magix",
                                tint = colors.cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Say \"Hey, Magix!\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Greets ${userSettings.formOfAddress}",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }

                // Interruption Emergency Stop Trigger
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surface1)
                        .border(1.dp, colors.redError.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        .clickable { viewModel.emergencyHalt() }
                        .padding(14.dp)
                        .testTag("home_stop_interrupt_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.redError.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Emergency Stop",
                                tint = colors.redError,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Emergency Stop",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Instant halt / abort",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Voice Interaction Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.borderGlow.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .clickable { viewModel.speakToMagixAtHomeScreen() }
                    .padding(16.dp)
                    .testTag("home_mic_trigger_card")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (assistantState == AssistantState.LISTENING) colors.cyan
                                    else colors.surface2
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Voice Directive",
                                tint = if (assistantState == AssistantState.LISTENING) colors.void else colors.cyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = if (assistantState == AssistantState.LISTENING) "Listening..." else "Tap to Speak Directive",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Voice profile: ${activeVoice.name} · ${userSettings.voiceSpeed}x",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Outlined.GraphicEq,
                        contentDescription = null,
                        tint = colors.cyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Controlled Android Tool Directives
            Text(
                text = "CONTROLLED ANDROID TOOLS",
                style = MagixTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolChip("Open YouTube", Icons.Outlined.Apps) {
                    viewModel.sendUserMessage("Open YouTube")
                }
                ToolChip("System Settings", Icons.Outlined.Settings) {
                    viewModel.sendUserMessage("Launch settings")
                }
                ToolChip("Go Home", Icons.Outlined.Home) {
                    viewModel.sendUserMessage("Go home")
                }
                ToolChip("Inspect Screen", Icons.Outlined.Visibility) {
                    viewModel.sendUserMessage("What is on my screen?")
                }
                ToolChip("Screenshot", Icons.Outlined.CameraAlt) {
                    viewModel.sendUserMessage("Take screenshot")
                }
                ToolChip("Flashlight", Icons.Outlined.FlashlightOn) {
                    viewModel.sendUserMessage("Turn on flashlight")
                }
                ToolChip("Remind Me", Icons.Outlined.Notifications) {
                    viewModel.sendUserMessage("Remind me to study at 7 PM")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Interaction Card
            val lastMsg = chatMessages.lastOrNull()
            if (lastMsg != null) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "RECENT CONVERSATION & TELEMETRY",
                        style = MagixTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(colors.surface1)
                            .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                            .clickable { onNavigate(MagixScreen.CHAT) }
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lastMsg.sender == com.example.domain.model.ChatSender.USER) "YOU" else "MAGIX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastMsg.sender == com.example.domain.model.ChatSender.USER) colors.textSecondary else colors.cyan
                                )
                                Text(
                                    text = "Tap to open chat",
                                    fontSize = 11.sp,
                                    color = colors.textAccent
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = lastMsg.text,
                                fontSize = 13.sp,
                                color = colors.textPrimary,
                                maxLines = 3
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Quick Autonomous Routines
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AUTONOMOUS SUBROUTINES",
                    style = MagixTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = if (automations.isNotEmpty()) automations.first().title else "Custom Routine",
                        subtitle = if (automations.isNotEmpty()) "Execute saved routine" else "Define automation",
                        icon = Icons.Outlined.ElectricBolt,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (automations.isNotEmpty()) {
                                viewModel.executeRoutineNow(automations.first())
                            } else {
                                onNavigate(MagixScreen.AUTOMATIONS)
                            }
                        }
                    )

                    QuickActionCard(
                        title = "Query Memory",
                        subtitle = "Context parameters",
                        icon = Icons.Outlined.Psychology,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(MagixScreen.MEMORY) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val colors = MagixTheme.colors
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surface1)
            .border(1.dp, colors.borderSubtle, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MagixTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.cyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}
