package com.example.habithero.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.habithero.model.ChallengeProgress
import com.example.habithero.ui.theme.HeroGold
import java.util.concurrent.TimeUnit
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter

@Composable
fun ChallengeProgressPopup(
    challengeProgress: ChallengeProgress,
    onDismiss: () -> Unit
) {
    val remaining = challengeProgress.habits.count { !it.isCompletedToday }
    val challengeComplete = remaining == 0
    var showCelebration by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current

    if (challengeComplete) {
        LaunchedEffect(Unit) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            showCelebration = true
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (challengeComplete) {
                        CelebrationHeader()
                    } else {
                        Text(
                            text = challengeProgress.challengeName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    challengeProgress.habits.forEach { habitWithCompletion ->
                        HabitProgressItem(
                            habitName = habitWithCompletion.habit.name,
                            iconUrl = habitWithCompletion.habit.iconUrl,
                            isCompleted = habitWithCompletion.isCompletedToday,
                            animate = habitWithCompletion.habit.id == challengeProgress.newlyCompletedHabitId
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val motivationalText = if (challengeComplete) {
                        "Amazing! You\'ve completed the challenge for today!"
                    } else {
                        "Great job! Only $remaining more habits left to complete today’s challenge!"
                    }

                    Text(
                        text = motivationalText,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Continue")
                    }
                }
            }
            if (showCelebration) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize().zIndex(1f),
                    parties = listOf(
                        Party(
                            speed = 10f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
                            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                            emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(100),
                            position = Position.Relative(0.5, 0.0)
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun CelebrationHeader() {
    var animationStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationStarted = true
    }

    val scale by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = 0.4f, 
            stiffness = 200f
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (animationStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            this.alpha = alpha
        }
    ) {
        Icon(
            imageVector = Icons.Default.WorkspacePremium,
            contentDescription = "Trophy",
            tint = HeroGold,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Challenge Complete!",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = HeroGold
        )
    }
}

@Composable
private fun HabitProgressItem(
    habitName: String,
    iconUrl: String,
    isCompleted: Boolean,
    animate: Boolean
) {
    var hasAnimated by remember { mutableStateOf(false) }

    val crossOutProgress by animateFloatAsState(
        targetValue = if (isCompleted && hasAnimated) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "crossOutAnimation"
    )

    LaunchedEffect(key1 = animate) {
        if (animate) {
            hasAnimated = true
        }
    }

    val textDecoration = if (isCompleted && !animate) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(model = iconUrl, contentDescription = habitName, modifier = Modifier.size(24.dp))
        Box(contentAlignment = Alignment.CenterStart) {
            Text(
                text = habitName,
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = textDecoration
            )
            if (animate) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val y = center.y
                    drawLine(
                        color = HeroGold,
                        start = Offset(x = 0f, y = y),
                        end = Offset(x = size.width * crossOutProgress, y = y),
                        strokeWidth = Stroke.DefaultMiter
                    )
                }
            }
        }
    }
}
