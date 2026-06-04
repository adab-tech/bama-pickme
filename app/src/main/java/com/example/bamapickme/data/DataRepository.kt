package com.example.bamapickme.data

import android.content.Context
import android.util.Log
import com.example.bamapickme.data.remote.*
import com.example.bamapickme.security.LocationFuzzer
import com.example.bamapickme.security.ScamDetector
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface DataRepository {
  val listings: Flow<List<Listing>>
  val requests: Flow<List<Request>>
  val threads: Flow<List<Thread>>
  val reports: Flow<List<Report>>
  val feedItems: Flow<List<ActivityFeedItem>>
  val activeUser: Flow<User?>
  val users: List<User>

  fun getMessages(threadId: String): List<Message>
  fun login(userId: String)
  fun logout()
  fun signInAnonymously(onComplete: (Boolean, String) -> Unit)
  fun registerWithEmail(email: String, password: String, displayName: String, onComplete: (Boolean, String) -> Unit)
  fun loginWithEmail(email: String, password: String, onComplete: (Boolean, String) -> Unit)
  fun isFirebaseEnabled(): Boolean
  fun addListing(
    title: String,
    category: String,
    locationName: String,
    latitude: Double,
    longitude: Double,
    isSafeZone: Boolean,
    condition: String,
    contact: String,
    pickupWindow: String,
    description: String,
    dropType: String,
    imageUri: String?
  ): Pair<Boolean, String>

  fun addRequest(title: String, urgency: String, locationName: String, details: String): Pair<Boolean, String>
  fun claimListing(listingId: String): Pair<Boolean, String>
  fun offerSupport(requestId: String): Pair<Boolean, String>
  fun sendMessage(threadId: String, body: String): Pair<Boolean, String>
  fun createReport(targetId: String, targetType: String, targetTitle: String, reason: String): Pair<Boolean, String>
  fun resolveReport(reportId: String): Pair<Boolean, String>
  fun askChatbot(query: String): Pair<Boolean, String>
}

class DefaultDataRepository(private val context: Context) : DataRepository {
  private val db = BamaDatabaseHelper(context)

  // Production SDK Managers (Firebase)
  private val authManager = FirebaseAuthManager(context)
  private val firestoreDb = FirestoreDatabase(context)
  private val storageManager = CloudStorageManager(context)

  override val users = listOf(
    User("maya", "Maya, Senior", "Donor", "Biochemistry student clearing out materials.", "maya@crimson.ua.edu", true, 98),
    User("jaylen", "Jaylen, Freshman", "Seeker", "Looking to get settled into campus life.", "jaylen@crimson.ua.edu", true, 100),
    User("tamia", "Tamia, Staff Moderator", "Admin", "Supports safe campus exchanges.", "tamia@ua.edu", true, 100)
  )

  private val _listings = MutableStateFlow<List<Listing>>(emptyList())
  override val listings: Flow<List<Listing>> = _listings.asStateFlow()

  private val _requests = MutableStateFlow<List<Request>>(emptyList())
  override val requests: Flow<List<Request>> = _requests.asStateFlow()

  private val _threads = MutableStateFlow<List<Thread>>(emptyList())
  override val threads: Flow<List<Thread>> = _threads.asStateFlow()

  private val _reports = MutableStateFlow<List<Report>>(emptyList())
  override val reports: Flow<List<Report>> = _reports.asStateFlow()

  private val _feedItems = MutableStateFlow<List<ActivityFeedItem>>(emptyList())
  override val feedItems: Flow<List<ActivityFeedItem>> = _feedItems.asStateFlow()

  private val _activeUser = MutableStateFlow<User?>(null)
  override val activeUser: Flow<User?> = _activeUser.asStateFlow()

  init {
    seedDatabase()
    refreshData()

    // Real-Time Firebase Sync: Subscribes if configurations are active
    if (BamaServiceFactory.isFirebaseAvailable(context)) {
      firestoreDb.subscribeToListings { firebaseListings ->
        firebaseListings.forEach { db.insertListing(it) }
        _listings.value = db.getListings()
      }
    }
  }

