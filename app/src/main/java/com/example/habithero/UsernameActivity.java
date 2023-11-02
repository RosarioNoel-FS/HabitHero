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
            String username = usernameEditText.getText().toString();
            if (isValidUsername(username)) {
                saveUsernameToFirestore(username);
            }
        });
    }

    private boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (username.length() < 3) {
            Toast.makeText(this, "Username should have at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        // Add more validation rules if needed
        return true;
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
