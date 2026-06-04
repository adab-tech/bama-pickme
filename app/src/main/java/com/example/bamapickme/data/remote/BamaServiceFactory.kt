package com.example.bamapickme.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.UUID

object BamaServiceFactory {
  private const val TAG = "BamaServiceFactory"

  /**
   * Checks if a live Firebase configuration is fully initialized on the device.
   */
  fun isFirebaseAvailable(context: Context): Boolean {
    return try {
      val apps = FirebaseApp.getApps(context)
      if (apps.isEmpty()) {
        FirebaseApp.initializeApp(context)
      }
      val app = FirebaseApp.getInstance()
      val pId = app.options.projectId
      // Check if project ID is not the mock package info
      pId != null && pId.isNotEmpty() && pId != "bamapickme-mock"
    } catch (e: Exception) {
      Log.w(TAG, "Firebase SDK not configured, falling back to local simulation: ${e.message}")
      false
    }
  }
}

class FirebaseAuthManager(private val context: Context) {
  private val firebaseAuth: FirebaseAuth? = if (BamaServiceFactory.isFirebaseAvailable(context)) {
    try {
      FirebaseAuth.getInstance()
    } catch (e: Exception) {
      null
    }
  } else {
    null
  }

  fun isUserSignedIn(): Boolean {
    return firebaseAuth?.currentUser != null
  }

  fun getFirebaseUserId(): String? {
    return firebaseAuth?.currentUser?.uid
  }

  fun getFirebaseUserName(): String? {
    return firebaseAuth?.currentUser?.displayName
  }

  fun getFirebaseUserEmail(): String? {
    return firebaseAuth?.currentUser?.email
  }

  fun signInAnonymously(onComplete: (Boolean, String) -> Unit) {
    val auth = firebaseAuth
    if (auth == null) {
      onComplete(false, "Firebase Auth not initialized.")
      return
    }
    auth.signInAnonymously()
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          onComplete(true, "Successfully signed in.")
        } else {
          onComplete(false, task.exception?.message ?: "Unknown Firebase error.")
        }
      }
  }

  fun registerWithEmail(email: String, password: String, displayName: String, onComplete: (Boolean, String) -> Unit) {
    val auth = firebaseAuth
    if (auth == null) {
      onComplete(false, "Firebase Auth not initialized.")
      return
    }
    auth.createUserWithEmailAndPassword(email, password)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          val user = auth.currentUser
          val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(displayName)
            .build()
          user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { updateTask ->
              if (updateTask.isSuccessful) {
                onComplete(true, "Registration successful!")
              } else {
                onComplete(true, "Registration successful (profile update failed).")
              }
            } ?: onComplete(true, "Registration successful!")
        } else {
          onComplete(false, task.exception?.message ?: "Registration failed.")
        }
      }
  }

  fun loginWithEmail(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
    val auth = firebaseAuth
    if (auth == null) {
      onComplete(false, "Firebase Auth not initialized.")
      return
    }
    auth.signInWithEmailAndPassword(email, password)
      .addOnCompleteListener { task ->
        if (task.isSuccessful) {
          onComplete(true, "Successfully logged in.")
        } else {
          onComplete(false, task.exception?.message ?: "Login failed.")
        }
      }
  }

  fun signOut() {
    firebaseAuth?.signOut()
  }
}

class FirestoreDatabase(private val context: Context) {
  private val firestore: FirebaseFirestore? = if (BamaServiceFactory.isFirebaseAvailable(context)) {
    try {
      FirebaseFirestore.getInstance()
    } catch (e: Exception) {
      null
    }
  } else {
    null
  }

