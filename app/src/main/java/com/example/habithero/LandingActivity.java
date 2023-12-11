package com.example.habithero;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class LandingActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private EditText emailEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_screen);

        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());

        emailEditText = findViewById(R.id.emailSignInEditText);
        passwordEditText = findViewById(R.id.passwordSignInEditText);

        Button signInButton = findViewById(R.id.email_sign_in_button);
        Button createAccountButton = findViewById(R.id.create_account_button);

        findViewById(R.id.google_sign_in_button).setOnClickListener(v -> signInWithGoogle());
        signInButton.setOnClickListener(v -> signInWithEmail());
        createAccountButton.setOnClickListener(v -> navigateToSignUpActivity());
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithEmail() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        SoundHelper.playSound(this, SoundHelper.SoundType.COMPLETION);
                        navigateToMainActivity();
                    } else {
                        Toast.makeText(LandingActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

                    }
                });
    }

    private void navigateToSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Sign in failed: Account details not found.", Toast.LENGTH_SHORT).show();
                    SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();
                        if (isNewUser) {
                            SoundHelper.playSound(this, SoundHelper.SoundType.COMPLETION);

                            Toast.makeText(LandingActivity.this, "Welcome! Please set your username.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LandingActivity.this, UsernameActivity.class));
                        } else {
                            SoundHelper.playSound(this, SoundHelper.SoundType.COMPLETION);
                            Toast.makeText(LandingActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LandingActivity.this, MainActivity.class));
                        }
                    } else {
                        Toast.makeText(LandingActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        SoundHelper.playSound(this, SoundHelper.SoundType.DENY);

                    }
                });
    }

//    private void playSignInSound() {
//        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.next_sound);
//        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
//        mediaPlayer.start();
//    }
}
