package com.example.habithero.ui.dialogs

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.habithero.R
import com.example.habithero.ui.theme.HeroGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LifeLostDialog(
    challengeName: String,
    livesRemaining: Int,
    onDismiss: () -> Unit
) {
    val colorProgress = remember { Animatable(0f) }
    val iconScale = remember { Animatable(1f) }
    val iconRotation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 1. Initial heartbeat/thump
        iconScale.animateTo(1.3f, animationSpec = tween(150))
        iconScale.animateTo(1f, animationSpec = tween(150))

        delay(250)

        // 2. Shake animation
        iconRotation.animateTo(15f, animationSpec = tween(100))
        iconRotation.animateTo(-15f, animationSpec = tween(100, delayMillis = 50))
        iconRotation.animateTo(0f, animationSpec = tween(100, delayMillis = 50))

        delay(300)

        // 3. "Break" animation: animate color progress and shrink
        launch {
            colorProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400)
            )
        }
        launch {
            iconScale.animateTo(
                targetValue = 0.8f,
                animationSpec = tween(durationMillis = 400)
            )
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("You Missed a Day!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(Modifier.height(16.dp))

                Icon(
                    painter = painterResource(id = R.drawable.ic_heart),
                    contentDescription = "Heart Icon",
                    tint = lerp(start = Color.Red, stop = Color.Gray.copy(alpha = 0.5f), fraction = colorProgress.value),
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale.value)
                        .rotate(iconRotation.value)
                )

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "You lost a life in the \"$challengeName\" challenge. You have $livesRemaining ${if (livesRemaining == 1) "life" else "lives"} left.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = HeroGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("I'll do better!", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