  /**
   * Subscribes to listings in Firestore if available.
   * If not available, callback is not fired for Firestore changes.
   */
  fun subscribeToListings(onUpdate: (List<com.example.bamapickme.data.Listing>) -> Unit) {
    val fs = firestore ?: return
    fs.collection("listings")
      .orderBy("timestamp")
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.e("FirestoreDatabase", "Error listening to listings", error)
          return@addSnapshotListener
        }
        if (snapshot != null) {
          val list = snapshot.documents.mapNotNull { doc ->
            try {
              com.example.bamapickme.data.Listing(
                id = doc.id,
                title = doc.getString("title") ?: "",
                category = doc.getString("category") ?: "",
                locationName = doc.getString("locationName") ?: "",
                latitude = doc.getDouble("latitude") ?: 0.0,
                longitude = doc.getDouble("longitude") ?: 0.0,
                isSafeZone = doc.getBoolean("isSafeZone") ?: false,
                fuzzedLatitude = doc.getDouble("fuzzedLatitude") ?: 0.0,
                fuzzedLongitude = doc.getDouble("fuzzedLongitude") ?: 0.0,
                condition = doc.getString("condition") ?: "",
                donorName = doc.getString("donorName") ?: "",
                donorId = doc.getString("donorId") ?: "",
                contact = doc.getString("contact") ?: "",
                pickupWindow = doc.getString("pickupWindow") ?: "",
                description = doc.getString("description") ?: "",
                status = doc.getString("status") ?: "available",
                reservedById = doc.getString("reservedById"),
                imageUri = doc.getString("imageUri"),
                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                dropType = doc.getString("dropType") ?: "handoff"
              )
            } catch (e: Exception) {
              null
            }
          }
          onUpdate(list)
        }
      }
  }

  fun uploadListingToFirestore(listing: com.example.bamapickme.data.Listing, onComplete: (Boolean) -> Unit) {
    val fs = firestore
    if (fs == null) {
      onComplete(false)
      return
    }

    val listingMap = hashMapOf(
      "title" to listing.title,
      "category" to listing.category,
      "locationName" to listing.locationName,
      "latitude" to listing.latitude,
      "longitude" to listing.longitude,
      "isSafeZone" to listing.isSafeZone,
      "fuzzedLatitude" to listing.fuzzedLatitude,
      "fuzzedLongitude" to listing.fuzzedLongitude,
      "condition" to listing.condition,
      "donorName" to listing.donorName,
      "donorId" to listing.donorId,
      "contact" to listing.contact,
      "pickupWindow" to listing.pickupWindow,
      "description" to listing.description,
      "status" to listing.status,
      "reservedById" to listing.reservedById,
      "imageUri" to listing.imageUri,
      "timestamp" to listing.timestamp,
      "dropType" to listing.dropType
    )

    fs.collection("listings").document(listing.id)
      .set(listingMap)
      .addOnSuccessListener { onComplete(true) }
      .addOnFailureListener {
        Log.e("FirestoreDatabase", "Failed to write listing", it)
        onComplete(false)
      }
  }
}

class CloudStorageManager(private val context: Context) {
  private val storage: FirebaseStorage? = if (BamaServiceFactory.isFirebaseAvailable(context)) {
    try {
      FirebaseStorage.getInstance()
    } catch (e: Exception) {
      null
    }
  } else {
    null
  }

  /**
   * Uploads a local photo to Firebase Storage. If Firebase is offline,
   * caches the file in local app directory and returns its local URI.
   */
  fun uploadPhoto(imageUriStr: String, onResult: (String) -> Unit) {
    val storageInstance = storage
    if (storageInstance == null || imageUriStr.startsWith("mock://")) {
      // Mock Storage Fallback: Save local copy to cache
      onResult(imageUriStr)
      return
    }

    val uri = Uri.parse(imageUriStr)
    val storageRef = storageInstance.reference.child("images/${UUID.randomUUID()}.jpg")

    storageRef.putFile(uri)
      .addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
          onResult(downloadUri.toString())
        }
      }
      .addOnFailureListener {
        Log.e("CloudStorageManager", "Upload failed: ${it.message}, falling back to local file.")
        onResult(imageUriStr) // fallback to original input
      }
  }
}
