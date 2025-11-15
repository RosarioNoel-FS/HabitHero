package com.example.habithero

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.HeroGold
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

@Composable
fun HabitDetailScreen(
    viewModel: DetailViewModel,
    onBackClick: () -> Unit,
    onEditClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBackClick()
        }
    }

    Scaffold(containerColor = Color.Transparent) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            DetailScreenContent(
                uiState = uiState,
                onBack = onBackClick,
                onEditClick = { habitId -> onEditClick(habitId) },
                onDelete = { viewModel.deleteHabit() },
                onDetach = { viewModel.detachHabitFromChallenge() },
                onComplete = { viewModel.completeHabit() },
                onUpdateReminder = { enabled, minutes -> viewModel.updateReminder(enabled, minutes) },
                onConfettiShown = { viewModel.onConfettiShown() },
                onErrorShown = { viewModel.onErrorShown() }
            )
        }
    }
}

@Composable
fun DetailScreenContent(
    uiState: DetailUiState,
    onBack: () -> Unit,
    onEditClick: (String) -> Unit,
    onDelete: () -> Unit,
    onDetach: () -> Unit,
    onComplete: () -> Unit,
    onUpdateReminder: (Boolean, Int) -> Unit,
    onConfettiShown: () -> Unit,
    onErrorShown: () -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showChallengeDeleteDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    var parties by remember { mutableStateOf<List<Party>>(emptyList()) }
    val habit = uiState.habit
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            onErrorShown()
        }
    }

    val party = remember {
        Party(
            emitter = Emitter(duration = 1500, TimeUnit.MILLISECONDS).perSecond(200),
            position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0)),
            angle = 90,
            spread = 45,
            speed = 15f,
            maxSpeed = 30f,
            damping = 0.9f,
            colors = listOf(0xFFFCE18A.toInt(), 0xFFFF726D.toInt(), 0xFFF4306D.toInt(), 0xFFB48DEF.toInt())
        )
    }

    LaunchedEffect(uiState.confettiEventId) {
        if (uiState.confettiEventId != null) {
            onConfettiShown()
            parties = listOf(party)
            delay(1800)
            parties = emptyList()
        }
    }

    if (uiState.isLoading && habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Habit not found. Please go back.")
        }
        return
    }

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onDelete()
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showChallengeDeleteDialog) {
        ChallengeHabitDeleteDialog(
            onConfirmDetach = {
                onDetach()
                showChallengeDeleteDialog = false
            },
            onConfirmDelete = {
                onDelete()
                showChallengeDeleteDialog = false
            },
            onDismiss = { showChallengeDeleteDialog = false }
        )
    }

    if (showCalendar) {
        ProgressCalendarDialog(habit = habit, onDismiss = { showCalendar = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenHeader(habit = habit, onBack = onBack, onEditClick = { onEditClick(habit.id) })
            StatsRow(habit = habit)
            DeadlineCard(habit = habit)
            ReminderSection(
                reminderEnabled = habit.reminderEnabled,
                reminderMinutes = habit.reminderTimeMinutes,
                onReminderChange = onUpdateReminder
            )
            CompletionCard(habit = habit, onComplete = onComplete)
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showCalendar = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Completion Calendar", color = Color.White)
            }
            QuoteCard()
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (habit.sourceChallengeId != null) {
                        showChallengeDeleteDialog = true
                    } else {
                        showDeleteConfirmDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = Color.Red.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Habit", color = Color.Red.copy(alpha = 0.7f))
            }
        }

        if (parties.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier.fillMaxSize().zIndex(1f),
                parties = parties
            )
        }
    }
}

// --- REMINDER SECTION --- 
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
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
        title = { Text("Alert") },
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
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                HorizontalDivider(color = Color.Gray)
                if (enabled) {
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
                                }
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
                        RadioButton(selected = isCustom, onClick = { isCustom = true })
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
                Text("Done")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = MaterialTheme.colorScheme.surface
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
                RadioButton(selected = currentUnit == "minutes", onClick = { currentUnit = "minutes" })
                Text("minutes", color = Color.White)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.selectable(selected = currentUnit == "hours", onClick = { currentUnit = "hours" })
            ) {
                RadioButton(selected = currentUnit == "hours", onClick = { currentUnit = "hours" })
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


@Composable
fun ChallengeHabitDeleteDialog(
    onConfirmDetach: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remove Challenge Habit?", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(
                    text = "This habit is part of a challenge. You can either remove it from the challenge or keep it as a personal habit.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirmDetach,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = HeroGold)
                ) {
                    Text("Keep as Personal Habit", color = Color.Black)
                }

                OutlinedButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
                ) {
                    Text("Remove from Challenge", color = Color.Red.copy(alpha = 0.7f))
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ScreenHeader(habit: Habit, onBack: () -> Unit, onEditClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onBack()
        }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = habit.iconUrl, contentDescription = "Habit Icon", modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(habit.category, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        if (habit.sourceChallengeId == null) {
            IconButton(onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onEditClick()
            }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Habit", tint = Color.White)
            }
        }
    }
}

