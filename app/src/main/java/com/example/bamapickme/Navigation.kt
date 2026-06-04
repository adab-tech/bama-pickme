package com.example.bamapickme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.bamapickme.data.DefaultDataRepository
import com.example.bamapickme.ui.auth.AuthScreen
import com.example.bamapickme.ui.chat.ChatDetailScreen
import com.example.bamapickme.ui.main.MainScreen
import com.example.bamapickme.ui.map.MapDetailScreen

@Composable
fun MainNavigation() {
  val context = androidx.compose.ui.platform.LocalContext.current.applicationContext
  val repository = DefaultDataRepository(context)
  val activeUser by repository.activeUser.collectAsStateWithLifecycle(initialValue = null)

  // Start at Auth screen if not signed in, else Main screen
  val backStack = rememberNavBackStack(if (activeUser != null) Main else Auth)

  LaunchedEffect(activeUser) {
    if (activeUser != null) {
      if (!backStack.contains(Main)) {
        // Clear backstack to enter main dashboard as the root
        while (backStack.removeLastOrNull() != null) {}
        backStack.add(Main)
      }
    } else {
      if (!backStack.contains(Auth)) {
        while (backStack.removeLastOrNull() != null) {}
        backStack.add(Auth)
      }
    }
  }

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Auth> {
          AuthScreen(
            repository = repository,
            onAuthSuccess = {
              // Navigated reactively by LaunchedEffect observing activeUser Flow
            },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<Main> {
          MainScreen(
            repository = repository,
            onNavigate = { navKey -> backStack.add(navKey) },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<ChatDetail> { key ->
          ChatDetailScreen(
            threadId = key.threadId,
            threadTitle = key.threadTitle,
            repository = repository,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize()
          )
        }
        entry<MapDetail> { key ->
          MapDetailScreen(
            title = key.title,
            latitude = key.lat,
            longitude = key.lon,
            isSafeZone = key.isSafeZone,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize()
          )
        }
      },
  )
}
