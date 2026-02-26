# Changelog

All notable changes to this project will be documented in this file.

## [0.4.2] - 2026-02-26

### Added
- **Gallery History View**: Completely redesigned history screen using a 2-column staggered grid with visual thumbnails.
- **Minimalist Player**: Redesigned video player with clean overlay, quick 5s skip buttons, and auto-hiding controls.
- **Download Notifications**: Integrated active download queue into a notification bell icon (bottom sheet) and improved foreground notifications.
- **Start Animation**: Added visual pulse animation and snackbar feedback when starting a download.
- **Smart Versioning**: App version is now dynamically synced from `build.gradle.kts` across the UI.

### Changed
- Increased application version to `0.4.2` (Version Code `7`).
- Optimized APK output naming (e.g., `vidown-arm64-v8a-0.4.2.apk`).
- Removed the dedicated "Queue" bottom navigation tab.

### Fixed
- Resolved `ExoPlayer` seek increment build errors.
- Fixed layout and import issues in `MainActivity.kt`.

## [0.4.0] - 2026-02-25

### Added
- **Storage Access Framework (SAF)**: Users can now pick custom download locations on their device beyond standard folders via the `SettingsScreen`.
- **Concurrency Control**: Added setting to limit the number of simultaneous active downloads (1 to 5).
- **Default Resolution Preference**: Users can now prioritize exact resolutions (like 1080p, 720p, Audio Only).
- **Splash Screen**: Transitioned the app launch experience to the seamless Android 12+ `androidx.core:core-splashscreen` API using the custom provided logo.

### Changed
- Increased application version to `0.4.0` (Version Code `6`).
- Optimized `WorkManager` default initialization.

## [0.3.6] - 2026-02-25

### Added
- **Auto-Update System**: Integrated an Over-The-Air (OTA) update system powered by the GitHub Releases API.
- **Extractor Updates**: The underlying `yt-dlp` engine automatically pulls the latest stable extractors in the background every 3 days using Android's `WorkManager` so downloads never unexpectedly break.
- **Launch Check**: App automatically checks for new updates in the background on startup and gracefully prompts the user.
- **Settings Check**: Added a manual "Check for Updates" and "Update Download Extractors" button directly into the `SettingsScreen`.
- **Install Flow**: Downloads new `.apk` via `DownloadManager` and seamlessly triggers Android's native package installer using a FileProvider.

### Changed
- Increased application version to `0.3.6` (Version Code `5`).

## [0.3.5] - 2026-02-25

### Added
- **Integrated Media Player**: Built a first-party in-app video player using `androidx.media3` (ExoPlayer). Tapping a history item now opens a full-screen player inside Vidown, auto-locking to landscape with a floating back button.
- **Swipe-to-Delete History**: History items can now be swiped left to delete — removes both the database record and the file from device storage.

### Changed
- **Full UI/UX Redesign**: All screens (Home, Queue, History, Settings, Player, Navigation) have been comprehensively redesigned.
  - `HomeScreen`: Gradient header, pill-shaped inline search+fetch bar, full-bleed thumbnail hero with gradient scrim.
  - `QueueScreen` & `HistoryScreen`: Consistent card design with left-edge accent bar, status pill badges, and thicker animated progress bars.
  - `SettingsScreen`: Theme selection as stylish icon+radio cards plus a new About section showing the app version.
  - `Navigation Bar`: Icon-only, label-free for a minimal look; hidden entirely when in the Player.
- Increased application version to `0.3.5` (Version Code `4`).

## [0.2.5] - 2026-02-24

### Added
- **Download History**: Integrated Android Room to permanently SQLite record all completed and failed downloads, viewable via a new bottom navigation tab.
- **APK Optimization**: Enabled gradle ABI splits for native builds (`armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`). This aggressively strips unused architectures, reducing installation footprint from ~200MB+ down to ~50MB per unique Android device.

### Changed
- Increased application version to `0.2.5` (Version Code `2`).

## [0.2.2] - 2026-02-24

### Added
- **Settings Page**: Added a new settings screen leveraging Jetpack DataStore to toggle application themes (System Default, Light, Dark).
- **Background Foregrounds**: Upgraded the `DownloadWorker` to act as a proper Foreground Service with persistent, un-killable system notifications for long downloads.
- **Dynamic Extractor Updating**: Implemented a method to update the internal `yt-dlp` binary extractors over-the-air.
- **ProGuard Integrations**: Updated `proguard-rules.pro` to explicitly keep Room and WorkManager definitions, fixing R8 Full Mode crashing.

### Changed
- Increased application version to `0.2.2` (Version Code `1`).
- Fixed deprecated Material Icons (e.g. standardizing to `Icons.AutoMirrored.Rounded.List`).
- Resolved various pre-Android 10 `MediaStore` saving issues by introducing branching behavior for API 28 paths.
