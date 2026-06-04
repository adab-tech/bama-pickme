package com.example.bamapickme.ui.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bamapickme.data.DataRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  repository: DataRepository,
  onAuthSuccess: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSignUp by remember { mutableStateOf(false) }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var name by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val context = LocalContext.current
  val crimson = Color(0xFF9E1B32)
  val gold = Color(0xFFD8B26E)
  val darkBg = Color(0xFF140F10)
  val cardBg = Color(0xFF221A1C)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(darkBg),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // Branding Header
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(
              Brush.linearGradient(listOf(crimson, Color(0xFF6F1021))),
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          Text("A", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 36.sp)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "BamaPickMe",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 28.sp,
          color = Color.White
        )
        Text(
          text = "Campus Donation & Exchange",
          fontSize = 14.sp,
          color = gold,
          fontWeight = FontWeight.Medium
        )
      }

      // Card Form
      Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Text(
            text = if (isSignUp) "Create Account" else "Sign In",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.White
          )

          errorMessage?.let { msg ->
            Text(
              text = msg,
              color = crimson,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.fillMaxWidth()
            )
          }

          if (isSignUp) {
            OutlinedTextField(
              value = name,
              onValueChange = { name = it; errorMessage = null },
              label = { Text("Display Name (e.g. Maya, Senior)") },
              leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = gold) },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = crimson,
                unfocusedBorderColor = Color.DarkGray,
                focusedLabelColor = crimson,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
              ),
              modifier = Modifier.fillMaxWidth()
            )
          }

          OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim(); errorMessage = null },
            label = { Text("University Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = gold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = crimson,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = crimson,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = gold) },
            trailingIcon = {
              IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                Icon(
                  imageVector = if (isPasswordVisible) Icons.Default.Warning else Icons.Default.Info, // simple visible toggle
                  contentDescription = "Toggle password visibility",
                  tint = gold
                )
              }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = crimson,
              unfocusedBorderColor = Color.DarkGray,
              focusedLabelColor = crimson,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
          )

          if (isLoading) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(color = crimson)
            }
          } else {
            Button(
              onClick = {
                if (email.isEmpty() || password.isEmpty() || (isSignUp && name.isEmpty())) {
                  errorMessage = "Please fill in all fields."
                  return@Button
                }
                isLoading = true
                if (isSignUp) {
                  repository.registerWithEmail(email, password, name) { success, msg ->
                    isLoading = false
                    if (success) {
                      Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                      onAuthSuccess()
                    } else {
                      errorMessage = msg
                    }
                  }
                } else {
                  repository.loginWithEmail(email, password) { success, msg ->
                    isLoading = false
                    if (success) {
                      Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                      onAuthSuccess()
                    } else {
                      errorMessage = msg
                    }
                  }
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = crimson),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = if (isSignUp) "Register" else "Login",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 15.sp
              )
            }
          }

          // Switch sign-in mode
          Text(
            text = if (isSignUp) "Already have an account? Sign In" else "New to BamaPickMe? Create Account",
            color = gold,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                isSignUp = !isSignUp
                errorMessage = null
              }
              .padding(vertical = 4.dp)
          )
        }
      }

      // Guest / Demo Button
      Text(
        text = "Or continue using a demo account",
        fontSize = 12.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
      )

      Button(
        onClick = {
          isLoading = true
          if (repository.isFirebaseEnabled()) {
            repository.signInAnonymously { success, msg ->
              isLoading = false
              if (success) {
                Toast.makeText(context, "Signed in as Guest", Toast.LENGTH_SHORT).show()
                onAuthSuccess()
              } else {
                Toast.makeText(context, "Auth offline, booting local demo.", Toast.LENGTH_SHORT).show()
                repository.login("maya") // local fallback
                onAuthSuccess()
              }
            }
          } else {
            // Local Mock fallback
            repository.login("maya")
            isLoading = false
            onAuthSuccess()
          }
        },
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent,
          contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.Transparent)
      ) {
        Text("Continue as Guest / Explorer", fontWeight = FontWeight.Bold, color = Color.White)
      }
    }
  }
}
