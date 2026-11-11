# AGENTS.md

The single source of project knowledge for AI assistants and contributors. Read it first.

## What this is

**Only Needed**: a minimal Android launcher for anyone who wants to use their phone with intention, not muscle memory. Home shows the clock, the date and exactly six apps. Any other app is found by typing its full name, then choosing how long to use it. A red button gives a calm five minute break. Nothing else.

Kotlin, Jetpack Compose, minSdk 26, targetSdk 36. Package/namespace `com.example.productivitylauncher` (source package only), applicationId `com.launcher.onlyneeded` (permanent after the first Play upload). Sister project (separate repo, on hold): the student launcher "minimal-launcher" with many more features. This app was started as a stripped copy of it.

## Working rules

1. No `INTERNET` permission, no network code, no tracking libraries. Optional permissions must degrade gracefully when denied.
2. The owner builds from a phone with no PC. Every build and release runs in GitHub Actions. Give file paths and full code, never zips.
3. Keep scope tiny. Do not add features the owner has not asked for. Features come only when he says so.
4. UI: copy existing components (`SettingRow`, `PageHeader`, `Caption`, `Toggle`, `Components.kt`), colours from `AppColors`, fonts from `Theme.kt`, animation only through `Motion.*`. Support light and dark and all four Looks. The look follows `docs/design/still-prototype.html` but is being tweaked and made more polished; animations stay and get richer.
5. Do not hard-code the app name (use `@string/app_name`), the applicationId or the icon.
6. Update this file when behaviour or rules change.

## Behaviour

- **Home:** date (mono caps), settings gear top right, 112sp odometer clock, up to six app names (28sp, tap opens at once), "Add an app" row while fewer than six, a quiet one-line "Screen time today" total (needs Usage access), then the bottom bar: "Find an app" pill, Phone, Messages. Swipe up opens Find an app; swipe down opens Settings (`verticalSwipes`). Rows that do not fully fit are dropped by `FitColumn`.
- **Find an app (drawer):** a bottom sheet with one text field. Nothing is listed until the full name of an app is typed exactly (case and edge spaces ignored, `exactNameMatches`). Long-press a result: Add to Home / App info.
- **Opening an app:** Home apps open at once. Any other app goes to `Route.Time`: choose 5, 10, 15, 30 or custom minutes, then the app opens and `SessionOverlayService` shows a small floating timer (needs Display over other apps). When time is up, `Route.TimesUp` on return ("Back to Home" or "I need a bit more").
- **Red button:** on by default. `UrgeButtonService` draws a red dot over every app; two taps start a 5 minute lock (`urge_end`). `UrgeScreen` is a landscape Steady-style flip clock counting MM:SS with only Phone and Messages. Strict guard: during the lock `UrgeButtonService` keeps a 1px overlay and every 400 ms checks the foreground app through UsageStats; anything except the launcher, phone, messages, system UI and keyboard is sent back. `NotificationGuardService` (Notification access) dismisses notifications during the lock. Android cannot kill apps, so this works by bringing the launcher back.
- **Setup:** welcome, pick six Home apps, permissions, theme and look.
- **Settings:** theme, look, animations, Home apps, red button, permissions (Usage, Display over apps, Notification access, default home app), privacy policy, delete all data, version.

## Layout

```
app/src/main/java/com/example/productivitylauncher/
  MainActivity.kt          single HOME activity, 390dp design width scaling
  data/                    LauncherState (SharedPreferences "launcher"), Logic, System, Usage, AppEntry
  service/                 SessionOverlayService, UrgeButtonService, NotificationGuardService
  ui/                      LauncherApp (routing), Nav, home/, apps/, gate/ (TimeScreen, TimesUp), urge/, steady/ (flip clock helpers only), settings/, onboarding/, components/, theme/
```

SharedPreferences keys: `onboarded, theme, look, animations, home_apps, urge_button, urge_end, session_pkg, session_end, session_notified`.

## Build and test

Push to a branch; the **Build** workflow runs `assembleDebug testDebugUnitTest lintDebug`. **Screenshots** runs an emulator and pushes PNGs to the `shots-out` branch (`.github/scripts/shots.sh`; adb activity component is `com.launcher.onlyneeded/com.example.productivitylauncher.MainActivity`). **Release build** makes a signed AAB when the signing secrets exist (`docs/RELEASING.md`).

## Open decisions

Final Play title, icon is the two-tone check, pricing (free with one-time unlock via Play Billing is the plan, not built yet), study-channel and other extras only on request.
