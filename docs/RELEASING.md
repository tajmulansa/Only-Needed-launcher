# Releasing

How to turn this project into an app on Google Play. Maintainers only, but contributors may find it useful.

## Before the first upload

1. **Choose the final app name and application ID.**
   - The ID is `applicationId` in `app/build.gradle.kts`: `com.launcher.onlyneeded`. Never change it after the first Play upload. `namespace` is only the source package and stays `com.example.productivitylauncher`.
   - After the first upload to Google Play the ID can never change. Decide once.
   - If you change `namespace`, also move the Kotlin package folders and update the `package` lines and imports.
2. **Check the target API requirement.** Since 31 August 2026 new apps and updates must target Android 16 (API 36). Check the [official page](https://developer.android.com/google/play/requirements/target-sdk) before each release, the deadline moves yearly.
3. **Privacy policy.** Required by Google Play. State plainly: no accounts, no ads, no analytics, no network, data stays on the device, and what each optional permission is for.
4. **Store listing rules.**
   - Do not use other companies' brand names (for example social network names) in the title, description, keywords or screenshots.
   - Do not claim rankings such as "best" or "#1" in the title.
   - Describe the app as adding friction, not "blocking" apps, and keep the user in control.
5. **Replace the placeholder icon and the working title.**

## Signing

Google Play needs a signed Android App Bundle (AAB). Use **Play App Signing**: you sign uploads with an *upload key*, and Google manages the final signing key.

The release workflow signs the build when these repository secrets exist (Settings, Secrets and variables, Actions):

| Secret | Value |
| --- | --- |
| `KEYSTORE_BASE64` | Your upload keystore file, base64-encoded |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias |
| `KEY_PASSWORD` | Key password |

Create a keystore once with `keytool`, then encode it:

```bash
keytool -genkeypair -v -keystore upload.keystore -alias upload \
  -keyalg RSA -keysize 2048 -validity 10000
base64 -w 0 upload.keystore   # paste the output as KEYSTORE_BASE64
```

**Never commit the keystore or passwords.** `.gitignore` already excludes `*.keystore` and `*.jks`. Keep a private backup, losing the upload key makes updates harder.

Without these secrets the workflow still runs and produces unsigned files, which are useful for checking that release builds work.

## Making a release

1. Update `versionName` and increase `versionCode` in `app/build.gradle.kts` (Google Play requires a higher `versionCode` for every upload).
2. Merge to `main` once the **Build** workflow is green.
3. Run the **Release build** workflow from the Actions tab, or push a tag such as `v0.1.0`.
4. Download `release-files` from the finished run and upload the `.aab` in Google Play Console. Start with the **internal testing** track.
5. Test on real devices, then promote to production.

## Checklist

- [ ] Final name, application ID, icon
- [ ] `targetSdk` meets the current Play requirement
- [ ] `versionCode` is higher than the last upload
- [ ] Privacy policy URL ready
- [ ] Store listing has no third-party brand names or ranking claims
- [ ] Permissions in the manifest match the privacy policy
- [ ] Tested on at least two Android versions
