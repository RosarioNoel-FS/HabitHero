package com.example.habithero

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

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
                    Icon(Icons.Default.Schedule, "Clock", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Daily Deadline: ${format.format(calendar.time)}", color = Color.White)
                }

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
