package com.example.bamapickme.security

import java.util.Locale

object ScamDetector {
  private val SCAM_KEYWORDS = listOf(
    "venmo", "cashapp", "paypal", "zelle", "cash app", "wire transfer",
    "western union", "moneygram", "gift card", "steam card", "amazon card",
    "shipping fee", "shipping payment", "courier fee", "pay me first",
    "advance fee", "refundable deposit", "whatsapp me at", "text me at",
    "telegram", "external link", "phishing", "click here", "buy now"
  )

  /**
   * Scans a title and description for suspicious scam signatures.
   * Returns a pair of (isScam, reason).
   */
  fun scanListing(title: String, description: String): Pair<Boolean, String?> {
    val combinedText = "$title $description".lowercase(Locale.getDefault())

    // 1. Check for suspicious keywords
    for (keyword in SCAM_KEYWORDS) {
      if (combinedText.contains(keyword)) {
        return true to "Contains suspicious payment or communication reference: '$keyword'."
      }
    }

    // 2. Check for generic URLs which are often used for phishing in campus markets
    val hasUrl = combinedText.contains("http://") || 
                 combinedText.contains("https://") ||
                 (combinedText.contains(".com") && !combinedText.contains("ua.edu")) ||
                 (combinedText.contains(".net") && !combinedText.contains("ua.edu")) ||
                 combinedText.contains(".org") ||
                 combinedText.contains(".xyz")

    if (hasUrl) {
      return true to "Contains external link or non-campus web address. Only campus links (.ua.edu) are permitted."
    }

    // 3. Check for typical spam characteristics (e.g. excessive capitalization or length patterns)
    val originalText = "$title $description"
    val upperCaseRatio = originalText.count { it.isUpperCase() }.toFloat() / originalText.length.coerceAtLeast(1)
    if (upperCaseRatio > 0.4f && originalText.length > 30) {
      return true to "Suspicious formatting (excessive uppercase letters)."
    }

    return false to null
  }

  /**
   * Detects duplicate listings to prevent spam flood.
   */
  fun isDuplicate(
    title: String,
    description: String,
    existingTitlesAndDescriptions: List<Pair<String, String>>
  ): Boolean {
    val normTitle = title.trim().lowercase(Locale.getDefault())
    val normDesc = description.trim().lowercase(Locale.getDefault())

    for ((eTitle, eDesc) in existingTitlesAndDescriptions) {
      val normETitle = eTitle.trim().lowercase(Locale.getDefault())
      val normEDesc = eDesc.trim().lowercase(Locale.getDefault())

      if (normTitle == normETitle && normDesc == normEDesc) {
        return true
      }
    }
    return false
  }
}
