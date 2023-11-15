package com.example.habithero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;

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
                Toast.makeText(this, validationMessage, Toast.LENGTH_SHORT).show();
            } else {
                saveUsernameToFirestore(username);
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
        db.collection("users").document(userId).set(Collections.singletonMap("username", username))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UsernameActivity.this, "Username saved!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(UsernameActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UsernameActivity.this, "Error saving username. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }



}
