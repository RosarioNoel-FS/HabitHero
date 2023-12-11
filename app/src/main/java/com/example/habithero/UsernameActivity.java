package com.example.habithero;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class UsernameActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText usernameEditText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.username_screen);

        db = FirebaseFirestore.getInstance();

        usernameEditText = findViewById(R.id.username_editText);
        Button confirmButton = findViewById(R.id.confirm_username);

        confirmButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String validationMessage = isValidUsername(username);
            if (!"OK".equals(validationMessage)) {
                SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

                Toast.makeText(this, validationMessage, Toast.LENGTH_SHORT).show();
            } else {
                checkUsernameUnique(username);
            }
        });
    }

    private String isValidUsername(String username) {
        // Check if the username is empty
        if (username.trim().isEmpty()) {
            return "Username cannot be empty.";
        }
        // Check the length of the username
        if (username.length() < 3) {
            return "Username is too short. Minimum length is 3 characters.";
        }
        if (username.length() > 15) {
            return "Username is too long. Maximum length is 15 characters.";
        }
        // Check for invalid characters
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            return "Username can only contain letters, numbers, and underscores.";
        }
        // Check for consecutive underscores or starting with an underscore
        if (username.contains("__") || username.startsWith("_")) {
            return "Username cannot contain consecutive underscores or start with an underscore.";
        }
        // Check if the username doesn't start with a number
        if (Character.isDigit(username.charAt(0))) {
            return "Username cannot start with a number.";
        }
        // All checks passed, username is valid
        return "OK";
    }

    private void saveUsernameToFirestore(String username) {
        // Here, assuming you want to save the username under a document with the user's UID
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);

        db.collection("users").document(userId).set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UsernameActivity.this, "Username saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UsernameActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("UsernameActivity", "Error saving username: " + e.getMessage(), e);

                    Toast.makeText(UsernameActivity.this, "Error saving username. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    // New method to check if the username is unique
    private void checkUsernameUnique(String username) {
        db.collection("users")
                .whereEqualTo("username", username)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        SoundHelper.playSound(this, SoundHelper.SoundType.COMPLETION);

                        saveUsernameToFirestore(username);
                    } else {
                        Toast.makeText(UsernameActivity.this, "Username already exists. Please choose another.", Toast.LENGTH_SHORT).show();
                        SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UsernameActivity", "Error checking username uniqueness: " + e.getMessage(), e);
                    SoundHelper.playSound(this, SoundHelper.SoundType.DENY);
                    Toast.makeText(UsernameActivity.this, "Error checking username. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



}
