# GitHub repository secrets

Repository: [adab-tech/bama-pickme](https://github.com/adab-tech/bama-pickme)

## Actions

| Secret | Required | Purpose |
|--------|----------|---------|
| `GOOGLE_MAPS_API_KEY` | Recommended | Maps SDK (see `secrets.properties.example`) |

| `GOOGLE_SERVICES_JSON` | Recommended | Full contents of `app/google-services.json` (file is **not** in git) |

Do **not** commit `app/google-services.json`. Copy from Firebase Console locally; sync to Actions with the command in `SECURITY.md`.

**Release signing (optional, not in CI yet):**

| Secret | Purpose |
|--------|---------|
| `ANDROID_KEYSTORE_BASE64` | Base64-encoded `.jks` / `.keystore` |
| `ANDROID_KEYSTORE_PASSWORD` | Keystore password |
| `ANDROID_KEY_ALIAS` | Key alias |
| `ANDROID_KEY_PASSWORD` | Key password |

```powershell
gh secret set GOOGLE_MAPS_API_KEY --repo adab-tech/bama-pickme --body "YOUR_MAPS_KEY"
```

**Local:** copy `secrets.properties.example` → `secrets.properties` and add your key.

**Test on device/emulator:**

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
adb install app\build\outputs\apk\debug\app-debug.apk
```
