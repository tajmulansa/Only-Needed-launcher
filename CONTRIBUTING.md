# Contributing

Thanks for helping! This project is built by a solo student developer and welcomes contributors of every level. You do not need a computer: everything below also works from a phone.

## Ways to contribute

- **Code:** pick an issue labeled `good first issue` or `help wanted`.
- **Design:** mockups, color and layout ideas (read [docs/DESIGN.md](docs/DESIGN.md) first).
- **Writing:** the pet's lines, onboarding text, README and docs improvements.
- **Testing:** try the debug APK on your device and report bugs with the Android version and phone model.
- **Ideas:** open a "Feature or idea" issue. Explain the student problem it solves.

## Ground rules

These keep the product coherent. PRs that break them will be asked to change.

1. **Minimal and calm.** Text first. No clutter, no decorative extras.
2. **Gentle, never shaming.** The pet and any message may encourage, but must not guilt, scare, or punish. No "failed" states.
3. **Student first.** Features should help students study, plan, or sleep better.
4. **Private by default.** No network access, accounts, ads, or analytics. Do not add the INTERNET permission or a tracking library. If you think something needs network access, open an issue first.
5. **The user stays in control.** Gated apps are always chosen by the user. Nothing hidden, nothing that blocks apps without consent. Weakening a rule can be delayed, never impossible.
6. **Red is only for gated apps.** `AppColors.Danger` marks distracting apps and nothing else.
7. **English only for now.** Localization is out of scope until v1 ships.
8. **Optional permissions stay optional.** Anything needing Usage Access or Notifications must degrade gracefully when denied.

## Set up

### From a phone (no PC)

1. Fork the repository on GitHub.
2. Edit files in the GitHub web editor (tap a file, then the pencil icon) and commit to a new branch.
3. Open the **Actions** tab on your fork, run the **Build** workflow, and download the `debug-apk` artifact to test.
4. Open a pull request when the build is green.

### On a computer

1. Install JDK 17 and Android Studio.
2. Fork and clone the repo, then open it in Android Studio.
3. Run on an emulator or device, or use `./gradlew assembleDebug`.

Before opening a pull request, run:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

## Workflow

1. **Discuss first** for anything bigger than a small fix: open or comment on an issue so nobody wastes time.
2. **Branch** from `main`. Use a clear name, such as `feat/frog-timer`, `fix/app-list-crash`, or `docs/readme-typos`.
3. **Commit** with short, present-tense messages. Prefixes help: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
4. **Pull request** using the template. Keep it focused on one change. Include a screenshot for any UI change.
5. **Review.** A maintainer will review it. Please be patient, this is a small team.

## Code style

- Follow the [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) and the `.editorconfig` in the repo.
- Keep composables small. Screens live in `ui/<feature>/`, shared pieces in `ui/components/`.
- Use `AppText` for text and tokens from `AppColors`. Never hard-code colors or fonts in a screen.
- Touch targets must be at least 48dp tall. Text must stay readable (contrast of at least 4.5:1 for normal text).
- Put logic that does not need Android (filtering, scheduling rules, pet decisions) in plain Kotlin so it can be unit tested.
- Prefer clear code over clever code. Comment the *why*.

## Tests

- Add JVM unit tests under `app/src/test/` for new logic. See `AppFilterTest` as an example.
- UI changes: describe how you tested them by hand in the pull request.

## Writing pet lines

When the pet arrives, its lines are part of the product. Good lines are:

- **Short**, one sentence where possible
- **Friendly and specific**, tied to what the user is doing ("2 minutes on your frog?")
- **Encouraging, never guilt-tripping.** Say "not done yet", not "you failed"
- **Rare.** The pet has a hard daily cap and a quiet mode, so each line must earn its place

## Dependencies

Add new libraries only when needed and open an issue first. Versions go in `gradle/libs.versions.toml`. Libraries must be compatible with the MIT license and must not add network access or tracking.

## Licensing

By contributing, you agree that your contribution is licensed under the [MIT License](LICENSE) of this project.

## Questions

Open an issue. There are no silly questions here.
