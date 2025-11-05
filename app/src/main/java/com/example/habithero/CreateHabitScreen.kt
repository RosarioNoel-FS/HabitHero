package com.example.habithero

import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    viewModel: CreateHabitViewModel = viewModel(),
    onHabitCreatedOrUpdated: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

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
                IconButton(onClick = onBackClick) {
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

                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !uiState.isEditMode && !showCategoryDropdown }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        value = uiState.selectedCategory,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        colors = customTextFieldColors(),
                        enabled = !uiState.isEditMode
                    )
                    ExposedDropdownMenu(
                        expanded = !uiState.isEditMode && showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        uiState.categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category, color = Color.White) },
                                onClick = {
                                    viewModel.onCategoryChanged(category)
                                    showCategoryDropdown = false
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                val format = SimpleDateFormat("h:mm a", Locale.getDefault())
                val calendar = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, uiState.selectedHour)
                    set(java.util.Calendar.MINUTE, uiState.selectedMinute)
                }
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Schedule, "Clock", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Deadline: ${format.format(calendar.time)}", color = Color.White)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.saveHabit() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(if (uiState.isEditMode) "Save Changes" else "Create Habit")
                }
            }
        }
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

@Composable
fun StyledTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialHour) }
    var selectedMinute by remember { mutableStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Set Daily Deadline", style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))

                Text("Choose when you want to complete this habit each day. If you don\'t complete it by this time, your streak will reset.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)

                Spacer(Modifier.height(16.dp))

                AndroidView(
                    factory = { context ->
                        TimePicker(context).apply {
                            setIs24HourView(false)
                            hour = selectedHour
                            minute = selectedMinute
                            setOnTimeChangedListener { _, hourOfDay, minuteOfHour ->
                                selectedHour = hourOfDay
                                selectedMinute = minuteOfHour
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedHour, selectedMinute) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD600)) // Hero Gold
                    ) {
                        Text("Confirm", color = Color.Black)
                    }
                }
            }
        }
    }
}
