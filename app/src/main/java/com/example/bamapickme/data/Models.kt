package com.example.bamapickme.data

import kotlinx.serialization.Serializable

@Serializable
data class User(
  val id: String,
  val name: String,
  val role: String, // "Donor", "Seeker", "Admin"
  val tagline: String,
  val email: String,
  val isVerified: Boolean = false,
  val trustScore: Int = 100
)

@Serializable
data class Listing(
  val id: String,
  val title: String,
  val category: String,
  val locationName: String,
  val latitude: Double,
  val longitude: Double,
  val isSafeZone: Boolean,
  val fuzzedLatitude: Double,
  val fuzzedLongitude: Double,
  val condition: String,
  val donorName: String,
  val donorId: String,
  val contact: String,
  val pickupWindow: String,
  val description: String,
  val status: String, // "available", "reserved", "claimed", "flagged"
  val reservedById: String? = null,
  val imageUri: String? = null,
  val timestamp: Long = System.currentTimeMillis(),
  val dropType: String = "handoff" // "handoff", "bin"
)

@Serializable
data class Request(
  val id: String,
  val title: String,
  val urgency: String, // "Urgent", "This Week", "Flexible"
  val seekerName: String,
  val seekerId: String,
  val locationName: String,
  val details: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Thread(
  val id: String,
  val title: String,
  val listingId: String?,
  val participantIds: List<String>,
  val subtitle: String,
  val lastMessageTimestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Message(
  val id: String,
  val threadId: String,
  val authorId: String,
  val authorName: String,
  val body: String,
  val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class Report(
  val id: String,
  val targetId: String,
  val targetType: String, // "listing", "request", "message"
  val targetTitle: String,
  val reason: String,
  val reportedById: String,
  val reportedByName: String,
  val status: String = "open", // "open", "resolved", "dismissed"
  val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ActivityFeedItem(
  val id: String,
  val type: String, // "dropoff", "claim", "request", "security_flag"
  val user: String,
  val itemTitle: String,
  val details: String,
  val icon: String, // "🛢️", "🤝", "✅", "⚠️", "📋"
  val timestamp: Long = System.currentTimeMillis()
)
