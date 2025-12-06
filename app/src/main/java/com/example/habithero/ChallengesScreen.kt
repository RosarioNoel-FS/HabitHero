package com.example.habithero

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habithero.model.Challenge
import com.example.habithero.ui.theme.HeroGold

@Composable
fun ChallengesScreen(
    viewModel: ChallengesViewModel,
    onChallengeClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = uiState.error!!)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large Title Header
                item {
                    Column {
                        Text(
                            "Challenges",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Transform your life with proven habit combinations",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
                items(uiState.challenges) { challenge ->
                    ChallengeCard(
                        challenge = challenge,
                        isAccepted = challenge.id in uiState.acceptedChallengeIds,
                        onClick = { onChallengeClick(challenge.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ChallengeCard(
    challenge: Challenge,
    isAccepted: Boolean,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current

    val backgroundResId = when (challenge.id) {
        "morning_warrior" -> R.drawable.morning_warrior_card
        "productivity_master" -> R.drawable.productivity_master
        else -> null // No custom background
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, HeroGold.copy(alpha = 0.3f))
    ) {
        if (backgroundResId != null) {
            ThemedChallengeCardContent(challenge, isAccepted, backgroundResId)
        } else {
            DefaultChallengeCardContent(challenge, isAccepted)
        }
    }
}

@Composable
private fun DefaultChallengeCardContent(challenge: Challenge, isAccepted: Boolean) {
    Column(modifier = Modifier.padding(24.dp)) {
        // Top Row: Icon, Title/Days, Accepted Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = HeroGold,
                modifier = Modifier.size(40.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = challenge.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isAccepted) {
                        Spacer(Modifier.width(8.dp))
                        if (challenge.isCompletedToday) {
                            CompletedBadge()
                        } else {
                            AcceptedBadge()
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    if (isAccepted) {
                        Text("Day ${challenge.currentDay} of ${challenge.daysTotal}", color = Color.Gray)
                    } else {
                        Text("${challenge.durationDays} Days", color = Color.Gray)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Description
        Text(challenge.description, color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        // Bottom Row: Habit Count, View Details
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
                Text("${challenge.habits.size} Habits", color = Color.Gray)
            }
            if (isAccepted) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(id = R.drawable.ic_heart), contentDescription = "Lives", tint = Color.Red, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(challenge.lives.toString(), color = Color.Gray)
                }
            }
            Text(
                text = if (isAccepted) "View Details →" else "Start Challenge →",
                color = HeroGold,
                fontWeight = FontWeight.Bold
            )
        }
        if (isAccepted) {
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { challenge.progressPercent },
                modifier = Modifier.fillMaxWidth(),
                color = HeroGold,
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
private fun ThemedChallengeCardContent(challenge: Challenge, isAccepted: Boolean, backgroundResId: Int) {
    Box(modifier = Modifier.height(200.dp)) {
        // Background Image
        Image(
            painter = painterResource(id = backgroundResId),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f), // Darker at the top
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)  // Darker at the bottom
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Title and Description (Top)
            Text(
                text = challenge.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White // Themed text color
            )
            Spacer(Modifier.height(4.dp))
            Text(challenge.description, color = Color.White.copy(alpha = 0.8f)) // Themed text color
            
            // Spacer to push the bottom content down
            Spacer(Modifier.weight(1f))

            // Bottom Row (Stats and Link)
            Column {
                if (isAccepted) {
                     Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Day ${challenge.currentDay} of ${challenge.daysTotal}", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.width(16.dp))
                        Icon(painterResource(id = R.drawable.ic_heart), contentDescription = "Lives", tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(challenge.lives.toString(), color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "View Details →",
                            color = HeroGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { challenge.progressPercent },
                        modifier = Modifier.fillMaxWidth(),
                        color = HeroGold,
                        trackColor = Color.Gray.copy(alpha = 0.3f)
                    )
                } else {
                     Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Habit Count
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${challenge.habits.size} Habits",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        // Duration
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarToday,
                                null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "${challenge.durationDays} Days",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                        // This spacer pushes the text to the end
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = "Start Challenge →",
                            color = HeroGold,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Accepted Badge at the top right
        if (isAccepted) {
            Box(modifier = Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.TopEnd) {
                if (challenge.isCompletedToday) {
                    CompletedBadge()
                } else {
                    AcceptedBadge()
                }
            }
        }
    }
}

@Composable
private fun AcceptedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Green.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            null,
            tint = Color.Green,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "Accepted",
            color = Color.Green,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CompletedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(HeroGold.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.Check,
            null,
            tint = HeroGold,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            "Completed",
            color = HeroGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
