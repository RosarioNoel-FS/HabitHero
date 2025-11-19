package com.example.habithero

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.habithero.ui.theme.HeroTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // Not signed in, launch the authentication activity
            startActivity(Intent(this, AuthenticationActivity::class.java))
            finish() // Finish MainActivity so the user can't go back to it
            return
        }

        setContent {
            HeroTheme {
                MainScreen()
            }
        }
    }
}
