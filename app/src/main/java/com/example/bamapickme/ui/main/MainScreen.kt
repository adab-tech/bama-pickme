package com.example.bamapickme.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.example.bamapickme.ChatDetail
import com.example.bamapickme.MapDetail
import com.example.bamapickme.data.*
import com.example.bamapickme.security.LocationFuzzer
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
  repository: DataRepository,
  onNavigate: (NavKey) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  val listings by repository.listings.collectAsStateWithLifecycle(initialValue = emptyList())
  val requests by repository.requests.collectAsStateWithLifecycle(initialValue = emptyList())
  val threads by repository.threads.collectAsStateWithLifecycle(initialValue = emptyList())
  val reports by repository.reports.collectAsStateWithLifecycle(initialValue = emptyList())
  val feedItems by repository.feedItems.collectAsStateWithLifecycle(initialValue = emptyList())
  val activeUser by repository.activeUser.collectAsStateWithLifecycle(initialValue = null)

  val crimson = Color(0xFF9E1B32)
  val gold = Color(0xFFD8B26E)

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .background(Brush.linearGradient(listOf(crimson, Color(0xFF6F1021))), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Text("A", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text("BamaPickMe", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF22181a))
              Text("Alabama Community Exchange", fontSize = 11.sp, color = Color(0xFF6d5b5d))
            }
          }
        },
        actions = {
          activeUser?.let { user ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(end = 12.dp)
            ) {
              Text(
                text = user.name.substringBefore(","),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = crimson
              )
              if (user.isVerified) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Verified",
                  tint = Color(0xFF23643E),
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAF5))
      )
    },
    bottomBar = {
      NavigationBar(containerColor = Color.White) {
        val tabs = listOf(
          Triple("Market", Icons.Default.ShoppingCart, 0),
          Triple("Donate", Icons.Default.AddCircle, 1),
          Triple("AI Assistant", Icons.Default.Face, 2),
          Triple("Inbox", Icons.Default.Email, 3),
          Triple("Account", Icons.Default.Person, 4)
        )
        tabs.forEach { (label, icon, index) ->
          NavigationBarItem(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            icon = { Icon(icon, contentDescription = label) },
            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = crimson,
              selectedTextColor = crimson,
              indicatorColor = Color(0x129E1B32),
              unselectedIconColor = Color(0xFF6d5b5d),
              unselectedTextColor = Color(0xFF6d5b5d)
            )
          )
        }
      }
    },
    containerColor = Color(0xFFFAFAF5),
    modifier = modifier
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      when (selectedTab) {
        0 -> MarketplaceTab(
          listings = listings,
          requests = requests,
          feedItems = feedItems,
          repository = repository,
          onNavigate = onNavigate
        )
        1 -> DonateTab(
          repository = repository
        )
        2 -> ChatbotTab(
          repository = repository
        )
        3 -> InboxTab(
          threads = threads,
          onNavigate = onNavigate
        )
        4 -> AccountTab(
          activeUser = activeUser,
          reports = reports,
          repository = repository
        )
      }
    }
  }
}

