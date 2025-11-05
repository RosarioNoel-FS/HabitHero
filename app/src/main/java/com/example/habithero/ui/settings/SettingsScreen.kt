package com.example.habithero.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.habithero.model.User
import com.example.habithero.ui.theme.DarkBlue
import com.example.habithero.ui.theme.HeroGold
import com.example.habithero.ui.theme.HeroTheme
import com.example.habithero.ui.theme.TextGray
import com.example.habithero.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onChangeName: (String) -> Unit,
    onSaveName: () -> Unit,
    onPickPhoto: (Uri) -> Unit,
    onSignOut: () -> Unit,
) {
    val pickImage = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? -> uri?.let(onPickPhoto) }
    )

    HeroTheme {
        Scaffold(
            containerColor = DarkBlue
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = White)
                    }
                    Column {
                        Text("Settings", style = com.example.habithero.ui.theme.Typography.headlineMedium, color = White)
                        Text("Manage your hero profile", style = com.example.habithero.ui.theme.Typography.bodyMedium, color = TextGray)
                    }
                }

                // Profile Section
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Profile", style = com.example.habithero.ui.theme.Typography.titleLarge, fontWeight = FontWeight.Bold, color = White)
                    Spacer(Modifier.height(16.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = com.example.habithero.ui.theme.CardDarkBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = state.user.photoUrl,
                                    contentDescription = "Profile photo",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .clickable { pickImage.launch("image/*") },
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(state.user.name, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = White)
                                    Text(state.user.email, fontSize = 14.sp, color = TextGray)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                            Text("Username", fontSize = 14.sp, color = TextGray)
                            OutlinedTextField(
                                value = state.nameEditing,
                                onValueChange = onChangeName,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                trailingIcon = {
                                    IconButton(onClick = onSaveName) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Username", tint = HeroGold)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = HeroGold,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = White,
                                    unfocusedTextColor = White,
                                    cursorColor = HeroGold,
                                    focusedContainerColor = DarkBlue.copy(alpha = 0.5f),
                                    unfocusedContainerColor = DarkBlue.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // Account Section
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text("Account", style = com.example.habithero.ui.theme.Typography.titleLarge, fontWeight = FontWeight.Bold, color = White)
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = com.example.habithero.ui.theme.CardDarkBlue)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Sign Out", tint = Color(0xFFE53E3E))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", color = Color(0xFFE53E3E), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // Footer card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4A5568)), // A slightly different purple/blue
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Habit Hero", style = com.example.habithero.ui.theme.Typography.titleLarge, color = White, fontWeight = FontWeight.Bold)
                        Text("Building superhero habits, one day at a time 🦸‍♂️", color = TextGray)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1A202C)
@Composable
fun SettingsScreenPreview() {
    HeroTheme {
        SettingsScreen(
            state = SettingsUiState(
                loading = false,
                user = User(
                    name = "SuperHero",
                    email = "hero@habitapp.com"
                ),
                nameEditing = "SuperHero"
            ),
            onBack = {},
            onChangeName = {},
            onSaveName = {},
            onPickPhoto = {},
            onSignOut = {}
        )
    }
}
