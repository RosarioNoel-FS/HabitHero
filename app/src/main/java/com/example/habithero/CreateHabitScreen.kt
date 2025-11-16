package com.example.habithero

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.HeroGold
import java.text.SimpleDateFormat
import java.util.Locale

// Main Screen Composable
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = viewModel(),
    onHabitCreatedOrUpdated: () -> Unit,
    onBackClick: () -> Unit,
    onCategoryClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Always load current habit data once when this screen is opened
    LaunchedEffect(Unit) {
        viewModel.loadHabitData()
    }

    LaunchedEffect(uiState.isHabitCreatedOrUpdated) {
        if (uiState.isHabitCreatedOrUpdated) {
            onHabitCreatedOrUpdated()
        }
    }

    if (showTimePicker) {
        StyledTimePickerDialog(
            initialHour = uiState.selectedHour,
            initialMinute = uiState.selectedMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.onTimeChanged(hour, minute)
                showTimePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            Row(Modifier.fillMaxWidth()) {
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBackClick()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(it)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (uiState.isEditMode) "Edit Habit" else "Create New Habit", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = uiState.habitName,
                    onValueChange = viewModel::onHabitNameChanged,
                    label = { Text("Habit Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = uiState.habitEmoji,
                    onValueChange = viewModel::onHabitEmojiChanged,
                    label = { Text("Emoji (e.g. 🔥)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                CategorySelector(
                    iconUrl = uiState.iconUrl,
                    categoryName = uiState.selectedCategory,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onCategoryClick()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                val format = SimpleDateFormat("h:mm a", Locale.getDefault())
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, uiState.selectedHour)
                    set(java.util.Calendar.MINUTE, uiState.selectedMinute)
                }
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showTimePicker = true
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, "Clock", tint = HeroGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Deadline: ${format.format(calendar.time)}", color = Color.White)
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                ReminderSection(
                    reminderEnabled = uiState.reminderEnabled,
                    reminderMinutes = uiState.reminderTimeMinutes,
                    onReminderChange = viewModel::onReminderChanged
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.saveHabit() 
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (uiState.isEditMode) "Save Changes" else "Create Habit")
                }
            }
        }
    }
}

// --- NEW REMINDER IMPLEMENTATION ---

private fun formatReminderMinutes(minutes: Int): String {
    return when {
        minutes == 0 -> "At time of event"
        minutes < 60 -> "$minutes minutes before"
        minutes % 60 == 0 -> {
            val hours = minutes / 60
            if (hours == 1) "1 hour before" else "$hours hours before"
        }
        else -> {
            val hours = minutes / 60
            val remainingMinutes = minutes % 60
            "$hours hr $remainingMinutes min before"
        }
    }
}

@Composable
private fun ReminderSection(
    reminderEnabled: Boolean,
    reminderMinutes: Int,
    onReminderChange: (Boolean, Int) -> Unit
) {
    var showReminderDialog by remember { mutableStateOf(false) }

    if (showReminderDialog) {
        ReminderSettingsDialog(
            initialEnabled = reminderEnabled,
            initialMinutes = reminderMinutes,
            onDismiss = { showReminderDialog = false },
            onConfirm = { enabled, minutes ->
                onReminderChange(enabled, minutes)
                showReminderDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showReminderDialog = true },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, "Reminders", tint = Color.White)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Alert", fontWeight = FontWeight.Bold, color = Color.White)
                    if (reminderEnabled) {
                        Text(formatReminderMinutes(reminderMinutes), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Off", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Change Alert", tint = Color.Gray)
        }
    }
}

