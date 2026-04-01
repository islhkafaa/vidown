# Changelog

All notable changes to this project will be documented in this file.

## [0.6.7] - 2026-04-01

### Added

- **Multi-Language Support**:
  - Added support for **Indonesian** (Bahasa Indonesia).
  - Added a **Language** setting in the Appearance menu.
  - Implemented runtime language switching so the UI updates instantly.
- **Queue Management**:
  - Added **Drag-to-Reorder** for pending downloads.
  - Added haptic feedback when reordering items.
- **UI Consistency**:
  - Updated **Settings** titles to use proper casing (e.g., "Appearance").
  - Localized accessibility labels and error messages.

### Changed

- **MainActivity Migration**: Upgraded `MainActivity` to extend `AppCompatActivity` to support modern Android per-app language preferences.
- **Stable Filtering**: Refactored History filtering logic to use internal keys, ensuring source filters work correctly regardless of the selected language.

## [0.6.5] - 2026-03-24

### Added

- **Dynamic Format Selection**:
  - Added `FormatSelectionSheet` to choose video and audio quality.
  - Users can now select specific video and audio formats (resolutions, codecs, sizes) for each download.
  - Added intelligent format labeling: "High Quality" (DASH streams) and "Direct" (Progressive streams).
  - Categorized selection UI grouping formats into Video and Audio sections.
- **Download Engine Optimization**:
  - **Concurrent Fragments**: Added support for multi-threaded fetching (up to 16 connections) for DASH/HLS streams.
  - **Buffer Tuning**: Integrated configurable buffer and chunk sizes (`Standard`, `High`, `Extreme`) for optimized throughput.
  - **Network Refinements**: Added a "Force IPv4" toggle to bypass ISP-specific IPv6 routing performance bottlenecks.
  - Improved HLS merging performance using `--hls-use-mpegts`.

### Changed

- **UI & UX**:
  - Updated **Settings** to use the new `GlassSurface` component.
  - Updated toggle rows with circular icons.
  - Fixed **Snackbar** positioning to avoid overlapping navigation bars.
- **UI Simplification**:
  - Removed the "Default Quality" preference from settings as it's no longer necessary with per-download selection.
  - Consolidated the download flow into a single "Download Options" interaction.
- **Improved Metadata Labels**:
  - Refined `Format` domain model with `friendlyLabel` logic to strip technical jargon (e.g., "DASH") for a cleaner experience.

### Removed

- Deprecated "Always Ask" and "Always Best Video" resolution logic in favor of the new dynamic selection.

## [0.6.0] - 2026-03-15

### Added

- **UI Modularization**:
  - Completely refactored `HomeScreen` and `HistoryScreen` logic into a new `app.vidown.ui.component` package.
  - Introduced `GlassSurface` as a centralized component for consistent glassmorphism across the app.
- **Media Organization**:
  - Added platform-based filtering in the History screen (YouTube, TikTok, Instagram, Facebook, etc.).
  - Introduced scrollable `FilterChip` UI for quick navigation of downloaded content.
- **Smart Automation**:
  - **WiFi-Only Downloads**: Added a new setting to restrict downloads to unmetered networks, preserving cellular data.
  - **Auto-Update Engines**: Integrated a background maintenance worker (`MaintenanceManager`) that periodically keeps the `yt-dlp` extractors updated silently.
  - New automation toggles added to the Settings screen for user control.

### Fixed

- Improved navigation import stability and layout consistency across different screen densities.

## [0.5.5] - 2026-03-15

### Added

- **Shared Transitions**:
  - Implemented `SharedTransitionLayout` across the navigation flow.
  - Smooth morphing animation when transitioning from Home or History thumbnails to the Player Screen.
- **Advanced Player Gestures**:
  - Vertical swipe gestures for Volume control on the right half of the player.
  - Vertical swipe gestures for Brightness control on the left half of the player.
  - Custom visual indicators (overlays) providing real-time feedback for gesture adjustments.
- **Smart Features**:
  - **Clipboard Auto-Fetch**: App now detects video URLs in the clipboard and suggests pasting them for faster downloads.
  - **Audio Meta-Tagging**: Downloaded audio files automatically include metadata (Title, Artist/Uploader) and embedded thumbnail as cover art.

### Changed

- **Player UI Polishing**:
  - Externalized all hardcoded strings to `strings.xml` for better localization and accessibility.
  - Fixed Play/Pause icon tint to consistent white.
  - Improved layout alignment of time labels and slider controls.
- **Project Maintenance**:
  - Optimized imports and cleaned up redundant boilerplate in `MainActivity` and `PlayerScreen`.

## [0.5.2] - 2026-03-10

### Changed

- **Glass Design System**:
  - Rebuilt the application with a consistent design language.
  - Implemented translucency and gradient borders across cards and bars.
- **Enhanced Navigation**:
  - Re-engineered the bottom navigation bar into a compact, centered, floating "Glass Pill".
  - Custom circular selection indicators and reduced item padding for a modern, minimal look.
- **Improved Feedback**:
  - Linear fetching indicator added to the top of the home screen content for subtle yet clear user feedback.
  - Revamped notification badge in the top bar with correct scaling and un-clipped numbers.

## [0.5.0] - 2026-02-27

### Added

- **Download Speed & ETA**: Real-time download speed and estimated time remaining (ETA) are now displayed in both the UI and system notifications.
- **Improved History Experience**:
  - Search bar to quickly find downloads by title or uploader.
  - Auto-retry button on failed downloads.
- **Interactive Playlists**:
  - Full support for YouTube playlists and search results.
  - Clickable playlist entries to fetch specific video details/formats.
  - Improved "Download All" stability using `dump-single-json`.
- **Auto-Retry**: Downloads now automatically retry up to 3 times on transient network failures.
- **Share to Vidown**: Accept URLs shared from other apps directly, opening the download dialog immediately.

### Changed

- **Concurrent Downloads**: Improved setting to use text input (1-20) instead of a fixed dropdown for more flexibility.
- Increased application version to `0.5.0` (Version Code `8`).

### Fixed

- **Better Cancellation**: Optimized cancellation logic to reliably stop tasks and clean up resources immediately.
- **Playlist Resolution**: Ensured playlist items use the correct webpage URL for reliable extraction.
- **UI Stability**: Resolved various "Unresolved Reference" lint errors and improved icon consistency across screens.

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
