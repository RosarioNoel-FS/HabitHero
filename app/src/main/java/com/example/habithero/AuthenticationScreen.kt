package com.example.habithero

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.habithero.ui.theme.HeroGold
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthenticationScreen(onAuthenticationSuccess: () -> Unit) {
    val viewModel: AuthenticationViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    var startFlyAwayAnimation by remember { mutableStateOf(false) }
    val density = LocalDensity.current

    fun launchGoogleSignIn() {
        scope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.default_web_client_id))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context as Activity, request)

                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    viewModel.signInWithGoogle(idToken)
                } else if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        viewModel.signInWithGoogle(idToken)
                    } catch (e: Exception) {
                        Log.e("AuthScreen", "Failed to extract GoogleIdTokenCredential from CustomCredential", e)
                        viewModel.onGoogleSignInError()
                    }
                } else {
                    Log.e("AuthScreen", "Unexpected credential type: ${credential::class.java.name}")
                    viewModel.onGoogleSignInError()
                }

            } catch (e: GetCredentialException) {
                Log.e("AuthScreen", "Google Sign-In failed", e)
                viewModel.onGoogleSignInError()
            }
        }
    }


    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            SoundHelper.playSound(context, SoundHelper.SoundType.COMPLETION)
            startFlyAwayAnimation = true
            delay(1500) // Wait for animation to finish
            onAuthenticationSuccess()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            SoundHelper.playSound(context, SoundHelper.SoundType.DENY)
            viewModel.clearError()
        }
    }

    val screenHeightPx = with(density) { LocalContext.current.resources.displayMetrics.heightPixels.toFloat() }

    val flyAwayY by animateFloatAsState(
        targetValue = if (startFlyAwayAnimation) -screenHeightPx else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutLinearInEasing),
        label = "flyAwayY"
    )

    val flyAwayScale by animateFloatAsState(
        targetValue = if (startFlyAwayAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutLinearInEasing),
        label = "flyAwayScale"
    )

    val formAlpha by animateFloatAsState(
        targetValue = if (startFlyAwayAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "formAlpha"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ) {
        Image(painter = painterResource(id = R.drawable.sign_in_img), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.3f)

        val infiniteTransition = rememberInfiniteTransition(label = "balloon_boy_animation")
        val bobbingAnimation by infiniteTransition.animateFloat(
            initialValue = -15f,
            targetValue = 15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = androidx.compose.animation.core.LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bobbing"
        )

        Image(
            painter = painterResource(id = R.drawable.balloon_boy),
            contentDescription = null, // Decorative image
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .size(200.dp)
                .graphicsLayer {
                    translationY = if (startFlyAwayAnimation) flyAwayY else bobbingAnimation
                    scaleX = if (startFlyAwayAnimation) flyAwayScale else 1f
                    scaleY = if (startFlyAwayAnimation) flyAwayScale else 1f
                }
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
                .graphicsLayer { alpha = formAlpha },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.text_logo),
                contentDescription = "Habit Hero Logo",
                modifier = Modifier.fillMaxWidth(0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (uiState.authenticationMode == AuthenticationMode.SIGN_IN) "Sign in to continue your journey" else "Join the league of heroes",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(value = uiState.email, onValueChange = viewModel::onEmailChanged, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = uiState.password, onValueChange = viewModel::onPasswordChanged, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            if (uiState.authenticationMode == AuthenticationMode.SIGN_UP) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = uiState.confirmPassword, onValueChange = viewModel::onConfirmPasswordChanged, label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))
            }
            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = viewModel::authenticate, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50)) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                } else {
                    Text(if (uiState.authenticationMode == AuthenticationMode.SIGN_IN) "Sign In" else "Sign Up")
                }
            }
            TextButton(onClick = viewModel::toggleAuthenticationMode) {
                Text(text = if (uiState.authenticationMode == AuthenticationMode.SIGN_IN) "Don\'t have an account? Sign Up" else "Already have an account? Sign In", textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
                Text(" OR ", color = Color.Gray)
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.Gray)
            }
            Spacer(Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.google_sign_in_button),
                contentDescription = "Sign In with Google",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable { launchGoogleSignIn() }
            )
        }
    }
}
