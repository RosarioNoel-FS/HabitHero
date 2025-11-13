package com.example.habithero

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
                onUpdateReminder = { viewModel.updateReminder(it) },
                onUpdateReminderTime = { viewModel.updateReminderTime(it) },
                onConfettiShown = { viewModel.onConfettiShown() } // Pass the event handler
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
    onUpdateReminder: (Boolean) -> Unit,
    onUpdateReminderTime: (Int) -> Unit,
    onConfettiShown: () -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showChallengeDeleteDialog by remember { mutableStateOf(false) }
    var showReminderTimeDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    var parties by remember { mutableStateOf<List<Party>>(emptyList()) }
    val habit = uiState.habit

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

    // This is the guaranteed working fix for the confetti animation.
    // It uses a one-shot event from the ViewModel to trigger the animation,
    // ensuring it works reliably every time without race conditions.
    LaunchedEffect(uiState.confettiEventId) {
        if (uiState.confettiEventId != null) {
            // First, consume the event. This prevents it from firing again on screen rotation.
            onConfettiShown()

            // Then, trigger the local animation.
            parties = listOf(party)
            delay(1800) // Match the animation duration
            parties = emptyList()
        }
    }

    if (uiState.isLoading && habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error)
        }
        return
    }

    if (habit == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Habit not found.")
        }
        return
    }

    if (showReminderTimeDialog) {
        ReminderTimeDialog(
            onDismiss = { showReminderTimeDialog = false },
            onTimeSelected = { minutes ->
                onUpdateReminderTime(minutes)
                showReminderTimeDialog = false
            }
        )
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
            ReminderCard(habit = habit, onUpdateReminder = onUpdateReminder, onTimeClicked = { showReminderTimeDialog = true })
            CompletionCard(habit = habit, onComplete = onComplete)
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    showCalendar = true
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = colorScheme.surface.copy(alpha = 0.5f))
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
                colors = ButtonDefaults.outlinedButtonColors(containerColor = colorScheme.surface.copy(alpha = 0.5f))
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

@Composable
fun ChallengeHabitDeleteDialog(
    onConfirmDetach: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Remove Challenge Habit?", style = typography.titleLarge, color = Color.White)
                Text(
                    text = "This habit is part of a challenge. You can either remove it from the challenge or keep it as a personal habit.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    style = typography.bodyMedium
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
fun ReminderTimeDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (Int) -> Unit
) {
    val timeOptions = listOf(5, 10, 15, 30, 60)
    val haptics = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Remind Me Before Deadline", style = typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                timeOptions.forEach { minutes ->
                    TextButton(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTimeSelected(minutes)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("$minutes minutes before", color = HeroGold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}


@Composable
fun ReminderCard(habit: Habit, onUpdateReminder: (Boolean) -> Unit, onTimeClicked: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = habit.reminderEnabled, onClick = onTimeClicked)
            ) {
                Icon(Icons.Default.Notifications, "Reminder", tint = HeroGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Reminder Notifications", fontWeight = FontWeight.Bold, color = Color.White)
                    if (habit.reminderEnabled) {
                        Text("${habit.reminderTimeMinutes} minutes before deadline", fontSize = 14.sp, color = HeroGold)
                    } else {
                        Text("Get notified when it's time for this habit", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = habit.reminderEnabled,
                onCheckedChange = onUpdateReminder
            )
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
                .background(colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(model = habit.iconUrl, contentDescription = "Habit Icon", modifier = Modifier.size(28.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(habit.name, style = typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Text(habit.category, style = typography.bodyMedium, color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
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
                Text(text = value, style = typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
                Text(text = label, style = typography.bodyMedium, color = Color.Gray)
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
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
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
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.padding(32.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!habit.isCompletedToday) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    AsyncImage(model = habit.iconUrl, "Habit Icon", modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Ready to Conquer Today?", style = typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
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
                    Text("Completed Today! 🎉", style = typography.titleLarge, color = Color.White)
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
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.3f)),
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
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Delete Habit", style = typography.titleLarge, color = Color.White)
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
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Calendar", style = typography.titleLarge, color = Color.White)
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
