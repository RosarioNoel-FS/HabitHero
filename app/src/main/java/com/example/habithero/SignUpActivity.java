package com.example.habithero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_screen);

        mAuth = FirebaseAuth.getInstance();
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        Button signUpButton = findViewById(R.id.signUpButton);
        Button alreadyHaveAccountBtn = findViewById(R.id.already_have_account_btn);

        signUpButton.setOnClickListener(v -> registerUser());
        alreadyHaveAccountBtn.setOnClickListener(view -> navigateLandingActivity());
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password cannot be empty", Toast.LENGTH_SHORT).show();
            SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SoundHelper.playSound(this, SoundHelper.SoundType.NEXT);
                        navigateToUsernameActivity();
                    } else {
                        SoundHelper.playSound(this, SoundHelper.SoundType.DENY);
                        Toast.makeText(SignUpActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToUsernameActivity() {
        Intent intent = new Intent(this, UsernameActivity.class);
        startActivity(intent);
        finish();
    }private void navigateLandingActivity() {
        Intent intent = new Intent(this, LandingActivity.class);
        startActivity(intent);
        finish();
    }
}
