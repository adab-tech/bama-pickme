package com.example.bamapickme.security

import kotlin.math.cos
import kotlin.random.Random

object LocationFuzzer {

  // Coordinates centered around University of Alabama campus
  data class SafeZone(val name: String, val description: String, val latitude: Double, val longitude: Double)

  val campusSafeZones = listOf(
    SafeZone("Ferguson Student Center Lobby", "Well-lit indoor public lounge area.", 33.2114, -87.5458),
    SafeZone("UA Police Department Lobby", "24/7 monitored secure exchange point.", 33.2085, -87.5401),
    SafeZone("Rodgers Science & Engineering Library", "Public library study rooms and lobby.", 33.2132, -87.5432),
    SafeZone("Ridgecrest South Courtyard", "Open outdoor student courtyard area.", 33.2161, -87.5424),
    SafeZone("Gorgas Library Main Plaza", "Open, busy public square in front of Gorgas library.", 33.2118, -87.5481)
  )

  /**
   * Applies random jitter to latitude/longitude coordinates to protect user privacy.
   * Displaces the coordinate by up to 50 meters (approx 0.00045 degrees).
   */
  fun fuzzCoordinates(latitude: Double, longitude: Double, isSafeZone: Boolean): Pair<Double, Double> {
    if (isSafeZone) {
      return Pair(latitude, longitude) // Safe Zones are public and do not need fuzzing
    }

    // Convert 50 meters displacement into degrees
    val earthRadius = 6378137.0 // in meters
    val maxOffsetMeters = 50.0

    // Random displacement in polar coordinates
    val r = Random.nextDouble(0.0, maxOffsetMeters)
    val theta = Random.nextDouble(0.0, 2 * Math.PI)

    val deltaLat = r * Math.sin(theta) / earthRadius
    val deltaLon = r * Math.cos(theta) / (earthRadius * cos(Math.toRadians(latitude)))

    val fuzzedLat = latitude + Math.toDegrees(deltaLat)
    val fuzzedLon = longitude + Math.toDegrees(deltaLon)

    return Pair(
      Math.round(fuzzedLat * 1000000.0) / 1000000.0, // round to 6 decimals (10cm accuracy)
      Math.round(fuzzedLon * 1000000.0) / 1000000.0
    )
  }

  /**
   * Checks if coordinates match any safe zone.
   */
  fun getMatchingSafeZone(latitude: Double, longitude: Double): SafeZone? {
    val threshold = 0.0001 // Approx 10 meters tolerance
    return campusSafeZones.find {
      Math.abs(it.latitude - latitude) < threshold && Math.abs(it.longitude - longitude) < threshold
    }
  }
}