  private fun seedDatabase() {
    // 1. Seed users
    val existingUsers = db.getUsers()
    if (existingUsers.isEmpty()) {
      users.forEach { db.insertUser(it) }
    }

    // 2. Seed starter listings
    val existingListings = db.getListings()
    if (existingListings.isEmpty()) {
      db.insertListing(
        Listing(
          id = UUID.randomUUID().toString(),
          title = "Organic Chemistry Textbook Set",
          category = "Textbooks",
          locationName = "Ridgecrest South Courtyard",
          latitude = 33.2161,
          longitude = -87.5424,
          isSafeZone = true,
          fuzzedLatitude = 33.2161,
          fuzzedLongitude = -87.5424,
          condition = "Good",
          donorName = "Maya, Senior",
          donorId = "maya",
          contact = "maya@crimson.ua.edu",
          pickupWindow = "Today after 5 PM",
          description = "Includes textbook, solution manual, and lab notebook. Perfect for CHEM 231/232.",
          status = "available",
          dropType = "handoff"
        )
      )

      db.insertListing(
        Listing(
          id = UUID.randomUUID().toString(),
          title = "Schwinn Campus Bicycle",
          category = "Transportation",
          locationName = "Near Ferguson Center",
          latitude = 33.2114,
          longitude = -87.5458,
          isSafeZone = true,
          fuzzedLatitude = 33.2114,
          fuzzedLongitude = -87.5458,
          condition = "Fair",
          donorName = "Maya, Senior",
          donorId = "maya",
          contact = "maya@crimson.ua.edu",
          pickupWindow = "Weekdays 1 PM - 4 PM",
          description = "Reliable commuter bike with working brakes. Ideal for getting across campus.",
          status = "available",
          dropType = "handoff"
        )
      )

      db.insertListing(
        Listing(
          id = UUID.randomUUID().toString(),
          title = "Working Microwave (Bin Drop)",
          category = "Dorm Essentials",
          locationName = "Tutwiler Recycling Bin",
          latitude = 33.2045,
          longitude = -87.5421,
          isSafeZone = false,
          fuzzedLatitude = 33.2048,
          fuzzedLongitude = -87.5424,
          condition = "Like New",
          donorName = "Jordan, Student",
          donorId = "maya",
          contact = "jordy@crimson.ua.edu",
          pickupWindow = "Anytime - dropped at bin",
          description = "Dropped a working microwave at the Tutwiler off-campus recycle bin container. Fully operational.",
          status = "available",
          dropType = "bin"
        )
      )
    }

    // 3. Seed starter requests
    val existingRequests = db.getRequests()
    if (existingRequests.isEmpty()) {
      db.insertRequest(
        Request(
          id = UUID.randomUUID().toString(),
          title = "Financial Calculator",
          urgency = "Urgent",
          seekerName = "Jaylen, Freshman",
          seekerId = "jaylen",
          locationName = "Shelby Hall Lobby",
          details = "Needed for upcoming exam. Meeting on campus works."
        )
      )
    }

    // 4. Seed feed
    val existingFeed = db.getFeedItems()
    if (existingFeed.isEmpty()) {
      db.insertFeedItem(
        ActivityFeedItem(
          id = UUID.randomUUID().toString(),
          type = "dropoff",
          user = "Jordan",
          itemTitle = "Working Microwave",
          details = "Dropped off at Tutwiler Recycling Bin.",
          icon = "🛢️"
        )
      )
      db.insertFeedItem(
        ActivityFeedItem(
          id = UUID.randomUUID().toString(),
          type = "dropoff",
          user = "Maya",
          itemTitle = "Organic Chemistry Textbook Set",
          details = "Listed for in-person handoff at Ridgecrest South.",
          icon = "🤝"
        )
      )
    }

    // 5. Seed threads and messages
    val existingThreads = db.getThreads()
    if (existingThreads.isEmpty()) {
      val welcomeThreadId = "welcome-thread"
      db.insertThread(
        Thread(
          id = welcomeThreadId,
          title = "Welcome to BamaPickMe",
          listingId = null,
          participantIds = listOf("maya", "jaylen", "tamia"),
          subtitle = "Platform onboarding and secure guidelines"
        )
      )
      db.insertMessage(
        Message(
          id = UUID.randomUUID().toString(),
          threadId = welcomeThreadId,
          authorId = "tamia",
          authorName = "Tamia, Staff Moderator",
          body = "Welcome. Pin exchange points, request items, or try asking the AI chatbot helper about active listings."
        )
      )
    }

    // Default Login (uses Firebase Auth if active, otherwise falls back to Maya for Mock mode)
    if (BamaServiceFactory.isFirebaseAvailable(context)) {
      if (authManager.isUserSignedIn()) {
        val uid = authManager.getFirebaseUserId() ?: "firebase-user"
        val name = authManager.getFirebaseUserName() ?: authManager.getFirebaseUserEmail() ?: "Firebase User (${uid.take(5)})"
        val email = authManager.getFirebaseUserEmail() ?: "firebase@ua.edu"
        _activeUser.value = User(
          id = uid,
          name = name,
          role = "Donor",
          tagline = "Authenticated via Firebase",
          email = email,
          isVerified = true,
          trustScore = 100
        )
      } else {
        _activeUser.value = null // boot to AuthScreen if Firebase is active but not signed in
      }
    } else {
      _activeUser.value = users[0] // Mock mode default login
    }
  }

