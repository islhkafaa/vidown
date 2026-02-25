# Changelog

All notable changes to this project will be documented in this file.

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
