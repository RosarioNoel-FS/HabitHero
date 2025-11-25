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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habithero.ui.theme.HeroGold

@Composable
fun RewardsScreen(viewModel: RewardsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Your Progress", style = typography.headlineLarge, color = Color.White)
        Text("See how far you\'ve come!", style = typography.bodyLarge, color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = uiState.error!!, color = Color.Red)
            }
        } else if (uiState.userStats != null) {
            // Stats Section
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RewardStatCard(modifier = Modifier.weight(1f), label = "Best Streak", value = "${uiState.userStats!!.bestStreak} Days", icon = Icons.Default.LocalFireDepartment, iconColor = HeroGold)
                RewardStatCard(modifier = Modifier.weight(1f), label = "Total Done", value = uiState.userStats!!.totalCompleted.toString(), icon = Icons.Default.CheckCircle, iconColor = Color(0xFF32CD32))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                RewardStatCard(modifier = Modifier.weight(1f), label = "Active Habits", value = uiState.userStats!!.activeHabits.toString(), icon = Icons.Default.BarChart, iconColor = Color.Blue.copy(alpha = 0.7f))
                RewardStatCard(modifier = Modifier.weight(1f), label = "Habits Created", value = uiState.userStats!!.habitsCreated.toString(), icon = Icons.Default.Star, iconColor = HeroGold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Achievement Badges Section
            Text("Achievement Badges", style = typography.headlineMedium, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No stats found.", color = Color.Gray)
            }
        }
    }
}

@Composable
fun RewardStatCard(modifier: Modifier = Modifier, label: String, value: String, icon: ImageVector, iconColor: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface.copy(alpha = 0.5f)),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(32.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text(text = label, style = typography.bodyMedium, color = Color.Gray)
        }
    }
}