  private fun refreshData() {
    _listings.value = db.getListings()
    _requests.value = db.getRequests()
    _threads.value = db.getThreads()
    _reports.value = db.getReports()
    _feedItems.value = db.getFeedItems()
  }

  override fun getMessages(threadId: String): List<Message> {
    return db.getMessages(threadId)
  }

  override fun login(userId: String) {
    val user = users.find { it.id == userId }
    _activeUser.value = user
  }

  override fun logout() {
    _activeUser.value = null
    if (authManager.isUserSignedIn()) {
      authManager.signOut()
    }
  }

  override fun isFirebaseEnabled(): Boolean {
    return BamaServiceFactory.isFirebaseAvailable(context)
  }

  override fun signInAnonymously(onComplete: (Boolean, String) -> Unit) {
    authManager.signInAnonymously { success, message ->
      if (success) {
        val uid = authManager.getFirebaseUserId() ?: "firebase-user"
        val name = authManager.getFirebaseUserName() ?: authManager.getFirebaseUserEmail() ?: "Firebase User (${uid.take(5)})"
        val email = authManager.getFirebaseUserEmail() ?: "firebase@ua.edu"
        _activeUser.value = User(
          id = uid,
          name = name,
          role = "Donor",
          tagline = "Authenticated via Firebase",
          email = email,
          isVerified = true,
          trustScore = 100
        )
        onComplete(true, "Signed in successfully!")
      } else {
        onComplete(false, message)
      }
    }
  }

  override fun registerWithEmail(email: String, password: String, displayName: String, onComplete: (Boolean, String) -> Unit) {
    authManager.registerWithEmail(email, password, displayName) { success, message ->
      if (success) {
        val uid = authManager.getFirebaseUserId() ?: "firebase-user"
        val name = authManager.getFirebaseUserName() ?: displayName
        _activeUser.value = User(
          id = uid,
          name = name,
          role = "Donor",
          tagline = "Authenticated via Firebase",
          email = email,
          isVerified = true,
          trustScore = 100
        )
        onComplete(true, "Account created successfully!")
      } else {
        onComplete(false, message)
      }
    }
  }

  override fun loginWithEmail(email: String, password: String, onComplete: (Boolean, String) -> Unit) {
    authManager.loginWithEmail(email, password) { success, message ->
      if (success) {
        val uid = authManager.getFirebaseUserId() ?: "firebase-user"
        val name = authManager.getFirebaseUserName() ?: authManager.getFirebaseUserEmail() ?: "Firebase User (${uid.take(5)})"
        val userEmail = authManager.getFirebaseUserEmail() ?: email
        _activeUser.value = User(
          id = uid,
          name = name,
          role = "Donor",
          tagline = "Authenticated via Firebase",
          email = userEmail,
          isVerified = true,
          trustScore = 100
        )
        onComplete(true, "Signed in successfully!")
      } else {
        onComplete(false, message)
      }
    }
  }

  override fun addListing(
    title: String,
    category: String,
    locationName: String,
    latitude: Double,
    longitude: Double,
    isSafeZone: Boolean,
    condition: String,
    contact: String,
    pickupWindow: String,
    description: String,
    dropType: String,
    imageUri: String?
  ): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."

    // 1. Scan for scams/spam
    val (isScam, reason) = ScamDetector.scanListing(title, description)
    if (isScam) {
      // Flag automatically and save to report/moderation queue
      val autoReportId = UUID.randomUUID().toString()
      db.insertReport(
        Report(
          id = autoReportId,
          targetId = "flagged-listing",
          targetType = "listing",
          targetTitle = title,
          reason = "Auto-flagged by ScamDetector: $reason",
          reportedById = "system",
          reportedByName = "System Scanner",
          status = "open"
        )
      )
      refreshData()
      return false to "Blocked: $reason Your item has been sent for admin review."
    }

