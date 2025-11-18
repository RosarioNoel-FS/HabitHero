package com.example.habithero

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.HeroGold

@Composable
fun HabitSelectionScreen(
    viewModel: HabitSelectionViewModel = viewModel(),
    onHabitAdded: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var habitForTimePicker by remember { mutableStateOf<Habit?>(null) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(uiState.habitAdded) {
        if (uiState.habitAdded) {
            onHabitAdded()
        }
    }

    if (habitForTimePicker != null) {
        StyledTimePickerDialog(
            initialHour = 21, // Default to 9 PM
            initialMinute = 0,
            onDismiss = { habitForTimePicker = null },
            onConfirm = { hour, minute ->
                viewModel.addHabit(habitForTimePicker!!, hour, minute)
                habitForTimePicker = null
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(8.dp)) {
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onBackClick()
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (uiState.category != null) {
                    AsyncImage(model = uiState.category!!.iconUrl, contentDescription = "Category Icon", modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(uiState.category!!.name, style = typography.titleLarge, color = Color.White)
                        Text("Choose a habit to start building", style = typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(horizontal = 16.dp).fillMaxSize()) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.error != null) {
                 Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = uiState.error!!)
                }
            }else {
                Text("Available Habits (${uiState.habitList.size})", style = typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.habitList) { habit ->
                        AvailableHabitRow(habit = habit, onAddClick = { 
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            habitForTimePicker = habit 
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun AvailableHabitRow(habit: Habit, onAddClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(HeroGold),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(model = habit.iconUrl, contentDescription = "Habit Icon", modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = "Deadline", tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Set your daily deadline", color = Color.Gray, fontSize = 12.sp)
                }
            }

            IconButton(onClick = { 
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddClick() 
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = Color.White)
            }
        }
    }
}
