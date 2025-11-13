package com.example.habithero

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.habithero.ui.theme.GoldTransparent
import com.example.habithero.ui.theme.HeroGold
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// Represents the time-of-day sections
enum class TimeOfDay(val title: String, val emoji: String, val timeRange: String) {
    Morning("Morning", "🌅", "5:00 AM – 11:59 AM"),
    Afternoon("Afternoon", "🌤️", "12:00 PM – 5:59 PM"),
    Night("Night", "🌙", "6:00 PM – 4:59 AM")
}

fun getHabitTimeOfDay(habit: Habit): TimeOfDay {
    val hour = habit.completionHour
    return when (hour) {
        in 5..11 -> TimeOfDay.Morning
        in 12..17 -> TimeOfDay.Afternoon
        else -> TimeOfDay.Night // Covers 6 PM to 4:59 AM
    }
}

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onSettingsClick: () -> Unit,
    onFabClick: () -> Unit,
    onHabitClick: (Habit) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadContent()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeScreenContent(
        userName = uiState.userName,
        habits = uiState.habits,
        isLoading = uiState.isLoading,
        onSettingsClick = onSettingsClick,
        onFabClick = onFabClick,
        onHabitClick = onHabitClick,
        onDeleteHabit = { viewModel.deleteHabit(it) } // Pass the delete function
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    userName: String,
    habits: List<Habit>,
    isLoading: Boolean,
    onSettingsClick: () -> Unit,
    onFabClick: () -> Unit,
    onHabitClick: (Habit) -> Unit,
    onDeleteHabit: (String) -> Unit
) {
    val groupedHabits = remember(habits) {
        habits.groupBy { getHabitTimeOfDay(it) }
            .mapValues { (_, habits) ->
                habits.sortedWith(compareBy({ it.completionHour }, { it.completionMinute }))
            }
    }
    val isFirstTime = habits.isEmpty()
    val haptics = LocalHapticFeedback.current
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // Show confirmation dialog when a habit is staged for deletion
    if (habitToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                habitToDelete?.let { onDeleteHabit(it.id) }
                habitToDelete = null // Dismiss dialog
            },
            onDismiss = { habitToDelete = null } // Dismiss dialog
        )
    }

    // Animation for the FAB
    val fabScale = remember { Animatable(1f) }
    LaunchedEffect(isFirstTime) {
        if (isFirstTime) {
            fabScale.animateTo(
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 800),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            fabScale.stop()
            fabScale.snapTo(1f)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFabClick()
                },
                containerColor = colorScheme.primary,
                shape = shapes.extraLarge,
                modifier = Modifier.scale(fabScale.value)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit", tint = colorScheme.onPrimary)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            WelcomeHeader(name = userName, isFirstTime = isFirstTime, onSettingsClick = onSettingsClick)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (isFirstTime) {
                EmptyState()
            } else {
                Text(
                    text = "Your Active Habits (${habits.size})",
                    style = typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeOfDay.values().forEach { timeOfDay ->
                        groupedHabits[timeOfDay]?.let { habitsInSection ->
                            item { TimeOfDayHeader(timeOfDay) }
                            items(habitsInSection, key = { it.id }) { habit ->
                                val dismissState = rememberSwipeToDismissBoxState(
                                    confirmValueChange = {
                                        if (it == SwipeToDismissBoxValue.EndToStart) {
                                            habitToDelete = habit
                                            return@rememberSwipeToDismissBoxState false // Prevent immediate dismissal
                                        }
                                        true
                                    }
                                )

                                SwipeToDismissBox(
                                    state = dismissState,
                                    backgroundContent = { 
                                        val color = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.4f)
                                            else -> Color.Transparent
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(color, shape = shapes.large)
                                                .padding(horizontal = 20.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color.White
                                            )
                                        }
                                     },
                                    enableDismissFromEndToStart = true,
                                    enableDismissFromStartToEnd = false
                                ) {
                                    HabitListItem(habit = habit, onClick = { onHabitClick(habit) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.no_habit_img),
            contentDescription = "No habits yet",
            modifier = Modifier.size(250.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Let's build your first habit",
            style = typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap the + button to create your first habit",
            style = typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

@Composable
fun TimeOfDayHeader(timeOfDay: TimeOfDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = GoldTransparent),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(timeOfDay.emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(timeOfDay.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                Text(timeOfDay.timeRange, color = Color.Gray, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun WelcomeHeader(name: String, isFirstTime: Boolean, onSettingsClick: () -> Unit) {
    val title = if (isFirstTime) "Welcome, $name!" else "Welcome back, $name!"
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ready to build your habits today?",
                style = typography.bodyLarge,
                color = colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        IconButton(onClick = {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onSettingsClick()
        }) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = colorScheme.primary
            )
        }
    }
}

@Composable
fun HabitListItem(habit: Habit, onClick: () -> Unit) {
    val isCompleted = habit.isCompletedToday
    val haptics = LocalHapticFeedback.current

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, habit.completionHour)
        set(Calendar.MINUTE, habit.completionMinute)
    }
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    val deadlineTime = format.format(calendar.time)

    val cardModifier = if (isCompleted) {
        Modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, spotColor = HeroGold, shape = shapes.large)
            .clickable { 
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    } else {
        Modifier
            .fillMaxWidth()
            .clickable { 
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
    }

    val border = if (isCompleted) {
        BorderStroke(1.dp, HeroGold)
    } else {
        BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    }

    Card(
        modifier = cardModifier,
        shape = shapes.large,
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = border
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = habit.iconUrl,
                contentDescription = "Habit Icon",
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${habit.emoji} ${habit.name}".trim(), style = typography.titleMedium, fontWeight = FontWeight.Bold)
                Row {
                    Text("🔥 ${habit.streakCount} day streak", style = typography.bodyMedium, color = colorScheme.secondary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🕓 by $deadlineTime", style = typography.bodyMedium, color = colorScheme.secondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = habit.category, style = typography.bodySmall, color = colorScheme.secondary, fontWeight = FontWeight.Bold)

            }
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(HeroGold.copy(alpha = 0.2f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = HeroGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