    // 2. Scan for duplicate listings (spam)
    val existingListingsPairs = db.getListings().map { it.title to it.description }
    if (ScamDetector.isDuplicate(title, description, existingListingsPairs)) {
      return false to "Blocked: Duplicate listing detected. You cannot publish exact duplicates."
    }

    // 3. Fuzz locations to protect user privacy
    val (fLat, fLon) = LocationFuzzer.fuzzCoordinates(latitude, longitude, isSafeZone)

    // 4. Asynchronous Cloud Image Upload with Local Fallback
    var finalImageUri = imageUri
    if (imageUri != null) {
      storageManager.uploadPhoto(imageUri) { cloudUri ->
        finalImageUri = cloudUri
        Log.d("DataRepository", "Photo successfully uploaded: $cloudUri")
      }
    }

    val newListing = Listing(
      id = UUID.randomUUID().toString(),
      title = title,
      category = category,
      locationName = locationName,
      latitude = latitude,
      longitude = longitude,
      isSafeZone = isSafeZone,
      fuzzedLatitude = fLat,
      fuzzedLongitude = fLon,
      condition = condition,
      donorName = active.name,
      donorId = active.id,
      contact = contact,
      pickupWindow = pickupWindow,
      description = description,
      status = "available",
      imageUri = finalImageUri,
      dropType = dropType
    )

    db.insertListing(newListing)

    // 5. Firebase Firestore Sync
    if (BamaServiceFactory.isFirebaseAvailable(context)) {
      firestoreDb.uploadListingToFirestore(newListing) { success ->
        if (success) {
          Log.d("DataRepository", "Listing synced to Firestore successfully!")
        }
      }
    }

    // Add to activity feed
    val feedIcon = if (dropType == "bin") "🛢️" else "🤝"
    val feedDetails = if (dropType == "bin") "Dropped off at $locationName." else "Available for handoff at $locationName."
    db.insertFeedItem(
      ActivityFeedItem(
        id = UUID.randomUUID().toString(),
        type = "dropoff",
        user = active.name.substringBefore(","),
        itemTitle = title,
        details = feedDetails,
        icon = feedIcon
      )
    )

