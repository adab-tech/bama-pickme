# Security: exposed credentials

GitHub flagged that **API keys were visible in this public repository**. Treat them as compromised until you rotate them.

## Rotate immediately (Google Cloud / Firebase)

1. Open [Google Cloud Console](https://console.cloud.google.com/) → project **bamapickme**.
2. **APIs & Services → Credentials**:
   - Restrict or **delete** the exposed **Maps** key (used in `secrets.properties`).
   - Restrict or **delete** the exposed **Firebase Android** key (was in `google-services.json`).
3. Create **new** keys with:
   - Application restrictions (Android app + package `com.example.bamapickme`, or HTTP referrers for web).
   - API restrictions (only Maps SDK, Firebase APIs you use).
4. In [Firebase Console](https://console.firebase.google.com/) → Project settings → Your apps → download a **new** `google-services.json` into `app/` (file is gitignored).
5. Put the new Maps key in local `secrets.properties` and GitHub secret `GOOGLE_MAPS_API_KEY` (never commit the file).

## Optional: GitHub Actions

```powershell
# After you have a new google-services.json locally:
$json = Get-Content app\google-services.json -Raw
gh secret set GOOGLE_SERVICES_JSON --repo adab-tech/bama-pickme --body $json
gh secret set GOOGLE_MAPS_API_KEY --repo adab-tech/bama-pickme
```

## Other keys to rotate (if ever committed or shared)

- **OpportunityFinder**: Google Custom Search (`GOOGLE_API_KEY` / `GOOGLE_CSE_ID`) — [Google Cloud Credentials](https://console.cloud.google.com/apis/credentials).
- **Hausa AI / Desktop `.env`**: Gemini API keys — [Google AI Studio](https://aistudio.google.com/apikey) → revoke old, create new.

`secrets.properties` and `app/google-services.json` are **gitignored** going forward; use the `.example` templates only in git.
