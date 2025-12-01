package com.example.habithero.ui.rewards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.habithero.data.BadgeWithState

@Composable
fun BadgeCard(badgeWithState: BadgeWithState) {
    val isUnlocked = badgeWithState.isUnlocked
    val alpha = if (isUnlocked) 1f else 0.4f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(badgeWithState.badge.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = badgeWithState.badge.name,
                modifier = Modifier.fillMaxSize().alpha(alpha)
            )
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(40.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badgeWithState.badge.name,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.alpha(alpha)
        )
    }
}