    refreshData()
    return true to "Listing published successfully."
  }

  override fun addRequest(title: String, urgency: String, locationName: String, details: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."

    val newRequest = Request(
      id = UUID.randomUUID().toString(),
      title = title,
      urgency = urgency,
      seekerName = active.name,
      seekerId = active.id,
      locationName = locationName,
      details = details
    )

    db.insertRequest(newRequest)

    db.insertFeedItem(
      ActivityFeedItem(
        id = UUID.randomUUID().toString(),
        type = "request",
        user = active.name.substringBefore(","),
        itemTitle = title,
        details = "Requested urgency: $urgency near $locationName.",
        icon = "📋"
      )
    )

    refreshData()
    return true to "Request published successfully."
  }

  override fun claimListing(listingId: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."
    val listing = db.getListings().find { it.id == listingId } ?: return false to "Listing not found."

    if (listing.status != "available") {
      return false to "Listing already reserved or claimed."
    }

    val updatedListing = listing.copy(status = "reserved", reservedById = active.id)
    db.insertListing(updatedListing)

    // Create a conversation thread between Seeker, Donor and Admin
    val newThreadId = UUID.randomUUID().toString()
    db.insertThread(
      Thread(
        id = newThreadId,
        title = "${listing.title} handoff",
        listingId = listing.id,
        participantIds = listOf(listing.donorId, active.id, "tamia"),
        subtitle = "Handoff at ${listing.locationName} - Window: ${listing.pickupWindow}"
      )
    )

    db.insertMessage(
      Message(
        id = UUID.randomUUID().toString(),
        threadId = newThreadId,
        authorId = active.id,
        authorName = active.name,
        body = "Hi, I reserved your listing: ${listing.title}. Does the pickup window '${listing.pickupWindow}' still work?"
      )
    )

    db.insertFeedItem(
      ActivityFeedItem(
        id = UUID.randomUUID().toString(),
        type = "claim",
        user = active.name.substringBefore(","),
        itemTitle = listing.title,
        details = "Reserved for handoff.",
        icon = "🤝"
      )
    )

    refreshData()
    return true to "Item reserved! A secure chat channel has been opened."
  }

  override fun offerSupport(requestId: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."
    val request = db.getRequests().find { it.id == requestId } ?: return false to "Request not found."

    // Create conversation thread
    val newThreadId = UUID.randomUUID().toString()
    db.insertThread(
      Thread(
        id = newThreadId,
        title = "${request.title} support offer",
        listingId = null,
        participantIds = listOf(request.seekerId, active.id, "tamia"),
        subtitle = "Supporting request from ${request.seekerName}"
      )
    )

    db.insertMessage(
      Message(
        id = UUID.randomUUID().toString(),
        threadId = newThreadId,
        authorId = active.id,
        authorName = active.name,
        body = "Hi, I saw your request for: ${request.title}. I can help you with this. Let me know when is best to meet."
      )
    )

    refreshData()
    return true to "Support offer sent! Conversation started."
  }

  override fun sendMessage(threadId: String, body: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."
    val thread = db.getThreads().find { it.id == threadId } ?: return false to "Thread not found."

    val newMessage = Message(
      id = UUID.randomUUID().toString(),
      threadId = threadId,
      authorId = active.id,
      authorName = active.name,
      body = body
    )

    db.insertMessage(newMessage)
    refreshData()
    return true to "Message sent."
  }

  override fun createReport(targetId: String, targetType: String, targetTitle: String, reason: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."

    val newReport = Report(
      id = UUID.randomUUID().toString(),
      targetId = targetId,
      targetType = targetType,
      targetTitle = targetTitle,
      reason = reason,
      reportedById = active.id,
      reportedByName = active.name
    )

    db.insertReport(newReport)
    refreshData()
    return true to "Report submitted to moderators."
  }

  override fun resolveReport(reportId: String): Pair<Boolean, String> {
    val active = _activeUser.value ?: return false to "User not signed in."
    if (active.role != "Admin") {
      return false to "Only administrators can resolve reports."
    }

    val report = db.getReports().find { it.id == reportId } ?: return false to "Report not found."
    val updated = report.copy(status = "resolved")
    db.insertReport(updated)

    // If report was resolved and targets a listing, suspend it (flag it)
    if (report.targetType == "listing") {
      val listing = db.getListings().find { it.id == report.targetId || it.title == report.targetTitle }
      if (listing != null) {
        val flaggedListing = listing.copy(status = "flagged")
        db.insertListing(flaggedListing)
      }
    }

    refreshData()
    return true to "Report marked as resolved."
  }

  override fun askChatbot(query: String): Pair<Boolean, String> {
    val q = query.lowercase()

    // Query active database listings
    val activeListings = db.getListings().filter { it.status == "available" }
    val matchingListing = activeListings.find { q.contains(it.title.lowercase()) || q.contains(it.category.lowercase()) }

    val responseText = when {
      q.contains("microwave") -> {
        val micro = activeListings.find { it.title.lowercase().contains("microwave") }
        if (micro != null) {
          "Yes! A working microwave is currently available at the Tutwiler Recycling Bin (dropped off by Jordan). You can browse the Marketplace to claim it."
        } else {
          "Currently, there are no microwaves listed. You can post a Request on the Requests board so someone in the community can see your need!"
        }
      }
      q.contains("textbook") || q.contains("chemistry") || q.contains("book") -> {
        val book = activeListings.find { it.category == "Textbooks" || it.title.lowercase().contains("book") }
        if (book != null) {
          "I found a listing for '${book.title}' by ${book.donorName} at ${book.locationName}. Pickup window is: ${book.pickupWindow}."
        } else {
          "I couldn't find any textbooks listed right now. Check back soon or post a custom Seeker Request."
        }
      }
      q.contains("safe exchange") || q.contains("where to meet") || q.contains("police") -> {
        "For your safety, we recommend using pre-verified Safe Exchange Zones. Recommended spots include: the Ferguson Student Center Lobby, the UA Police Department Lobby, and Gorgas Library Main Plaza."
      }
      q.contains("closest bin") || q.contains("recycling") || q.contains("drop off") -> {
        "We have verified off-campus recycle drop bins near Tutwiler Hall, Shelby Hall, and Lakeside. Make sure to specify the drop-off status and upload a photo!"
      }
      matchingListing != null -> {
        "Yes! I found an available '${matchingListing.title}' listed under ${matchingListing.category} at ${matchingListing.locationName}."
      }
      else -> {
        "Roll Tide! I'm your BamaPickMe Assistant. Ask me about campus drop-off bins, available textbooks, microwaves, or where to find Safe Exchange Zones on campus."
      }
    }

    return true to responseText
  }
}
