# Design prototype

`still-prototype.html` is the visual and motion spec for the app, called **Still**. It is one self-contained HTML file (no build step, no network). Open it in a phone or desktop browser at about 390 px wide.

It shows Home, the Today/Study page with the focus ring, week dots, screen time and the to-do list, the All apps drawer, Quick actions, Settings and the three gate steps.

## How to use it

- It is the source of truth. When the Android app and the prototype disagree, the prototype wins (spacing, type sizes, motion timing, copy).
- Every colour and font is a CSS variable at the top of the file. The Android equivalents are in `ui/theme/Theme.kt`.
- The Android app is a 1:1 port: the whole UI is scaled so the screen is 390 units wide (see `MainActivity.kt`), which makes every proportion match this file on any phone.
- The prototype hard-codes the name "Tajmul"; the app asks for a name in setup.
- Where the app intentionally differs: the Quick actions toggles, the Study page name, the habit stack and noise player, the sleep-time setup, and the gate phrase being "I choose to open <app>" (same as the prototype).