// --- TAB 0: MARKETPLACE ---
@Composable
fun MarketplaceTab(
  listings: List<Listing>,
  requests: List<Request>,
  feedItems: List<ActivityFeedItem>,
  repository: DataRepository,
  onNavigate: (NavKey) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("All") }
  var showRequests by remember { mutableStateOf(false) }

  val categories = listOf("All", "Textbooks", "Electronics", "Transportation", "Dorm Essentials", "Furniture")

  Column(modifier = Modifier.fillMaxSize()) {
    // 1. Live Ticker
    if (feedItems.isNotEmpty()) {
      val firstFeed = feedItems.first()
      Card(
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF9E1B32)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(firstFeed.icon, fontSize = 14.sp)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "LIVE UPDATE: ${firstFeed.user} ${firstFeed.details}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1
          )
        }
      }
    }

    // 2. Navigation Header / Toggle
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { showRequests = false },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (!showRequests) Color(0xFF9E1B32) else Color(0x129E1B32),
          contentColor = if (!showRequests) Color.White else Color(0xFF9E1B32)
        )
      ) {
        Text("Browse Items", fontWeight = FontWeight.Bold)
      }
      Button(
        onClick = { showRequests = true },
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (showRequests) Color(0xFF9E1B32) else Color(0x129E1B32),
          contentColor = if (showRequests) Color.White else Color(0xFF9E1B32)
        )
      ) {
        Text("Browse Requests", fontWeight = FontWeight.Bold)
      }
    }

    // 3. Search & Filter Bar
    if (!showRequests) {
      Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search listings...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categories Scroll
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.take(3).forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat, fontSize = 12.sp) }
            )
          }
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          categories.drop(3).forEach { cat ->
            FilterChip(
              selected = selectedCategory == cat,
              onClick = { selectedCategory = cat },
              label = { Text(cat, fontSize = 12.sp) }
            )
          }
        }
      }
    }

    // 4. Content List
    val filteredListings = listings.filter {
      val matchesSearch = it.title.lowercase().contains(searchQuery.lowercase()) ||
        it.description.lowercase().contains(searchQuery.lowercase())
      val matchesCat = selectedCategory == "All" || it.category == selectedCategory
      matchesSearch && matchesCat && it.status != "flagged"
    }

    LazyColumn(
      modifier = Modifier.weight(1f),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      if (!showRequests) {
        items(filteredListings) { item ->
          ListingItemCard(item = item, repository = repository, onNavigate = onNavigate)
        }
      } else {
        items(requests) { req ->
          RequestItemCard(req = req, repository = repository)
        }
      }
    }
  }
}