@Composable
fun StatsRow(habit: Habit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        DetailStatCard(Modifier.weight(1f), "Day Streak", habit.streakCount.toString(), Icons.Default.LocalFireDepartment, HeroGold)
        DetailStatCard(Modifier.weight(1f), "Total Done", habit.completionCount.toString(), Icons.Default.TrackChanges, Color(0xFF32CD32))
    }
}

@Composable
fun DetailStatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}


@Composable
fun DeadlineCard(habit: Habit) {
    val deadlineTime = remember(habit.completionHour, habit.completionMinute) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, habit.completionHour)
            set(java.util.Calendar.MINUTE, habit.completionMinute)
        }
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        format.format(calendar.time)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Blue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Schedule, "Deadline", tint = Color.Blue, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Daily Deadline: $deadlineTime", fontWeight = FontWeight.Bold, color = Color.White)
                Text("Complete before this time to maintain your streak", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun CompletionCard(habit: Habit, onComplete: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(32.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!habit.isCompletedToday) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    AsyncImage(model = habit.iconUrl, "Habit Icon", modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Ready to Conquer Today?", style = MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
                    Text("Mark this habit as complete when you're done!", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    GradientButton("Mark as Complete") {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete()
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, "Completed", tint = Color(0xFF32CD32), modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Completed Today! 🎉", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text("Amazing work! You've completed this habit for today.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun QuoteCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Text("Every small step counts towards becoming the hero you want to be! 🧑‍🚀", Modifier.padding(16.dp), color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp)
    }
}


// --- Dialogs and Small Components --- //

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Button(
        onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }, 
        shape = RoundedCornerShape(50), 
        modifier = Modifier.fillMaxWidth(0.8f),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), 
        contentPadding = PaddingValues()
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().background(brush = Brush.horizontalGradient(colors = listOf(Color(0xFF5EFF8A), Color(0xFF27A2F8)))).padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Delete Habit", style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Are you sure you want to delete this habit permanently? This action cannot be undone.", color = Color.Gray)
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    }) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onConfirm()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.7f))
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressCalendarDialog(habit: Habit, onDismiss: () -> Unit) {
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    val completedDates = remember(habit.completionDates) {
        habit.completionDates.map { Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
    }
    val haptics = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Calendar", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    IconButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = habit.iconUrl, contentDescription = habit.name, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${habit.completionCount} total completions", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentMonth = currentMonth.minusMonths(1) 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month", tint = Color.White)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { 
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentMonth = currentMonth.plusMonths(1) 
                    }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    for (day in days) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(day, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                val firstDayOfMonth = currentMonth.atDay(1).dayOfWeek.value % 7
                val daysInMonth = currentMonth.lengthOfMonth()

                Column {
                    var dayCounter = 1
                    repeat(6) { weekIndex ->
                        if (dayCounter > daysInMonth) return@repeat
                        Row(modifier = Modifier.fillMaxWidth()) {
                            for (dayIndex in 0..6) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(1f), contentAlignment = Alignment.Center) {
                                    if ((weekIndex == 0 && dayIndex < firstDayOfMonth) || dayCounter > daysInMonth) {
                                        // Empty cell
                                    } else {
                                        val date = currentMonth.atDay(dayCounter)
                                        val isToday = date == LocalDate.now()
                                        val isCompleted = completedDates.contains(date)

                                        var boxModifier = Modifier.clip(RoundedCornerShape(8.dp))
                                        val textColor: Color

                                        if (isToday && isCompleted) {
                                            boxModifier = boxModifier.background(HeroGold)
                                            textColor = Color.Black
                                        } else if (isToday) {
                                            boxModifier = boxModifier.border(BorderStroke(1.dp, HeroGold), RoundedCornerShape(8.dp))
                                            textColor = HeroGold
                                        } else if (isCompleted) {
                                            boxModifier = boxModifier.background(Color(0xFF32CD32).copy(alpha = 0.3f))
                                            textColor = Color.White
                                        } else {
                                            textColor = Color.White.copy(alpha = 0.7f)
                                        }

                                        Box(modifier = boxModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(dayCounter.toString(), color = textColor, fontWeight = FontWeight.Bold)
                                        }
                                        dayCounter++
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    LegendItem(color = Color(0xFF32CD32).copy(alpha = 0.3f), text = "Completed")
                    Spacer(Modifier.width(16.dp))
                    LegendItem(color = HeroGold, text = "Today")
                }
            }
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = Color.Gray, fontSize = 12.sp)
    }
}
