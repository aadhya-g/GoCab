package com.example.gocab

import androidx.compose.runtime.mutableStateOf

object MainScreenNavigationHelper {
    val currentScreen = mutableStateOf(Screen.SPLASH)

    fun navigateTo(screen: Screen) {
        currentScreen.value = screen
    }
}
