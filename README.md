# Games (Light Phone III)

A small games tool for LightOS, built with the Light SDK. Three games: Snake, Sudoku, and Word Search.

## What's in it

- **Snake** - swipe to steer. No per-attempt limit, but capped at 20 minutes of playtime per day.
- **Sudoku** - 3 puzzles a day. If you back out mid-puzzle, it resumes right where you left off instead of starting over or costing you another attempt.
- **Word Search** - same daily limit and resume behavior as Sudoku.
- **Settings** - one option right now: invert colors (dark/light).

All progress and daily limits are stored locally on-device. Nothing leaves the phone.

## Installing

Grab the latest APK from this repo's [Releases](../../releases) page and install it directly.

This is self-signed, informal distribution - separate from Light's official Tool Library approval process. If you're using something like Obtainium to track and auto-update from this repo, point it at the repo URL and it'll pick up new releases as they're tagged.

## Building from source

Only needed if you want to modify the code yourself.

Requirements:
- Android Studio
- Android SDK with the API 34/36 platform installed
- A GitHub personal access token with `read:packages` scope (the SDK pulls its keyboard component from GitHub Packages, so Gradle needs this to sync)

Steps:
1. Clone this repo.
2. Create a `local.properties` file in the repo root with:
   ```
   sdk.dir=/path/to/your/Android/Sdk
   gpr.user=your_github_username
   gpr.key=your_github_token
   ```
3. Open the repo root in Android Studio and let Gradle sync.
4. Pick the `tool` run configuration, target an emulator or a Light Phone III in developer mode, and run.

For emulator testing, an AVD around 1080x1240 (API 34, no Google Play Services) is closest to the real device's screen. See `docs/system_app` in this repo if you want to run the actual LightOS emulator shell instead of a plain Android emulator.

## Known limitations

- **No custom app icon yet.** This version of the SDK doesn't expose a way to set a launcher icon - the generated manifest never references one, and there's no `icon` field in `lighttool.toml`. The icon artwork is already built and sitting in `tool/src/main/res/` (adaptive icon + a Public Sans "G"), ready to wire up the moment icon support exists.
- **Splash screen has a hard ~1 second minimum.** That's enforced in the SDK's own `LightActivity`, not something a tool can override. The splash itself is just a plain black screen with nothing on it.

## Releasing a new version

1. Bump `versionCode` and `versionName` in `tool/lighttool.toml`, commit. `versionCode` has to strictly increase each release, or Android won't recognize it as an update.
2. Tag the commit and push the tag:
   ```
   git tag v1.1.0
   git push origin v1.1.0
   ```
3. `.github/workflows/release.yml` builds the APK and publishes it as a GitHub Release with the APK attached automatically.

The only repo secrets this needs are `GH_PACKAGES_USER` and `GH_PACKAGES_TOKEN` (same GitHub Packages credentials used locally) - both are already required just to build the project at all, so there's nothing extra to set up.
