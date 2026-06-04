package com.example.bamapickme

import com.example.bamapickme.security.LocationFuzzer
import com.example.bamapickme.security.ScamDetector
import org.junit.Assert.*
import org.junit.Test

class SecurityUnitTest {

  @Test
  fun testScamDetectorFiltersScamKeywords() {
    val scamListing = "Post a working microwave but please pay me on Venmo for delivery"
    val (isScam, reason) = ScamDetector.scanListing("Microwave", scamListing)

    assertTrue(isScam)
    assertNotNull(reason)
    assertTrue(reason!!.contains("Venmo", ignoreCase = true))
  }

  @Test
  fun testScamDetectorFiltersPhishingUrls() {
    val phishListing = "Get free books by logging in here: http://badlink.com/ua-login"
    val (isScam, reason) = ScamDetector.scanListing("Free Books", phishListing)

    assertTrue(isScam)
    assertNotNull(reason)
    assertTrue(reason!!.contains("external link", ignoreCase = true))
  }

  @Test
  fun testScamDetectorAllowsSafeListing() {
    val safeListing = "Chemistry 101 textbook. Normal highlighting, clean pages. Meeting near Gorgas Library."
    val (isScam, reason) = ScamDetector.scanListing("Chemistry 101 Book", safeListing)

    assertFalse(isScam)
    assertNull(reason)
  }

  @Test
  fun testLocationFuzzerPreservesSafeZones() {
    // Ferguson Center coordinates
    val lat = 33.2114
    val lon = -87.5458

    val (fLat, fLon) = LocationFuzzer.fuzzCoordinates(lat, lon, isSafeZone = true)

    assertEquals(lat, fLat, 0.0001)
    assertEquals(lon, fLon, 0.0001)
  }

  @Test
  fun testLocationFuzzerObscuresPrivateCoordinates() {
    val lat = 33.1234
    val lon = -87.4321

    val (fLat, fLon) = LocationFuzzer.fuzzCoordinates(lat, lon, isSafeZone = false)

    // Verify coordinates are modified (fuzzed)
    assertNotEquals(lat, fLat)
    assertNotEquals(lon, fLon)

    // Verify distance offset is small (within bounding region)
    assertTrue(Math.abs(fLat - lat) < 0.005)
    assertTrue(Math.abs(fLon - lon) < 0.005)
  }
}
