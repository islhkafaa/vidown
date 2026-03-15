# Vidown

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-0.5.5-blue.svg?style=for-the-badge)

Vidown is an Android video downloader built with Jetpack Compose and `yt-dlp`. It provides background-resilient downloads and a minimalist video player.

## Features

- **yt-dlp Engine**: High-quality video merging via `youtubedl-android` and `ffmpeg`.
- **Background Downloads**: Managed by WorkManager as a Foreground Service.
- **Auto-Updates**: Background extractor updates and direct GitHub OTA support.
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

## APK Downloads

Current stable APKs are available in [GitHub Releases](https://github.com/islhkafaa/vidown/releases).
