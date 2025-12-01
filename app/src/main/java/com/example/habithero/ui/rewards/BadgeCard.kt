package com.example.habithero.ui.rewards

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.habithero.model.Badge

@Composable
fun BadgeCard(badge: Badge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp).alpha(0.4f) // Always greyed out for now
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(badge.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = badge.name,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.name,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
