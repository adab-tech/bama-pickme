package com.example.bamapickme.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class BamaDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

  companion object {
    private const val DATABASE_NAME = "bamapickme.db"
    private const val DATABASE_VERSION = 1

    // Table names
    private const val TABLE_USERS = "users"
    private const val TABLE_LISTINGS = "listings"
    private const val TABLE_REQUESTS = "requests"
    private const val TABLE_THREADS = "threads"
    private const val TABLE_MESSAGES = "messages"
    private const val TABLE_REPORTS = "reports"
    private const val TABLE_FEED = "activity_feed"
  }

  override fun onCreate(db: SQLiteDatabase) {
    db.execSQL(
      """
            CREATE TABLE $TABLE_USERS (
                id TEXT PRIMARY KEY,
                name TEXT,
                role TEXT,
                tagline TEXT,
                email TEXT,
                isVerified INTEGER,
                trustScore INTEGER
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_LISTINGS (
                id TEXT PRIMARY KEY,
                title TEXT,
                category TEXT,
                locationName TEXT,
                latitude REAL,
                longitude REAL,
                isSafeZone INTEGER,
                fuzzedLatitude REAL,
                fuzzedLongitude REAL,
                condition TEXT,
                donorName TEXT,
                donorId TEXT,
                contact TEXT,
                pickupWindow TEXT,
                description TEXT,
                status TEXT,
                reservedById TEXT,
                imageUri TEXT,
                timestamp INTEGER,
                dropType TEXT
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_REQUESTS (
                id TEXT PRIMARY KEY,
                title TEXT,
                urgency TEXT,
                seekerName TEXT,
                seekerId TEXT,
                locationName TEXT,
                details TEXT,
                timestamp INTEGER
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_THREADS (
                id TEXT PRIMARY KEY,
                title TEXT,
                listingId TEXT,
                participantIds TEXT,
                subtitle TEXT,
                lastMessageTimestamp INTEGER
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_MESSAGES (
                id TEXT PRIMARY KEY,
                threadId TEXT,
                authorId TEXT,
                authorName TEXT,
                body TEXT,
                timestamp INTEGER
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_REPORTS (
                id TEXT PRIMARY KEY,
                targetId TEXT,
                targetType TEXT,
                targetTitle TEXT,
                reason TEXT,
                reportedById TEXT,
                reportedByName TEXT,
                status TEXT,
                timestamp INTEGER
            )
            """.trimIndent()
    )

    db.execSQL(
      """
            CREATE TABLE $TABLE_FEED (
                id TEXT PRIMARY KEY,
                type TEXT,
                user TEXT,
                itemTitle TEXT,
                details TEXT,
                icon TEXT,
                timestamp INTEGER
            )
            """.trimIndent()
    )
  }

  override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_LISTINGS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_REQUESTS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_THREADS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_MESSAGES")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTS")
    db.execSQL("DROP TABLE IF EXISTS $TABLE_FEED")
    onCreate(db)
  }

  // --- Users CRUD ---
  fun insertUser(user: User) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", user.id)
      put("name", user.name)
      put("role", user.role)
      put("tagline", user.tagline)
      put("email", user.email)
      put("isVerified", if (user.isVerified) 1 else 0)
      put("trustScore", user.trustScore)
    }
    db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getUsers(): List<User> {
    val db = readableDatabase
    val list = mutableListOf<User>()
    val cursor = db.query(TABLE_USERS, null, null, null, null, null, null)
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          User(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            name = c.getString(c.getColumnIndexOrThrow("name")),
            role = c.getString(c.getColumnIndexOrThrow("role")),
            tagline = c.getString(c.getColumnIndexOrThrow("tagline")),
            email = c.getString(c.getColumnIndexOrThrow("email")),
            isVerified = c.getInt(c.getColumnIndexOrThrow("isVerified")) == 1,
            trustScore = c.getInt(c.getColumnIndexOrThrow("trustScore"))
          )
        )
      }
    }
    return list
  }

  // --- Listings CRUD ---
  fun insertListing(listing: Listing) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", listing.id)
      put("title", listing.title)
      put("category", listing.category)
      put("locationName", listing.locationName)
      put("latitude", listing.latitude)
      put("longitude", listing.longitude)
      put("isSafeZone", if (listing.isSafeZone) 1 else 0)
      put("fuzzedLatitude", listing.fuzzedLatitude)
      put("fuzzedLongitude", listing.fuzzedLongitude)
      put("condition", listing.condition)
      put("donorName", listing.donorName)
      put("donorId", listing.donorId)
      put("contact", listing.contact)
      put("pickupWindow", listing.pickupWindow)
      put("description", listing.description)
      put("status", listing.status)
      put("reservedById", listing.reservedById)
      put("imageUri", listing.imageUri)
      put("timestamp", listing.timestamp)
      put("dropType", listing.dropType)
    }
    db.insertWithOnConflict(TABLE_LISTINGS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getListings(): List<Listing> {
    val db = readableDatabase
    val list = mutableListOf<Listing>()
    val cursor = db.query(TABLE_LISTINGS, null, null, null, null, null, "timestamp DESC")
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          Listing(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            category = c.getString(c.getColumnIndexOrThrow("category")),
            locationName = c.getString(c.getColumnIndexOrThrow("locationName")),
            latitude = c.getDouble(c.getColumnIndexOrThrow("latitude")),
            longitude = c.getDouble(c.getColumnIndexOrThrow("longitude")),
            isSafeZone = c.getInt(c.getColumnIndexOrThrow("isSafeZone")) == 1,
            fuzzedLatitude = c.getDouble(c.getColumnIndexOrThrow("fuzzedLatitude")),
            fuzzedLongitude = c.getDouble(c.getColumnIndexOrThrow("fuzzedLongitude")),
            condition = c.getString(c.getColumnIndexOrThrow("condition")),
            donorName = c.getString(c.getColumnIndexOrThrow("donorName")),
            donorId = c.getString(c.getColumnIndexOrThrow("donorId")),
            contact = c.getString(c.getColumnIndexOrThrow("contact")),
            pickupWindow = c.getString(c.getColumnIndexOrThrow("pickupWindow")),
            description = c.getString(c.getColumnIndexOrThrow("description")),
            status = c.getString(c.getColumnIndexOrThrow("status")),
            reservedById = c.getString(c.getColumnIndexOrThrow("reservedById")),
            imageUri = c.getString(c.getColumnIndexOrThrow("imageUri")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
            dropType = c.getString(c.getColumnIndexOrThrow("dropType"))
          )
        )
      }
    }
    return list
  }

  // --- Requests CRUD ---
  fun insertRequest(request: Request) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", request.id)
      put("title", request.title)
      put("urgency", request.urgency)
      put("seekerName", request.seekerName)
      put("seekerId", request.seekerId)
      put("locationName", request.locationName)
      put("details", request.details)
      put("timestamp", request.timestamp)
    }
    db.insertWithOnConflict(TABLE_REQUESTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getRequests(): List<Request> {
    val db = readableDatabase
    val list = mutableListOf<Request>()
    val cursor = db.query(TABLE_REQUESTS, null, null, null, null, null, "timestamp DESC")
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          Request(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            urgency = c.getString(c.getColumnIndexOrThrow("urgency")),
            seekerName = c.getString(c.getColumnIndexOrThrow("seekerName")),
            seekerId = c.getString(c.getColumnIndexOrThrow("seekerId")),
            locationName = c.getString(c.getColumnIndexOrThrow("locationName")),
            details = c.getString(c.getColumnIndexOrThrow("details")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
          )
        )
      }
    }
    return list
  }

  // --- Threads CRUD ---
  fun insertThread(thread: Thread) {
    val db = writableDatabase
    val participantIdsJson = Json.encodeToString(ListSerializer(String.serializer()), thread.participantIds)
    val values = ContentValues().apply {
      put("id", thread.id)
      put("title", thread.title)
      put("listingId", thread.listingId)
      put("participantIds", participantIdsJson)
      put("subtitle", thread.subtitle)
      put("lastMessageTimestamp", thread.lastMessageTimestamp)
    }
    db.insertWithOnConflict(TABLE_THREADS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getThreads(): List<Thread> {
    val db = readableDatabase
    val list = mutableListOf<Thread>()
    val cursor = db.query(TABLE_THREADS, null, null, null, null, null, "lastMessageTimestamp DESC")
    cursor.use { c ->
      while (c.moveToNext()) {
        val pJson = c.getString(c.getColumnIndexOrThrow("participantIds"))
        val pList = Json.decodeFromString(ListSerializer(String.serializer()), pJson)
        list.add(
          Thread(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            title = c.getString(c.getColumnIndexOrThrow("title")),
            listingId = c.getString(c.getColumnIndexOrThrow("listingId")),
            participantIds = pList,
            subtitle = c.getString(c.getColumnIndexOrThrow("subtitle")),
            lastMessageTimestamp = c.getLong(c.getColumnIndexOrThrow("lastMessageTimestamp"))
          )
        )
      }
    }
    return list
  }

  // --- Messages CRUD ---
  fun insertMessage(message: Message) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", message.id)
      put("threadId", message.threadId)
      put("authorId", message.authorId)
      put("authorName", message.authorName)
      put("body", message.body)
      put("timestamp", message.timestamp)
    }
    db.insertWithOnConflict(TABLE_MESSAGES, null, values, SQLiteDatabase.CONFLICT_REPLACE)

    // Update parent thread last message timestamp
    val updateValues = ContentValues().apply {
      put("lastMessageTimestamp", message.timestamp)
    }
    db.update(TABLE_THREADS, updateValues, "id = ?", arrayOf(message.threadId))
  }

  fun getMessages(threadId: String): List<Message> {
    val db = readableDatabase
    val list = mutableListOf<Message>()
    val cursor = db.query(TABLE_MESSAGES, null, "threadId = ?", arrayOf(threadId), null, null, "timestamp ASC")
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          Message(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            threadId = c.getString(c.getColumnIndexOrThrow("threadId")),
            authorId = c.getString(c.getColumnIndexOrThrow("authorId")),
            authorName = c.getString(c.getColumnIndexOrThrow("authorName")),
            body = c.getString(c.getColumnIndexOrThrow("body")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
          )
        )
      }
    }
    return list
  }

  // --- Reports CRUD ---
  fun insertReport(report: Report) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", report.id)
      put("targetId", report.targetId)
      put("targetType", report.targetType)
      put("targetTitle", report.targetTitle)
      put("reason", report.reason)
      put("reportedById", report.reportedById)
      put("reportedByName", report.reportedByName)
      put("status", report.status)
      put("timestamp", report.timestamp)
    }
    db.insertWithOnConflict(TABLE_REPORTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getReports(): List<Report> {
    val db = readableDatabase
    val list = mutableListOf<Report>()
    val cursor = db.query(TABLE_REPORTS, null, null, null, null, null, "timestamp DESC")
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          Report(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            targetId = c.getString(c.getColumnIndexOrThrow("targetId")),
            targetType = c.getString(c.getColumnIndexOrThrow("targetType")),
            targetTitle = c.getString(c.getColumnIndexOrThrow("targetTitle")),
            reason = c.getString(c.getColumnIndexOrThrow("reason")),
            reportedById = c.getString(c.getColumnIndexOrThrow("reportedById")),
            reportedByName = c.getString(c.getColumnIndexOrThrow("reportedByName")),
            status = c.getString(c.getColumnIndexOrThrow("status")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
          )
        )
      }
    }
    return list
  }

  // --- Activity Feed CRUD ---
  fun insertFeedItem(item: ActivityFeedItem) {
    val db = writableDatabase
    val values = ContentValues().apply {
      put("id", item.id)
      put("type", item.type)
      put("user", item.user)
      put("itemTitle", item.itemTitle)
      put("details", item.details)
      put("icon", item.icon)
      put("timestamp", item.timestamp)
    }
    db.insertWithOnConflict(TABLE_FEED, null, values, SQLiteDatabase.CONFLICT_REPLACE)
  }

  fun getFeedItems(): List<ActivityFeedItem> {
    val db = readableDatabase
    val list = mutableListOf<ActivityFeedItem>()
    val cursor = db.query(TABLE_FEED, null, null, null, null, null, "timestamp DESC")
    cursor.use { c ->
      while (c.moveToNext()) {
        list.add(
          ActivityFeedItem(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            type = c.getString(c.getColumnIndexOrThrow("type")),
            user = c.getString(c.getColumnIndexOrThrow("user")),
            itemTitle = c.getString(c.getColumnIndexOrThrow("itemTitle")),
            details = c.getString(c.getColumnIndexOrThrow("details")),
            icon = c.getString(c.getColumnIndexOrThrow("icon")),
            timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
          )
        )
      }
    }
    return list
  }
}
