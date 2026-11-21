package com.example.ui.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AccessibilityNew
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.domain.model.MagixVoiceProfile
import com.example.domain.model.ThemeMode
import com.example.domain.model.VoiceSpeed
import com.example.ui.MagixViewModel
import com.example.ui.components.DevMockBadge
import com.example.ui.theme.MagixTheme

@Composable
fun SettingsScreen(
    viewModel: MagixViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val userSettings by viewModel.userSettings.collectAsState()
    val colors = MagixTheme.colors
    val scrollState = rememberScrollState()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var showEditAddressDialog by remember { mutableStateOf(false) }

    fun checkMicPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkNotifPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    // Real system permission states
    var micGranted by remember { mutableStateOf(checkMicPermission()) }
    var notifGranted by remember { mutableStateOf(checkNotifPermission()) }

    // Synchronize permission states on ON_RESUME lifecycle event (e.g. after returning from OS settings or app relaunch)
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                micGranted = checkMicPermission()
                notifGranted = checkNotifPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Check system overlay permission
    val canDrawOverlays = remember(userSettings) {
        Settings.canDrawOverlays(context)
    }

    val micLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micGranted = granted || checkMicPermission()
    }

    val notifLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notifGranted = granted || checkNotifPermission()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface0)
            .statusBarsPadding()
            .testTag("magix_settings_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 96.dp)
        ) {
            // Header
            Text(
                text = "System Settings",
                style = MagixTheme.typography.titleLarge,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Theme Persistence, Voice System & Live Presence Configuration",
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))
            DevMockBadge(text = "Phase 1 Parameters · Preferences Persist Locally Across Sessions")
            Spacer(modifier = Modifier.height(20.dp))

            // Section: User Identity & Form of Address
            SectionHeader(title = "USER IDENTITY & FORM OF ADDRESS")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Display Name
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditNameDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Display Name",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = userSettings.displayName,
                                fontSize = 13.sp,
                                color = colors.cyan
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Name",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.border)
                    )

                    // Form of Address (Default: "Boss")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showEditAddressDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Assistant Form of Address",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "MAGIX addresses you as: \"${userSettings.formOfAddress}\"",
                                fontSize = 12.sp,
                                color = colors.cyan
                            )
                        }

                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Form of Address",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Theme & Appearance (Persisted immediately across app launch)
            SectionHeader(title = "THEME & VISUAL IDENTITY (PERSISTED)")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = userSettings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) colors.surface2 else colors.surface1)
                            .border(
                                1.dp,
                                if (isSelected) colors.cyan else colors.border,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(14.dp)
                            .testTag("theme_option_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) colors.cyan else colors.surface3),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (mode) {
                                            ThemeMode.DARK -> Icons.Outlined.DarkMode
                                            ThemeMode.LIGHT -> Icons.Outlined.LightMode
                                            ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) colors.void else colors.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column {
                                    Text(
                                        text = mode.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = mode.description,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(colors.cyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = colors.void,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: MAGIX Live Presence & Overlay Architecture
            SectionHeader(title = "MAGIX LIVE PRESENCE & OVERLAY ARCHITECTURE")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // In-App Live Presence Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "In-App Live Presence Pill",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Dynamic top-of-screen presence with cutout awareness",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        Switch(
                            checked = userSettings.livePresenceEnabled,
                            onCheckedChange = { viewModel.toggleLivePresence(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = colors.cyan,
                                checkedTrackColor = colors.surface2,
                                uncheckedThumbColor = colors.textTertiary,
                                uncheckedTrackColor = colors.surface2
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(colors.border)
                    )

                    // System Overlay Layer & Fallback Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "System Overlay Status",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (canDrawOverlays) colors.greenSuccess else colors.amberWarm)
                                )
                            }
                            Text(
                                text = if (canDrawOverlays)
                                    "Preferred Mode: Overlay capability enabled on device"
                                else
                                    "Graceful Fallback Mode: Top-of-screen presence & notification fallback",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (!canDrawOverlays) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.cyan.copy(alpha = 0.15f))
                                    .border(1.dp, colors.cyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            "package:${context.packageName}".toUri()
                                        )
                                        context.startActivity(intent)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Grant Overlay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.cyan
                                )
                            }
                        }
                    }

                    if (canDrawOverlays) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(colors.border)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Floating Dynamic Island Pill",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Displays on top when minimized · Expands into mini-app with 'Hey, Magix!'",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(colors.cyan.copy(alpha = 0.15f))
                                    .border(1.dp, colors.cyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                    .clickable {
                                        com.example.service.MagixOverlayService.start(context)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Test Floating Pill",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.cyan
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Voice System
            SectionHeader(title = "VOICE SYSTEM (NEURAL PROFILES)")

            // Male Voice Option (Puck)
            VoiceOptionCard(
                profile = MagixVoiceProfile.PUCK_MALE,
                isSelected = userSettings.selectedVoiceId == MagixVoiceProfile.PUCK_MALE.internalId,
                onSelect = { viewModel.selectVoice(MagixVoiceProfile.PUCK_MALE) },
                onAudition = { viewModel.auditionVoice(MagixVoiceProfile.PUCK_MALE) }
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Female Voice Option (Kore)
            VoiceOptionCard(
                profile = MagixVoiceProfile.KORE_FEMALE,
                isSelected = userSettings.selectedVoiceId == MagixVoiceProfile.KORE_FEMALE.internalId,
                onSelect = { viewModel.selectVoice(MagixVoiceProfile.KORE_FEMALE) },
                onAudition = { viewModel.auditionVoice(MagixVoiceProfile.KORE_FEMALE) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Voice Speed Selector
            Text(
                text = "Voice Synthesis Speed",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VoiceSpeed.entries.forEach { speedItem ->
                    val isCurrent = kotlin.math.abs(userSettings.voiceSpeed - speedItem.speed) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isCurrent) colors.surface2 else colors.surface1)
                            .border(
                                1.dp,
                                if (isCurrent) colors.cyan else colors.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setVoiceSpeed(speedItem) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = speedItem.label,
                            fontSize = 12.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) colors.cyan else colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Permissions & Capabilities
            SectionHeader(title = "SYSTEM PERMISSIONS & CAPABILITIES")
            PermissionStatusRow(
                title = "Microphone Telemetry",
                description = "Voice recognition and acoustic prompt intake",
                isConnected = micGranted,
                onConnectClick = { micLauncher.launch(Manifest.permission.RECORD_AUDIO) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            PermissionStatusRow(
                title = "Proactive Notifications",
                description = "Routine completion alerts and status feeds",
                isConnected = notifGranted,
                onConnectClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            DeferredFeatureRow(
                title = "Android Accessibility Service",
                description = "Tapping, scrolling, and operating OS applications",
                statusText = "Contract Ready"
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Direct link to Android App Settings
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                    .clickable {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = "package:${context.packageName}".toUri()
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Android System App Settings",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Manage OS permissions, auto-reset settings, and background battery access",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surface2)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Open OS Settings",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.cyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: Privacy & Local Storage
            SectionHeader(title = "PRIVACY & LOCAL STORAGE")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Local Memory Database",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Stored in secure local SQLite on-device",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface2)
                                .clickable { viewModel.clearAllMemories() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Reset Memories", fontSize = 11.sp, color = colors.redError)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Conversation Cache",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Temporary stream buffer",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface2)
                                .clickable { viewModel.clearChat() }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text("Clear Chat", fontSize = 11.sp, color = colors.textSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Section: About
            SectionHeader(title = "APPLICATION VERSION & UPDATES")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(colors.surface1)
                    .border(1.dp, colors.border, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "App Status", fontSize = 13.sp, color = colors.textSecondary)
                            Text(text = "Latest Build Installed", fontSize = 12.sp, color = colors.greenSuccess, fontWeight = FontWeight.SemiBold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.greenSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "UP TO DATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.greenSuccess)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Installed Version", fontSize = 13.sp, color = colors.textSecondary)
                        Text(text = "v1.1.0 (Build 2)", fontSize = 13.sp, color = colors.cyan, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Update Delivery", fontSize = 13.sp, color = colors.textSecondary)
                        Text(text = "AI Studio Live Stream & APK", fontSize = 13.sp, color = colors.textPrimary)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Architecture", fontSize = 13.sp, color = colors.textSecondary)
                        Text(text = "Native Kotlin Compose & Room", fontSize = 13.sp, color = colors.textPrimary)
                    }
                }
            }
        }

        // Edit Display Name Dialog
        if (showEditNameDialog) {
            var tempName by remember { mutableStateOf(userSettings.displayName) }

            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                containerColor = colors.surface1,
                title = {
                    Text(
                        text = "Configure Display Name",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Display Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.cyan,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateDisplayName(tempName)
                            showEditNameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cyan,
                            contentColor = colors.void
                        )
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                }
            )
        }

        // Edit Form of Address Dialog
        if (showEditAddressDialog) {
            var tempAddress by remember { mutableStateOf(userSettings.formOfAddress) }
            val commonAddresses = listOf("Boss", "Commander", "Sir", "Madam")

            AlertDialog(
                onDismissRequest = { showEditAddressDialog = false },
                containerColor = colors.surface1,
                title = {
                    Text(
                        text = "Form of Address",
                        color = colors.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "How MAGIX addresses you during natural voice invocation:",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonAddresses.forEach { addr ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (tempAddress == addr) colors.cyan else colors.surface2)
                                        .clickable { tempAddress = addr }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = addr,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (tempAddress == addr) colors.void else colors.textPrimary
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = tempAddress,
                            onValueChange = { tempAddress = it },
                            label = { Text("Custom Address") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = colors.cyan,
                                unfocusedBorderColor = colors.border,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.updateFormOfAddress(tempAddress)
                            showEditAddressDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.cyan,
                            contentColor = colors.void
                        )
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditAddressDialog = false }) {
                        Text("Cancel", color = colors.textSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    val colors = MagixTheme.colors
    Text(
        text = title,
        style = MagixTheme.typography.labelSmall,
        color = colors.textTertiary
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun VoiceOptionCard(
    profile: MagixVoiceProfile,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onAudition: () -> Unit
) {
    val colors = MagixTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (isSelected) colors.surface2 else colors.surface1)
            .border(
                1.dp,
                if (isSelected) colors.cyan else colors.border,
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onSelect)
            .padding(16.dp)
            .testTag("voice_card_${profile.internalId.lowercase()}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) colors.cyan else colors.surface3),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.RecordVoiceOver,
                            contentDescription = null,
                            tint = if (isSelected) colors.void else colors.cyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = profile.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Internal Voice ID: ${profile.internalId}",
                            fontSize = 11.sp,
                            color = colors.cyan
                        )
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(colors.cyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = "Selected",
                            tint = colors.void,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Personality: ${profile.personality}",
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Audition button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface3)
                    .clickable(onClick = onAudition)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = null,
                        tint = colors.textAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Preview Voice Quote",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionStatusRow(
    title: String,
    description: String,
    isConnected: Boolean,
    onConnectClick: () -> Unit
) {
    val colors = MagixTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isConnected) colors.greenSuccess.copy(alpha = 0.15f) else colors.surface2)
                    .clickable(enabled = !isConnected, onClick = onConnectClick)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = if (isConnected) "Connected" else "Connect",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isConnected) colors.greenSuccess else colors.cyan
                )
            }
        }
    }
}

@Composable
private fun DeferredFeatureRow(
    title: String,
    description: String,
    statusText: String
) {
    val colors = MagixTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface1)
            .border(1.dp, colors.border, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surface2)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textTertiary
                )
            }
        }
    }
}
