# Bama PickMe

Android app for the Alabama Community Exchange — students give away items they no longer need, post requests for things they do, and hand them off at campus safe zones.

[![Portfolio](https://img.shields.io/badge/Adamu_Abubakar-adamu.tech-0f766e?style=flat-square)](https://adamu.tech)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android)](https://developer.android.com/)

---

## Overview

Bama PickMe is a campus donation and request board built with Kotlin and Jetpack Compose. Donors post items (textbooks, microwaves, dorm gear), seekers post what they need, and the two coordinate a pickup in the app instead of trading phone numbers.

## Features

- Donation listings and seeker requests with categories, urgency and handoff windows
- Map of pickup pins with suggested safe zones; exact locations are fuzzed until a handoff is arranged
- Reserve/claim flow that opens a private coordination chat
- Reporting and moderation queue for scams, duplicates and payment requests
- In-app AI helper for finding listings, safety rules and recycling points
- Firebase Auth (university email), Firestore and Cloud Storage, plus demo identities for trying the app without an account

## Quick start

```bash
git clone https://github.com/adab-tech/bama-pickme.git
cd bama-pickme
# Open in Android Studio · sync Gradle · run on device/emulator
```

## Tech stack

| Layer | Tools |
|-------|--------|
| Mobile | Kotlin, Jetpack Compose, Android SDK, Gradle |
| Backend | Firebase Auth, Firestore, Cloud Storage |
| Maps | Google Maps (maps-compose) |

## Author

**Adamu Abubakar** · [adamu.tech](https://adamu.tech) · [contact@adamu.tech](mailto:contact@adamu.tech)
