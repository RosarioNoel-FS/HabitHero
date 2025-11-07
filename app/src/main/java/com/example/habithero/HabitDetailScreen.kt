package com.example.habithero

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.HeroGold
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay

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

    when {
        uiState.isLoading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        uiState.error != null -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.error!!)
            }
        }
        uiState.habit != null -> {
            DetailScreenContent(
                habit = uiState.habit!!,
                onBack = onBackClick,
                onEditClick = { onEditClick(uiState.habit!!.id) },
                onDelete = { viewModel.deleteHabit() },
                onComplete = { viewModel.completeHabit() }
            )
        }
    }
}

@Composable
fun DetailScreenContent(
    habit: Habit,
    onBack: () -> Unit,
    onEditClick: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    var showCalendar by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    // Holds the list of Party configurations for KonfettiView. When this list
    // is non‑empty, the confetti animation runs. We also define a single
    // Party instance to reuse across recompositions. This configuration is
    // inspired by the official Konfetti Compose samples【676325972536257†L415-L441】.
    var parties by remember { mutableStateOf<List<Party>>(emptyList()) }

    // Predefine the party with sensible defaults. These settings control the
    // emitter duration, particle rate, position, movement, and colors. Using
    // remember ensures the same Party is reused without recreation on
    // recomposition.
    val party = remember {
        Party(
            emitter = Emitter(duration = 1500, TimeUnit.MILLISECONDS).perSecond(200),
            position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0)),
            angle = 90,
            spread = 45,
            speed = 15f,
            maxSpeed = 30f,
            damping = 0.9f,
            colors = listOf(
                0xFFFCE18A.toInt(),
                0xFFFF726D.toInt(),
                0xFFF4306D.toInt(),
                0xFFB48DEF.toInt()
            )
        )
    }

    /**
     * Custom completion handler that triggers confetti whenever the habit is
     * marked as complete. We set the `parties` state first to start the
     * animation, then call onComplete() to update the habit. This order
     * prevents recomposition from clearing the confetti state before it
     * appears on screen.
     */
    val completeWithConfetti: () -> Unit = {
        parties = listOf(party)
        onComplete()
    }

    // Automatically clear the confetti after it has finished playing so that
    // subsequent taps can retrigger the animation. This effect runs every
    // time `parties` changes. When the list is non‑empty, we wait slightly
    // longer than the emitter duration and then reset it to empty.
    LaunchedEffect(parties) {
        if (parties.isNotEmpty()) {
            kotlinx.coroutines.delay(1800)
            parties = emptyList()
        }
    }

    // Show the delete confirmation dialog
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

    // Show the calendar dialog
    if (showCalendar) {
        ProgressCalendarDialog(habit = habit, onDismiss = { showCalendar = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(containerColor = Color.Transparent) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ScreenHeader(habit = habit, onBack = onBack, onEditClick = onEditClick)
                StatsRow(habit = habit)
                DeadlineCard(habit = habit)
                CompletionCard(habit = habit, onComplete = completeWithConfetti)
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showCalendar = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Calendar",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Completion Calendar", color = Color.White)
                }
                QuoteCard()
                OutlinedButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showDeleteConfirmDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Habit",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Habit", color = Color.Red.copy(alpha = 0.7f))
                }
            }
        }

        // Render the confetti animation on top of everything only when active.
        if (parties.isNotEmpty()) {
            KonfettiView(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                parties = parties
            )
        }

    }
}

@Composable
fun ScreenHeader(habit: Habit, onBack: () -> Unit, onEditClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onBack()
            }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = habit.iconUrl,
                contentDescription = "Habit Icon",
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                habit.name,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(habit.category, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        IconButton(
            onClick = {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onEditClick()
            }
        ) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Habit", tint = Color.White)
        }
    }
}

@Composable
fun StatsRow(habit: Habit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DetailStatCard(
            Modifier.weight(1f),
            "Day Streak",
            habit.streakCount.toString(),
            Icons.Default.LocalFireDepartment,
            HeroGold
        )
        DetailStatCard(
            Modifier.weight(1f),
            "Total Done",
            habit.completionCount.toString(),
            Icons.Default.TrackChanges,
            Color(0xFF32CD32)
        )
    }
}

@Composable
fun DetailStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
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
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Blue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Schedule,
                    "Deadline",
                    tint = Color.Blue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Daily Deadline: $deadlineTime", fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Complete before this time to maintain your streak",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (!habit.isCompletedToday) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = habit.iconUrl,
                        contentDescription = "Habit Icon",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Ready to Conquer Today?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Mark this habit as complete when you're done!",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    GradientButton("Mark as Complete") {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete()
                    }
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        "Completed",
                        tint = Color(0xFF32CD32),
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Completed Today! 🎉",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        "Amazing work! You've completed this habit for today.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
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
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Text(
            "Every small step counts towards becoming the hero you want to be! 🧑‍🚀",
            Modifier.padding(16.dp),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
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
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0xFF5EFF8A), Color(0xFF27A2F8))
                    )
                )
                .padding(vertical = 12.dp),
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
                Text(
                    "Delete Habit",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Are you sure you want to delete this habit permanently? This action cannot be undone.",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red.copy(alpha = 0.7f)
                        )
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
        habit.completionDates.map {
            Instant.ofEpochMilli(it.time)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.toSet()
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
                    Text(
                        "Progress Calendar",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    IconButton(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = habit.iconUrl,
                        contentDescription = habit.name,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(
                            "${habit.completionCount} total completions",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
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
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            "Previous Month",
                            tint = Color.White
                        )
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
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Next Month",
                            tint = Color.White
                        )
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
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (
                                        (weekIndex == 0 && dayIndex < firstDayOfMonth) ||
                                        dayCounter > daysInMonth
                                    ) {
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
                                            boxModifier = boxModifier.border(
                                                BorderStroke(1.dp, HeroGold),
                                                RoundedCornerShape(8.dp)
                                            )
                                            textColor = HeroGold
                                        } else if (isCompleted) {
                                            boxModifier = boxModifier.background(
                                                Color(0xFF32CD32).copy(alpha = 0.3f)
                                            )
                                            textColor = Color.White
                                        } else {
                                            textColor = Color.White.copy(alpha = 0.7f)
                                        }

                                        Box(
                                            modifier = boxModifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                dayCounter.toString(),
                                                color = textColor,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        dayCounter++
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LegendItem(
                        color = Color(0xFF32CD32).copy(alpha = 0.3f),
                        text = "Completed"
                    )
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