@Composable
fun ListingItemCard(item: Listing, repository: DataRepository, onNavigate: (NavKey) -> Unit) {
  var showReportDialog by remember { mutableStateOf(false) }
  var reportReason by remember { mutableStateOf("") }
  var alertMessage by remember { mutableStateOf("") }

  val crimson = Color(0xFF9E1B32)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.category,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = crimson
        )

        // Drop-off/ Handoff Validation Icon
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (item.dropType == "bin") "🛢️ Bin Drop" else "🤝 Handoff",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF22181a)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (item.status == "reserved") "Reserved" else "Available",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.status == "reserved") Color(0xFF7A4B00) else Color(0xFF23643E),
            modifier = Modifier
              .background(
                color = if (item.status == "reserved") Color(0x22D8B26E) else Color(0x2223643E),
                shape = RoundedCornerShape(8.dp)
              )
              .padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = item.title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = Color(0xFF22181a)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = item.description,
        fontSize = 13.sp,
        color = Color(0xFF6d5b5d),
        lineHeight = 18.sp
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Location + Maps action
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            onNavigate(
              MapDetail(
                title = item.locationName,
                lat = item.latitude,
                lon = item.longitude,
                isSafeZone = item.isSafeZone
              )
            )
          },
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.LocationOn, contentDescription = "Location", tint = crimson, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (item.isSafeZone) "${item.locationName} 🛡️ (Safe-Spot)" else "${item.locationName} 📍 (Privacy Zone)",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = crimson
        )
      }

      Spacer(modifier = Modifier.height(4.dp))

      Text("Donor: ${item.donorName} • Window: ${item.pickupWindow}", fontSize = 12.sp, color = Color(0xFF6d5b5d))

      Spacer(modifier = Modifier.height(16.dp))

      if (alertMessage.isNotEmpty()) {
        Text(alertMessage, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson, modifier = Modifier.padding(bottom = 8.dp))
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            val (success, msg) = repository.claimListing(item.id)
            alertMessage = msg
          },
          enabled = item.status == "available",
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = crimson),
          modifier = Modifier.weight(1f)
        ) {
          Text("Claim / Reserve", fontWeight = FontWeight.Bold)
        }

        IconButton(
          onClick = { showReportDialog = true },
          colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0x0A22181a))
        ) {
          Icon(Icons.Default.Warning, contentDescription = "Report Listing", tint = Color(0xFF6d5b5d))
        }
      }
    }
  }

  // Report dialog
  if (showReportDialog) {
    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = { Text("Report Listing") },
      text = {
        Column {
          Text("Help keep the campus safe. State your reason for reporting this item:")
          Spacer(modifier = Modifier.height(8.dp))
          TextField(
            value = reportReason,
            onValueChange = { reportReason = it },
            placeholder = { Text("Suspicious user, duplicate, scam payment request...") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (reportReason.trim().isNotEmpty()) {
              repository.createReport(item.id, "listing", item.title, reportReason.trim())
              showReportDialog = false
              reportReason = ""
              alertMessage = "Report submitted to administrators."
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = crimson)
        ) {
          Text("Submit")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun RequestItemCard(req: Request, repository: DataRepository) {
  var alertMessage by remember { mutableStateOf("") }
  val crimson = Color(0xFF9E1B32)

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Urgency: ${req.urgency}",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF7A4B00),
          modifier = Modifier
            .background(Color(0x22D8B26E), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
        )
        Text("Seeker: ${req.seekerName.substringBefore(",")}", fontSize = 11.sp, color = Color(0xFF6d5b5d))
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = req.title,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 18.sp,
        color = Color(0xFF22181a)
      )

      Spacer(modifier = Modifier.height(4.dp))

      Text(
        text = req.details,
        fontSize = 13.sp,
        color = Color(0xFF6d5b5d)
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text("📍 Preferred Pickup: ${req.locationName}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = crimson)

      Spacer(modifier = Modifier.height(16.dp))

      if (alertMessage.isNotEmpty()) {
        Text(alertMessage, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson, modifier = Modifier.padding(bottom = 8.dp))
      }

      Button(
        onClick = {
          val (success, msg) = repository.offerSupport(req.id)
          alertMessage = msg
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = crimson),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text("Offer Help / Coordinate Handoff", fontWeight = FontWeight.Bold)
      }
    }
  }
}

// --- TAB 1: DONATE / POST ---
@Composable
fun DonateTab(repository: DataRepository) {
  var isDonationTab by remember { mutableStateOf(true) }

  // Donation form state
  var title by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Textbooks") }
  var locationName by remember { mutableStateOf("Ferguson Student Center Lobby") }
  var latitude by remember { mutableStateOf(33.2114) }
  var longitude by remember { mutableStateOf(-87.5458) }
  var isSafeZone by remember { mutableStateOf(true) }
  var condition by remember { mutableStateOf("Good") }
  var contact by remember { mutableStateOf("") }
  var pickupWindow by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var dropType by remember { mutableStateOf("handoff") } // "handoff", "bin"
  var photoMockAttached by remember { mutableStateOf(false) }

  // Request form state
  var reqTitle by remember { mutableStateOf("") }
  var reqUrgency by remember { mutableStateOf("This Week") }
  var reqLocationName by remember { mutableStateOf("") }
  var reqDetails by remember { mutableStateOf("") }

  var statusMessage by remember { mutableStateOf("") }

  val crimson = Color(0xFF9E1B32)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = {
            isDonationTab = true
            statusMessage = ""
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isDonationTab) crimson else Color(0x129E1B32),
            contentColor = if (isDonationTab) Color.White else crimson
          ),
          modifier = Modifier.weight(1f)
        ) {
          Text("Post Donation", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            isDonationTab = false
            statusMessage = ""
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(
            containerColor = if (!isDonationTab) crimson else Color(0x129E1B32),
            contentColor = if (!isDonationTab) Color.White else crimson
          ),
          modifier = Modifier.weight(1f)
        ) {
          Text("Request Item", fontWeight = FontWeight.Bold)
        }
      }
    }

    if (isDonationTab) {
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Publish Campus Donation", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF22181a))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Item Title (e.g. Calculus Book)") },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category select
            Text("Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              val list = listOf("Textbooks", "Electronics", "Dorm Essentials")
              list.forEach { cat ->
                FilterChip(
                  selected = category == cat,
                  onClick = { category = cat },
                  label = { Text(cat, fontSize = 11.sp) }
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Handoff vs Bin type
            Text("Handoff Method:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                  dropType = "handoff"
                  locationName = "Ferguson Student Center Lobby"
                  latitude = 33.2114
                  longitude = -87.5458
                  isSafeZone = true
                }
              ) {
                RadioButton(selected = dropType == "handoff", onClick = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("🤝 In-Person Handoff")
              }
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                  dropType = "bin"
                  locationName = "Tutwiler Recycle Bin"
                  latitude = 33.2045
                  longitude = -87.5421
                  isSafeZone = false
                }
              ) {
                RadioButton(selected = dropType == "bin", onClick = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("🛢️ Drop at Bin")
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location coordinates dropping picker mockup
            Text("Drop Pin Location:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson)
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0x089E1B32)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                if (dropType == "handoff") {
                  Text("Suggested Safe Zones:", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                  val safeSpots = LocationFuzzer.campusSafeZones
                  safeSpots.take(2).forEach { spot ->
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                          locationName = spot.name
                          latitude = spot.latitude
                          longitude = spot.longitude
                          isSafeZone = true
                        }
                    ) {
                      Icon(
                        imageVector = if (locationName == spot.name) Icons.Default.CheckCircle else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (locationName == spot.name) Color(0xFF23643E) else crimson,
                        modifier = Modifier.size(16.dp)
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(spot.name, fontSize = 12.sp)
                    }
                  }
                } else {
                  // Bin select fuzzed details
                  Text(
                    text = "🔒 Privacy obcurator active: Exact coordinates will be offset/fuzzed on seekers maps to protect residential locations.",
                    fontSize = 11.sp,
                    color = Color(0xFF7A4B00),
                    fontWeight = FontWeight.Bold
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Describe location (e.g. Tutwiler Bin 3)") },
                    modifier = Modifier.fillMaxWidth()
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = pickupWindow,
              onValueChange = { pickupWindow = it },
              label = { Text("Handoff Window (e.g. Today after 4 PM)") },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = contact,
              onValueChange = { contact = it },
              label = { Text("Contact Info (email / phone)") },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = description,
              onValueChange = { description = it },
              label = { Text("Description (No cash requests/payment handles)") },
              modifier = Modifier.fillMaxWidth(),
              minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mock Camera attachment
            Button(
              onClick = { photoMockAttached = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFECE5D8), contentColor = Color(0xFF22181a)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Share, contentDescription = "Camera")
              Spacer(modifier = Modifier.width(8.dp))
              Text(if (photoMockAttached) "✅ Camera Photo Attached" else "📷 Snap & Attach Photo")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (statusMessage.isNotEmpty()) {
              Text(
                text = statusMessage,
                fontWeight = FontWeight.Bold,
                color = if (statusMessage.startsWith("Success")) Color(0xFF23643E) else crimson,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
              )
            }

            Button(
              onClick = {
                if (title.isEmpty() || description.isEmpty() || contact.isEmpty() || pickupWindow.isEmpty()) {
                  statusMessage = "Error: Please fill out all required fields."
                  return@Button
                }
                val (success, msg) = repository.addListing(
                  title = title,
                  category = category,
                  locationName = locationName,
                  latitude = latitude,
                  longitude = longitude,
                  isSafeZone = isSafeZone,
                  condition = condition,
                  contact = contact,
                  pickupWindow = pickupWindow,
                  description = description,
                  dropType = dropType,
                  imageUri = if (photoMockAttached) "mock://photo" else null
                )
                statusMessage = if (success) "Success: $msg" else "Error: $msg"
                if (success) {
                  title = ""
                  description = ""
                  contact = ""
                  pickupWindow = ""
                  photoMockAttached = false
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = crimson),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Publish Donation", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    } else {
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text("Post Seeker Request", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF22181a))
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
              value = reqTitle,
              onValueChange = { reqTitle = it },
              label = { Text("What are you looking for?") },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Urgency:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              listOf("Urgent", "This Week", "Flexible").forEach { urg ->
                FilterChip(
                  selected = reqUrgency == urg,
                  onClick = { reqUrgency = urg },
                  label = { Text(urg, fontSize = 11.sp) }
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = reqLocationName,
              onValueChange = { reqLocationName = it },
              label = { Text("Preferred Area (e.g. Ferguson or Lakeside)") },
              modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
              value = reqDetails,
              onValueChange = { reqDetails = it },
              label = { Text("Why do you need this / Details") },
              modifier = Modifier.fillMaxWidth(),
              minLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (statusMessage.isNotEmpty()) {
              Text(
                text = statusMessage,
                fontWeight = FontWeight.Bold,
                color = if (statusMessage.startsWith("Success")) Color(0xFF23643E) else crimson,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
              )
            }

            Button(
              onClick = {
                if (reqTitle.isEmpty() || reqLocationName.isEmpty() || reqDetails.isEmpty()) {
                  statusMessage = "Error: Please fill out all required fields."
                  return@Button
                }
                val (success, msg) = repository.addRequest(
                  title = reqTitle,
                  urgency = reqUrgency,
                  locationName = reqLocationName,
                  details = reqDetails
                )
                statusMessage = if (success) "Success: $msg" else "Error: $msg"
                if (success) {
                  reqTitle = ""
                  reqLocationName = ""
                  reqDetails = ""
                }
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = crimson),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Post Seeker Request", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

// --- TAB 2: AI CHATBOT ---
@Composable
fun ChatbotTab(repository: DataRepository) {
  var chatbotHistory by remember {
    mutableStateOf(
      listOf(
        "AI" to "Roll Tide! I'm your BamaPickMe Assistant. Ask me about campus drop-off bins, available textbooks, microwaves, or where to find Safe Exchange Zones on campus."
      )
    )
  }
  var queryText by remember { mutableStateOf("") }
  val listState = remember { androidx.compose.foundation.lazy.LazyListState() }

  val crimson = Color(0xFF9E1B32)

  Column(modifier = Modifier.fillMaxSize()) {
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0x0F9E1B32)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Face, contentDescription = "AI Assistant", tint = crimson, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text("Instant AI Helper", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF22181a))
          Text("Find listings, safety rules, or recycle containers instantly.", fontSize = 12.sp, color = Color(0xFF6d5b5d))
        }
      }
    }

    LazyColumn(
      state = listState,
      modifier = Modifier
        .weight(1f)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      items(chatbotHistory) { (author, text) ->
        val isMe = author == "User"
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
        ) {
          Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
          ) {
            Text(
              text = if (isMe) "You" else "BamaPickMe Assistant 🤖",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF6d5b5d),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )

            Box(
              modifier = Modifier
                .background(
                  color = if (isMe) crimson else Color(0xFFECE5D8),
                  shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
              Text(
                text = text,
                color = if (isMe) Color.White else Color(0xFF22181a),
                fontSize = 14.sp
              )
            }
          }
        }
      }
    }

    // Input row
    Card(
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .padding(16.dp)
          .navigationBarsPadding()
          .imePadding(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        TextField(
          value = queryText,
          onValueChange = { queryText = it },
          placeholder = { Text("Ask about microwaves, bins, safe-zones...") },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(20.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0x089E1B32),
            unfocusedContainerColor = Color(0x089E1B32),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
          )
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (queryText.trim().isNotEmpty()) {
              val userQ = queryText.trim()
              chatbotHistory = chatbotHistory + ("User" to userQ)
              queryText = ""

              val (_, response) = repository.askChatbot(userQ)
              chatbotHistory = chatbotHistory + ("AI" to response)
            }
          },
          colors = IconButtonDefaults.iconButtonColors(containerColor = crimson)
        ) {
          Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
        }
      }
    }
  }
}

// --- TAB 3: INBOX ---
@Composable
fun InboxTab(
  threads: List<Thread>,
  onNavigate: (NavKey) -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Card(
      shape = RoundedCornerShape(0.dp),
      colors = CardDefaults.cardColors(containerColor = Color(0x0F9E1B32)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Text(
        text = "Active Pickup Coordinations",
        fontWeight = FontWeight.ExtraBold,
        fontSize = 16.sp,
        color = Color(0xFF22181a),
        modifier = Modifier.padding(16.dp)
      )
    }

    if (threads.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("No active conversation threads.\nReserve an item to open a coordination channel.", textAlign = TextAlign.Center, color = Color(0xFF6d5b5d))
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(threads) { thread ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onNavigate(ChatDetail(thread.id, thread.title))
              }
          ) {
            Row(
              modifier = Modifier.padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF9E1B32), modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(16.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(thread.title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color(0xFF22181a))
                Spacer(modifier = Modifier.height(2.dp))
                Text(thread.subtitle, fontSize = 12.sp, color = Color(0xFF6d5b5d))
              }
              Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open Chat", tint = Color(0xFF6d5b5d))
            }
          }
        }
      }
    }
  }
}

