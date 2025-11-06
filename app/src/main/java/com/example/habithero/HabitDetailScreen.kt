package com.example.habithero

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.HeroGold
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = uiState.error!!)
        }
    } else if (uiState.habit != null) {
        DetailScreenContent(
            habit = uiState.habit!!,
            onBack = onBackClick,
            onEditClick = { onEditClick(uiState.habit!!.id) },
            onDelete = { viewModel.deleteHabit() },
            onComplete = { viewModel.completeHabit() }
        )
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

    if (showDeleteConfirmDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDelete()
                showDeleteConfirmDialog = false
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showCalendar) {
        ProgressCalendarDialog(habit = habit, onDismiss = { showCalendar = false })
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeader(habit = habit, onBack = onBack, onEditClick = onEditClick)

            Spacer(modifier = Modifier.height(12.dp))

            StatsRow(habit = habit)

            Spacer(modifier = Modifier.height(12.dp))

            DeadlineCard(habit = habit)

            Spacer(modifier = Modifier.height(12.dp))

            CompletionCard(habit = habit, onComplete = onComplete)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showCalendar = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Calendar", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Completion Calendar", color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            QuoteCard()

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showDeleteConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.7f))
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = Color.Red.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Habit", color = Color.Red.copy(alpha = 0.7f))
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

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Progress Calendar", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close", tint = Color.Gray)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Habit Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(model = habit.iconUrl, contentDescription = habit.name, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("${habit.completionCount} total completions", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Month Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Month", tint = Color.White)
                    }
                    Text(
                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Month", tint = Color.White)
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Days of the week
                Row(modifier = Modifier.fillMaxWidth()) {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    for (day in days) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(day, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Calendar Grid
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
                                            boxModifier = boxModifier.background(Color(0xFF5EFF8A).copy(alpha = 0.3f))
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

                // Legend
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    LegendItem(color = Color(0xFF5EFF8A).copy(alpha = 0.3f), text = "Completed")
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


@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
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
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
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
fun ScreenHeader(habit: Habit, onBack: () -> Unit, onEditClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
        Spacer(modifier = Modifier.width(16.dp))
        AsyncImage(model = habit.iconUrl, contentDescription = "Habit Icon", modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("${habit.emoji} ${habit.name}".trim(), style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text(habit.category, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        IconButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = "Edit Habit", tint = Color.White)
        }
    }
}

@Composable
fun StatsRow(habit: Habit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(Modifier.weight(1f), "Day Streak", habit.streakCount.toString(), "🔥")
        StatCard(Modifier.weight(1f), "Total Done", habit.completionCount.toString(), "🎯")
    }
}

@Composable
fun DeadlineCard(habit: Habit) {
    val deadlineTime = remember(habit.completionHour, habit.completionMinute) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, habit.completionHour)
            set(java.util.Calendar.MINUTE, habit.completionMinute)
        }
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        format.format(calendar.time)
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Schedule, "Deadline", tint = Color.White)
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
    val isCompletedInCurrentPeriod = remember(habit.completionDates) {
        val now = Date()
        val lastCompletion = habit.lastCompletionDate

        if (lastCompletion == null) {
            false
        } else {
            val deadlineCal = java.util.Calendar.getInstance()
            deadlineCal.set(java.util.Calendar.HOUR_OF_DAY, habit.completionHour)
            deadlineCal.set(java.util.Calendar.MINUTE, habit.completionMinute)
            deadlineCal.set(java.util.Calendar.SECOND, 0)
            deadlineCal.set(java.util.Calendar.MILLISECOND, 0)

            if (deadlineCal.time.before(now)) {
                deadlineCal.add(java.util.Calendar.DAY_OF_YEAR, 1)
            }

            deadlineCal.add(java.util.Calendar.DAY_OF_YEAR, -1)
            val periodStart = deadlineCal.time

            lastCompletion.after(periodStart)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.padding(16.dp).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (!isCompletedInCurrentPeriod) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.background, CircleShape).padding(8.dp), contentAlignment = Alignment.Center) {
                        AsyncImage(model = habit.iconUrl, "Habit Icon", modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Ready to Conquer Today?", style = MaterialTheme.typography.titleMedium, color = Color.White, textAlign = TextAlign.Center)
                    Text("Mark this habit as complete when you\'re done!", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))
                    GradientButton("Mark as Complete", onComplete)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.CheckCircle, "Completed", tint = Color(0xFF5EFF8A), modifier = Modifier.size(50.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Completed Today! 🎉", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    Text("Amazing work! You\'ve completed this habit for today.", color = Color.Gray, textAlign = TextAlign.Center, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(0.8f),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), contentPadding = PaddingValues()
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
fun QuoteCard() {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF))) {
        Text("\"Every small step counts towards becoming the hero you want to be! 🧑‍🚀\"", Modifier.padding(16.dp), color = Color.White, textAlign = TextAlign.Center, fontSize = 12.sp)
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, label: String, value: String, icon: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, color = Color.White)
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
    }
}
