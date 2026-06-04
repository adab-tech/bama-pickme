package com.example.bamapickme.ui.map

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

fun checkMapsAvailable(): Boolean {
  return try {
    Class.forName("com.google.maps.android.compose.GoogleMap")
    true
  } catch (e: Throwable) {
    false
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapDetailScreen(
  title: String,
  latitude: Double,
  longitude: Double,
  isSafeZone: Boolean,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val useGoogleMaps = remember { checkMapsAvailable() }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Pickup Location Pin", fontWeight = FontWeight.Bold, color = Color(0xFF9E1B32)) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF9E1B32))
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFAFAF5))
      )
    },
    containerColor = Color(0xFFFAFAF5),
    modifier = modifier
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color(0xFF22181a),
        modifier = Modifier.padding(bottom = 8.dp)
      )

      Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .height(380.dp)
          .padding(vertical = 8.dp)
      ) {
        Box(modifier = Modifier.fillMaxSize()) {
          if (useGoogleMaps) {
            GoogleMapWrapper(
              latitude = latitude,
              longitude = longitude,
              isSafeZone = isSafeZone,
              title = title,
              modifier = Modifier.fillMaxSize()
            )
          }

          if (!useGoogleMaps) {
            // Fallback: Custom Canvas Map simulating the UA Campus Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
              val width = size.width
              val height = size.height

              // Draw Campus Grid background
              val gridSpacing = 60f
              for (x in 0..(width / gridSpacing).toInt()) {
                drawLine(
                  color = Color(0x0A22181a),
                  start = Offset(x * gridSpacing, 0f),
                  end = Offset(x * gridSpacing, height),
                  strokeWidth = 2f
                )
              }
              for (y in 0..(height / gridSpacing).toInt()) {
                drawLine(
                  color = Color(0x0A22181a),
                  start = Offset(0f, y * gridSpacing),
                  end = Offset(width, y * gridSpacing),
                  strokeWidth = 2f
                )
              }

              // Landmark reference circles
              drawCircle(color = Color(0xFFECE5D8), radius = 45f, center = Offset(width * 0.25f, height * 0.3f))
              drawCircle(color = Color(0xFFECE5D8), radius = 55f, center = Offset(width * 0.75f, height * 0.6f))

              val centerX = width / 2f
              val centerY = height / 2f

              if (isSafeZone) {
                drawCircle(color = Color(0x40D8B26E), radius = 70f, center = Offset(centerX, centerY))
                drawCircle(color = Color(0xFFD8B26E), radius = 12f, center = Offset(centerX, centerY))
                drawCircle(color = Color(0xFF9E1B32), radius = 6f, center = Offset(centerX, centerY))
              } else {
                drawCircle(color = Color(0x229E1B32), radius = 120f, center = Offset(centerX, centerY))
                drawCircle(
                  color = Color(0xFF9E1B32),
                  radius = 120f,
                  center = Offset(centerX, centerY),
                  style = Stroke(width = 4f)
                )
                drawCircle(color = Color(0xFF9E1B32), radius = 8f, center = Offset(centerX + 30f, centerY - 25f))
              }
            }

            Text(
              text = "Gorgas Quad",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF6d5b5d),
              modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, top = 64.dp)
            )

            Text(
              text = "Ridgecrest Complex",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF6d5b5d),
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 48.dp, bottom = 120.dp)
            )
          }

          // Top label bar on map
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .offset(y = if (isSafeZone) (-35).dp else (-55).dp)
              .background(
                color = if (isSafeZone) Color(0xFFD8B26E) else Color(0xFF9E1B32),
                shape = RoundedCornerShape(12.dp)
              )
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = if (isSafeZone) "🛡️ Verified Safe Zone" else "📍 Privacy Obscured Zone (50m)",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSafeZone) Color(0xFF22181a) else Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x0D9E1B32)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Pin Info",
              tint = Color(0xFF9E1B32),
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isSafeZone) "Public Exchange Safe-Spot" else "Private Student Housing Radius",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = Color(0xFF9E1B32)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = if (isSafeZone) {
              "This location is a pre-approved, university-monitored safe exchange zone. It is well-lit and covers high-traffic campus security corridors."
            } else {
              "To protect student security, the exact coordinates of this residential drop-off are fuzzed by 50 meters. Coordinate details: Lat: $latitude, Lon: $longitude."
            },
            fontSize = 14.sp,
            color = Color(0xFF6d5b5d),
            lineHeight = 20.sp
          )
        }
      }
    }
  }
}

@Composable
fun GoogleMapWrapper(
  latitude: Double,
  longitude: Double,
  isSafeZone: Boolean,
  title: String,
  modifier: Modifier = Modifier
) {
  val position = LatLng(latitude, longitude)
  val cameraPositionState = rememberCameraPositionState {
    this.position = CameraPosition.fromLatLngZoom(position, 16f)
  }

  GoogleMap(
    modifier = modifier,
    cameraPositionState = cameraPositionState
  ) {
    if (isSafeZone) {
      // Precise Star Pin for Public Safe Exchange Zone
      Marker(
        state = MarkerState(position = position),
        title = "🛡️ Safe Exchange: $title"
      )
    } else {
      // Privacy obscuring circle + marker offset slightly to protect residential locations
      val fuzzedPos = LatLng(latitude + 0.0002, longitude - 0.00015)
      Marker(
        state = MarkerState(position = fuzzedPos),
        title = "📍 Approximate Location"
      )
      Circle(
        center = position,
        radius = 50.0, // 50 meters
        fillColor = Color(0x229E1B32),
        strokeColor = Color(0xFF9E1B32),
        strokeWidth = 3f
      )
    }
  }
}