// --- TAB 4: ACCOUNT / ADMIN ---
@Composable
fun AccountTab(
  activeUser: User?,
  reports: List<Report>,
  repository: DataRepository
) {
  var showUserDialog by remember { mutableStateOf(false) }
  val crimson = Color(0xFF9E1B32)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    activeUser?.let { user ->
      item {
        Card(
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .background(Color(0x1A9E1B32), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = user.name.first().toString(),
                  color = crimson,
                  fontWeight = FontWeight.Bold,
                  fontSize = 20.sp
                )
              }
              Spacer(modifier = Modifier.width(16.dp))
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(user.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                  if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = "Verified Crimson Student",
                      tint = Color(0xFF23643E),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }
                Text("Role: ${user.role} • Security Score: ${user.trustScore}%", fontSize = 12.sp, color = Color(0xFF6d5b5d))
              }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            Text("Tagline:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = crimson)
            Text(user.tagline, fontSize = 13.sp, color = Color(0xFF6d5b5d))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = { showUserDialog = true },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = Color(0x1A9E1B32), contentColor = crimson),
              modifier = Modifier.fillMaxWidth()
            ) {
              Text("Switch Demo Identity", fontWeight = FontWeight.Bold)
            }

            val isFirebaseEnabled = repository.isFirebaseEnabled()
            val isFirebaseUser = user.id != "maya" && user.id != "jaylen" && user.id != "tamia"

            if (isFirebaseEnabled) {
              Spacer(modifier = Modifier.height(8.dp))
              if (isFirebaseUser) {
                Button(
                  onClick = { repository.logout() },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = crimson, contentColor = Color.White),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Sign Out from Firebase", fontWeight = FontWeight.Bold)
                }
              } else {
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                  onClick = {
                    repository.signInAnonymously { success, message ->
                      android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                  },
                  shape = RoundedCornerShape(12.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23643E), contentColor = Color.White),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Sign In with Firebase Auth", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      // If user is Admin, render reports moderation console
      if (user.role == "Admin") {
        item {
          Text(
            text = "Moderation Command Console",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = Color(0xFF22181a),
            modifier = Modifier.padding(vertical = 4.dp)
          )
        }

        if (reports.isEmpty()) {
          item {
            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              modifier = Modifier.fillMaxWidth()
            ) {
              Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Clean Queue: No active reports submitted.", color = Color(0xFF6d5b5d))
              }
            }
          }
        } else {
          items(reports) { rpt ->
            Card(
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(16.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "TYPE: ${rpt.targetType.uppercase(Locale.getDefault())}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = crimson
                  )
                  Text(
                    text = rpt.status.uppercase(Locale.getDefault()),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (rpt.status == "open") crimson else Color(0xFF23643E)
                  )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Target: ${rpt.targetTitle}",
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Reason: ${rpt.reason}",
                  fontSize = 13.sp,
                  color = Color(0xFF6d5b5d)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text("Reported By: ${rpt.reportedByName}", fontSize = 11.sp, color = Color(0xFF6d5b5d))

                if (rpt.status == "open") {
                  Spacer(modifier = Modifier.height(12.dp))
                  Button(
                    onClick = { repository.resolveReport(rpt.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23643E)),
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text("Resolve & Flag Listing")
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (showUserDialog) {
    AlertDialog(
      onDismissRequest = { showUserDialog = false },
      title = { Text("Select Identity") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          repository.users.forEach { usr ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  repository.login(usr.id)
                  showUserDialog = false
                }
                .padding(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .background(Color(0x1A9E1B32), CircleShape),
                contentAlignment = Alignment.Center
              ) {
                Text(usr.name.first().toString(), color = crimson, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(usr.name, fontWeight = FontWeight.Bold)
                Text("Role: ${usr.role}", fontSize = 11.sp, color = Color(0xFF6d5b5d))
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showUserDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
