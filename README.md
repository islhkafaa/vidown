# Vidown

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-0.6.5-blue.svg?style=for-the-badge)

Vidown is an Android video downloader built with Jetpack Compose and `yt-dlp`. It provides background-resilient downloads and a minimalist video player.

## Features

- **yt-dlp Engine**: High-quality video merging via `youtubedl-android` and `ffmpeg`.
- **Engine Optimization**: Multi-threaded concurrent fragment fetching and configurable buffer sizes (v0.6.5).
- **History & Organization**: Platform-based filtering for download history.
- **Glassmorphism UI**: Premium frosted surfaces and fluid Material 3 Expressive motion.
- **Background Downloads**: Managed by WorkManager as a Foreground Service.
- **Auto-Updates**: Background extractor updates and direct GitHub OTA support.
- **Media Organization**: Platform-based filtering for download history.
- **Smart Automation**: Wi-Fi only download constraints and background maintenance.
- **Shared Transitions**: Smooth UI morphing between screens.
- **Player Gestures**: Vertical swipes for volume and brightness control.
- **Dynamic Color**: Material 3 (Monet) theme support.

## Tech Stack

- **UI**: Jetpack Compose, Material 3
- **Concurrency**: Kotlin Coroutines, Flow
- **Persistence**: Room Database, DataStore
- **Engine**: yt-dlp, FFmpeg
- **Tasks**: WorkManager

## Quick Start

1. Clone the repository.
2. Open in Android Studio (Ladybug or newer).
3. Build and run the `app` module.

## Building for Release

To build a signed release APK, you need to provide signing information in your `local.properties` file or as Gradle project properties:

```properties
RELEASE_STORE_FILE=/path/to/your/keystore.jks
RELEASE_STORE_PASSWORD=your_keystore_password
RELEASE_KEY_ALIAS=your_key_alias
RELEASE_KEY_PASSWORD=your_key_password
```

## APK Downloads

Current stable APKs are available in [GitHub Releases](https://github.com/islhkafaa/vidown/releases).
