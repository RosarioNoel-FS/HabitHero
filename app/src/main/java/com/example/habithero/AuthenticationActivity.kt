package com.example.habithero

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.habithero.ui.theme.HeroTheme

class AuthenticationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HeroTheme {
                AuthenticationScreen(
                    onAuthenticationSuccess = {
                        // After successful sign in or sign up, navigate to the main app
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Finish this activity so the user can't go back to it
                    }
                )
            }
        }
    }
}
