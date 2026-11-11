# Design

> **The visual direction is the Still prototype**: [design/still-prototype.html](design/still-prototype.html). Open it in a browser at about 390 px wide and tap through. It is the source of truth: when the app and the prototype disagree, the prototype wins. The app is named "Only Needed" and has an icon; the application ID is still an open decision.

## Principles

1. **Minimal.** Text first, few elements, lots of space. Flat: no filled cards, hairlines instead of boxes.
2. **One list, next action first.** Home shows the next task; everything else is a swipe away.
3. **Friction is asymmetric.** Distracting apps are hard to open, everyday apps are one tap.
4. **Gentle.** Encourage, never shame. The user can always leave the gate with one tap.
5. **Honest and private.** Local data only, plain-language permission explanations.
6. **Students.** Language and features fit study, planning and sleep.

## Visual language (Still)

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| `phone` (bg) | `#ECEDEA` | `#0B0C0B` | Screen background |
| `card` (bg2) | `#E1E3DF` | `#161816` | Search pill, segmented track, soft fills |
| `text` (fg) | `#111312` | `#EEF0EC` | Text, primary buttons, rings |
| `muted` | `#7B7F79` | `#777B74` | Captions, secondary text |
| `gate` (warn) | `#B5502C` | `#E27A52` | Gated apps and urge/lock screens only |
| `line` | text at ~13% | text at ~13% | Hairlines, tracks |

- **Fonts:** Bricolage Grotesque for large text (>= 19sp), Geist for body, Geist Mono for captions, times and numbers. All bundled (SIL OFL, see `OFL-*.txt`). Tabular figures for clocks and timers.
- **Type scale:** page title 44sp Medium; step heading 34sp Medium; clock 112sp Light; ring/timer 56-60sp Light; list names 28sp (Home) and 24sp (drawer, pickers); body 17sp; secondary 15sp; small 13sp; caption 11sp mono uppercase with 1sp tracking. The same role always has the same size.
- **Layout:** 28dp gutters, 1dp hairlines, pills and circles (no rounded cards), 30dp top corners on the drawer sheet.
- **Themes:** Auto, Light, Dark. **Look** filters: Standard, Gray, Warm, Contrast (a colour filter over the whole app).
- **Motion:** slow and soft (cubic-bezier .32,.72,0,1). Rolling clock digits, staggered entrances, hairlines that draw in, pager parallax. One switch (Animations) turns all of it into instant changes. There is no app-opening animation.
- **Accessibility:** red/terracotta is never the only signal (gated apps also carry a "gated" label); touch targets at least 44dp; text keeps good contrast in both themes.

## Screens

| Screen | Purpose |
| --- | --- |
| Home (page 1) | Clock, greeting with name, Up next, six Home apps, bottom bar |
| Study (page 2) | Focus ring, week dots, screen-time bars, Tasks, Habit stack, other apps' widgets |
| All apps | Bottom-sheet drawer with search and A to Z list |
| Quick actions | Top sheet: toggles, noise player, Theme, Look |
| Gate | Breathe, type "I choose to open <app>", choose minutes |
| Locked, Time's up | Daily limit or focus lock; session finished |
| Break (red button) | 5 minute full-screen lock with Call and Message |
| Focus, Evening shutdown, Weekly review | Session timer; close the day; seven-day review |
| Settings | Appearance, gate, focus and rhythm (incl. wake and bedtime), apps, red button, permissions, data |
| Setup | 7 steps: welcome, apps to pause, Home apps, name, wake and bedtime, theme and look, privacy |
| Add widget | Habit stack and widgets from other apps |

Known limits: the gate only guards gated apps opened from this launcher (Android does not let a launcher block an app opened from a notification or Recents without an accessibility service). The floating timer needs "Display over other apps", screen time needs "Usage access", Silent needs Do Not Disturb access. Each is optional.

