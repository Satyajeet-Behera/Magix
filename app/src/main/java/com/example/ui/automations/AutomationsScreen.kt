package com.example.ui.automations

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoMode
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.domain.model.AutomationRoutine
import com.example.ui.MagixViewModel
import com.example.ui.theme.MagixTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationsScreen(
    viewModel: MagixViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.automations.collectAsState()
    val colors = MagixTheme.colors

    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateAutomationDialog(
            onDismiss = { showCreateDialog = false },
            onSave = { title, desc, schedule, category, actions ->
                viewModel.createAutomation(title, desc, schedule, category, actions)
                showCreateDialog = false
            }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.surface0)
            .statusBarsPadding()
            .testTag("magix_automations_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp)
        ) {
            // Screen Header & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Automations & Subroutines",
                        style = MagixTheme.typography.titleLarge,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${routines.size} persistent routines in Room SQLite",
                        fontSize = 12.sp,
                        color = colors.cyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (routines.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clearAllAutomations() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All Automations",
                                tint = colors.textSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Add Routine Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.cyan.copy(alpha = 0.15f))
                            .border(1.dp, colors.cyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { showCreateDialog = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("add_automation_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = colors.cyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "New Routine",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.cyan
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (routines.isEmpty()) {
                // Empty Clean Slate State
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("automations_empty_state"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(colors.surface1)
                                .border(1.dp, colors.cyan.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AutoMode,
                                contentDescription = null,
                                tint = colors.cyan,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "No Subroutines Configured",
                            style = MagixTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Your automation engine is completely clean. Create your own custom routines with triggers, schedules, and multi-step actions. All changes will be saved permanently in your device.",
                            style = MagixTheme.typography.bodySmall,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.cyan
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("empty_create_routine_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = colors.void,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Subroutine",
                                color = colors.void,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Routines List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("automations_list"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(routines, key = { it.id }) { routine ->
                        AutomationCard(
                            routine = routine,
                            onToggle = { isChecked ->
                                viewModel.toggleAutomation(routine.id, isChecked)
                            },
                            onRunNow = {
                                viewModel.executeRoutineNow(routine)
                            },
                            onDelete = {
                                viewModel.deleteAutomation(routine.id)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AutomationCard(
    routine: AutomationRoutine,
    onToggle: (Boolean) -> Unit,
    onRunNow: () -> Unit,
    onDelete: () -> Unit
) {
    val colors = MagixTheme.colors
    val timeFormat = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface1)
            .border(1.dp, colors.border, RoundedCornerShape(20.dp))
            .padding(18.dp)
            .testTag("routine_card_${routine.id}")
    ) {
        Column {
            // Header row with category pill, title, and toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surface2)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = routine.category.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.cyan
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = routine.title,
                        style = MagixTheme.typography.titleMedium,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete Routine",
                            tint = colors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = routine.isEnabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.void,
                            checkedTrackColor = colors.cyan,
                            uncheckedThumbColor = colors.textSecondary,
                            uncheckedTrackColor = colors.surface2
                        ),
                        modifier = Modifier.testTag("toggle_routine_${routine.id}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = routine.description,
                style = MagixTheme.typography.bodySmall,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Pills Flow
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                routine.actionsSummary.forEach { action ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.surface2)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = action,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom row: Schedule + Execute Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = routine.schedule,
                        fontSize = 11.sp,
                        color = colors.textTertiary
                    )
                }

                // Run Now Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surface2)
                        .border(1.dp, colors.cyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onRunNow)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("run_routine_btn_${routine.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Execute Subroutine",
                            tint = colors.cyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Run Now",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.cyan
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateAutomationDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, schedule: String, category: String, actions: List<String>) -> Unit
) {
    val colors = MagixTheme.colors

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf("Daily at 08:00 AM") }
    var category by remember { mutableStateOf("Daily") }
    var customActionInput by remember { mutableStateOf("") }
    var actionsList by remember { mutableStateOf(listOf("System Diagnostic", "Check Weather", "Voice Greeting")) }

    val categories = listOf("Daily", "Focus", "Work", "Home", "Custom")
    val schedulePresets = listOf("Daily at 08:00 AM", "Daily at 10:00 PM", "Voice Command", "Every Weekday")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("create_automation_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = colors.surface1,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
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
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(colors.cyan.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ElectricBolt,
                                contentDescription = null,
                                tint = colors.cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "New Subroutine",
                            style = MagixTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
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

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Subroutine Title", color = colors.textMuted) },
                    placeholder = { Text("e.g., Morning Briefing, Deep Work", color = colors.textMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_automation_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description Input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description", color = colors.textMuted) },
                    placeholder = { Text("What this subroutine executes...", color = colors.textMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "CATEGORY",
                    style = MagixTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = cat == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) colors.cyan.copy(alpha = 0.2f) else colors.surface2)
                                .border(1.dp, if (isSelected) colors.cyan else colors.borderSubtle, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.cyan else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Schedule Input & Presets
                Text(
                    text = "TRIGGER / SCHEDULE",
                    style = MagixTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = schedule,
                    onValueChange = { schedule = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.cyan,
                        unfocusedBorderColor = colors.borderSubtle,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    schedulePresets.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(colors.surface2)
                                .clickable { schedule = preset }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = preset,
                                fontSize = 10.sp,
                                color = colors.cyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions Sequence
                Text(
                    text = "ACTIONS SEQUENCE",
                    style = MagixTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    actionsList.forEachIndexed { index, act ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(colors.surface2)
                                .border(1.dp, colors.borderSubtle, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "${index + 1}. $act", fontSize = 11.sp, color = colors.textPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove action",
                                    tint = colors.textMuted,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable {
                                            actionsList = actionsList.filterIndexed { i, _ -> i != index }
                                        }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customActionInput,
                        onValueChange = { customActionInput = it },
                        placeholder = { Text("Add action step...", color = colors.textMuted, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.cyan,
                            unfocusedBorderColor = colors.borderSubtle,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.cyan.copy(alpha = 0.2f))
                            .clickable {
                                if (customActionInput.isNotBlank()) {
                                    actionsList = actionsList + customActionInput.trim()
                                    customActionInput = ""
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp)
                    ) {
                        Text("Add", color = colors.cyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save & Cancel Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.borderSubtle)
                    ) {
                        Text("Cancel", color = colors.textSecondary)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onSave(title, description, schedule, category, actionsList)
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_automation_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.cyan)
                    ) {
                        Text("Save Routine", color = colors.void, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
