package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.CapabilityState
import com.example.domain.model.SystemCapability
import com.example.ui.theme.MagixTheme

@Composable
fun PermissionCenterDialog(
    capabilities: List<SystemCapability>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val colors = MagixTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("permission_center_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface1,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Security,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Permission Center",
                                style = MagixTheme.typography.titleMedium,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Real Android System Telemetry",
                                style = MagixTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.surface2)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MAGIX requires explicit user authorization for hardware and accessibility control. Capabilities are only activated when granted in Android OS settings.",
                    style = MagixTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Capabilities List
                capabilities.forEach { cap ->
                    CapabilityItem(
                        capability = cap,
                        onOpenSettings = {
                            openSetting(context, cap.settingsAction)
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CapabilityItem(
    capability: SystemCapability,
    onOpenSettings: () -> Unit
) {
    val colors = MagixTheme.colors
    val isGranted = capability.state == CapabilityState.CONNECTED

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface0)
            .border(
                1.dp,
                if (isGranted) colors.success.copy(alpha = 0.4f) else colors.borderSubtle,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = capability.title,
                style = MagixTheme.typography.titleSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            // Status Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isGranted) colors.success.copy(alpha = 0.15f) else colors.textMuted.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = colors.success,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (isGranted) "AUTHORIZED" else "DISCONNECTED",
                    style = MagixTheme.typography.labelSmall,
                    color = if (isGranted) colors.success else colors.textMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = capability.description,
            style = MagixTheme.typography.bodySmall,
            color = colors.textSecondary
        )

        if (capability.whatItDoes.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Purpose: ${capability.whatItDoes}",
                style = MagixTheme.typography.labelSmall,
                color = colors.textMuted,
                fontSize = 11.sp
            )
        }

        if (capability.howToEnable.isNotBlank() && !isGranted) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "• Setup: ${capability.howToEnable}",
                style = MagixTheme.typography.labelSmall,
                color = colors.warning,
                fontSize = 11.sp
            )
        }

        if (!isGranted && capability.settingsAction != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.primary.copy(alpha = 0.12f))
                    .clickable { onOpenSettings() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Configure in Android Settings",
                    style = MagixTheme.typography.labelSmall,
                    color = colors.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun openSetting(context: Context, action: String?) {
    try {
        val targetAction = action ?: Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val intent = Intent(targetAction).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (targetAction == Settings.ACTION_APPLICATION_DETAILS_SETTINGS ||
                targetAction == Settings.ACTION_MANAGE_OVERLAY_PERMISSION) {
                data = "package:${context.packageName}".toUri()
            }
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        // Fallback to application details
        try {
            val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (_: Exception) {}
    }
}