@Composable
private fun ReminderSettingsDialog(
    initialEnabled: Boolean,
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Boolean, Int) -> Unit
) {
    var enabled by remember { mutableStateOf(initialEnabled) }
    var minutes by remember { mutableIntStateOf(initialMinutes) }

    val presetOptions = listOf(0, 10, 60)
    var isCustom by remember { mutableStateOf(initialMinutes !in presetOptions) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Alert", color = Color.White) },
        text = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("On", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = enabled, onCheckedChange = { enabled = it }, colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = HeroGold,
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    ))
                }
                HorizontalDivider(color = Color.Gray)
                if (enabled) {
                    val radioColors = RadioButtonDefaults.colors(
                        selectedColor = HeroGold,
                        unselectedColor = Color.Gray
                    )
                    presetOptions.forEach { preset ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = (minutes == preset && !isCustom), onClick = {
                                    minutes = preset
                                    isCustom = false
                                })
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (minutes == preset && !isCustom),
                                onClick = { 
                                    minutes = preset
                                    isCustom = false
                                },
                                colors = radioColors
                            )
                            Text(formatReminderMinutes(preset), color = Color.White, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = isCustom, onClick = { isCustom = true })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = isCustom, onClick = { isCustom = true }, colors = radioColors)
                        Text("Custom", color = Color.White, modifier = Modifier.padding(start = 8.dp))
                    }
                    if (isCustom) {
                        CustomReminderPicker(initialMinutes = minutes, onMinutesChange = { minutes = it })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(enabled, minutes) }) {
                Text("Done", color = HeroGold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color.White) } },
        containerColor = Color(0xFF1F2937),
        tonalElevation = 0.dp
    )
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CustomReminderPicker(
    initialMinutes: Int,
    onMinutesChange: (Int) -> Unit
) {
    val numbers = (0..59).map { it.toString() }

    val (initialNumber, initialUnit) = remember(initialMinutes) {
        if (initialMinutes >= 60 && initialMinutes % 60 == 0) {
            (initialMinutes / 60) to "hours"
        } else {
            initialMinutes to "minutes"
        }
    }

    var currentNumber by remember { mutableIntStateOf(initialNumber) }
    var currentUnit by remember { mutableStateOf(initialUnit) }

    val numberState = rememberLazyListState(initialFirstVisibleItemIndex = numbers.indexOf(currentNumber.toString()).coerceAtLeast(0))
    val finalNumberIndex by remember { derivedStateOf { if (numberState.isScrollInProgress) -1 else numberState.firstVisibleItemIndex } }

    val radioColors = RadioButtonDefaults.colors(
        selectedColor = HeroGold,
        unselectedColor = Color.Gray
    )

    LaunchedEffect(finalNumberIndex) {
        if (finalNumberIndex != -1) {
            currentNumber = finalNumberIndex
        }
    }

    LaunchedEffect(currentNumber, currentUnit) {
        val totalMinutes = if (currentUnit == "hours") currentNumber * 60 else currentNumber
        onMinutesChange(totalMinutes)
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScrollableNumberPicker(items = numbers, state = numberState)
        
        Spacer(Modifier.width(16.dp))

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(selected = currentUnit == "minutes", onClick = { currentUnit = "minutes" })
            ) {
                RadioButton(selected = currentUnit == "minutes", onClick = { currentUnit = "minutes" }, colors = radioColors)
                Text("minutes", color = Color.White)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(selected = currentUnit == "hours", onClick = { currentUnit = "hours" })
            ) {
                RadioButton(selected = currentUnit == "hours", onClick = { currentUnit = "hours" }, colors = radioColors)
                Text("hours", color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScrollableNumberPicker(
    items: List<String>,
    state: androidx.compose.foundation.lazy.LazyListState,
) {
    val itemHeight = 48.dp
    val snappingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    val centralIndex by remember { derivedStateOf { state.firstVisibleItemIndex } }

    LazyColumn(
        state = state,
        flingBehavior = snappingBehavior,
        modifier = Modifier
            .height(itemHeight * 3)
            .width(100.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(items.size) { index ->
            Text(
                text = items[index],
                style = if (index == centralIndex) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyLarge,
                color = if (index == centralIndex) Color.White else Color.Gray,
                modifier = Modifier.padding(vertical = (itemHeight - MaterialTheme.typography.titleLarge.lineHeight.value.dp)/2)
            )
        }
    }
}


// --- COMMON AND PREVIOUSLY EXISTING COMPOSABLES (UNMODIFIED) ---

@Composable
fun CategorySelector(iconUrl: String, categoryName: String, onClick: () -> Unit) {
    Box(modifier = Modifier.clickable(onClick = onClick)) {
        OutlinedTextField(
            value = if (categoryName.isNotBlank()) categoryName else "",
            onValueChange = {},
            placeholder = { Text("Select a category", color = Color.Gray) },
            label = { Text("Category") },
            leadingIcon = {
                if (iconUrl.isNotBlank()) {
                    AsyncImage(
                        model = iconUrl,
                        contentDescription = "Category Icon",
                        modifier = Modifier.size(32.dp).padding(start = 8.dp)
                    )
                }
            },
            trailingIcon = {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Select Category")
            },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = false, // To allow click on parent Box
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.White,
                disabledLabelColor = Color.Gray,
                disabledBorderColor = Color.Gray,
                disabledLeadingIconColor = Color.White,
                disabledTrailingIconColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                disabledPlaceholderColor = Color.Gray
            )
        )
    }
}

@Composable
private fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White,
    focusedBorderColor = Color.White,
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color.White,
    unfocusedLabelColor = Color.Gray,
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledTextColor = Color.Gray,
    disabledLabelColor = Color.Gray
)
