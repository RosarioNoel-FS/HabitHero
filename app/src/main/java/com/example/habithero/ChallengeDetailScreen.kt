package com.example.habithero

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithero.ui.theme.HeroGold

@Composable
fun ChallengeDetailScreen(
    viewModel: ChallengeDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (uiState.error != null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: ${uiState.error}")
        }
        return
    }

    uiState.challenge?.let { challenge ->
        Scaffold(containerColor = Color.Transparent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ScreenHeader(challenge = challenge, onBackClick = onBackClick)
                InfoCard("About This Challenge", challenge.about, Icons.Default.Info)
                InfoCard("Why This Matters", challenge.whyItMatters, Icons.Default.TrackChanges)
                PositiveEffectsCard(challenge.positiveEffects)
                HabitsCard(challenge.habits)
                AcceptChallengeButton(
                    isAlreadyAccepted = uiState.isAlreadyAccepted,
                    onAcceptChallenge = { viewModel.acceptChallenge() }
                )
            }
        }
    }
}

@Composable
private fun ScreenHeader(challenge: Challenge, onBackClick: () -> Unit) {
    Column {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.EmojiEvents, null, tint = HeroGold, modifier = Modifier.size(48.dp)) // Placeholder
            Spacer(Modifier.width(16.dp))
            Column {
                Text(challenge.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Row {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${challenge.durationDays} Days", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun InfoCard(title: String, content: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = HeroGold, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            Text(content, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun PositiveEffectsCard(effects: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Positive Effects You'll Experience", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            effects.forEach {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                    Icon(Icons.Default.Check, null, tint = Color(0xFF32CD32), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(it, color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
private fun HabitsCard(habits: List<HabitTemplate>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Habits You'll Build (${habits.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            habits.forEach { habit ->
                HabitInChallenge(habit)
            }
        }
    }
}

@Composable
private fun HabitInChallenge(habit: HabitTemplate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.2f))
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(habit.name, fontWeight = FontWeight.Bold, color = Color.White)
                Text(habit.category, color = Color.Gray, fontSize = 14.sp)
            }
            Text(String.format("%02d:%02d", habit.completionHour, habit.completionMinute), color = HeroGold, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AcceptChallengeButton(isAlreadyAccepted: Boolean, onAcceptChallenge: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        if (isAlreadyAccepted) {
             Button(
                onClick = {},
                enabled = false,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green.copy(alpha = 0.2f), disabledContainerColor = Color.Green.copy(alpha = 0.2f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 16.dp)) {
                    Icon(Icons.Default.Check, null, tint = Color.Green)
                    Spacer(Modifier.width(8.dp))
                    Text("Challenge Already Accepted", color = Color.Green, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Button(
                onClick = onAcceptChallenge,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth(0.9f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(colors = listOf(HeroGold, Color(0xFFF57C00)))
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Accept Challenge 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isAlreadyAccepted) "These habits have been added to your routine" else "All habits will be added to your daily routine",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}
