package com.example.bamapickme

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Auth : NavKey

@Serializable
data object Main : NavKey

@Serializable
data class ChatDetail(val threadId: String, val threadTitle: String) : NavKey

@Serializable
data class MapDetail(val title: String, val lat: Double, val lon: Double, val isSafeZone: Boolean) : NavKey
