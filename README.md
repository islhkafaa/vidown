# Vidown

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Version](https://img.shields.io/badge/version-0.5.0-blue.svg?style=for-the-badge)

Vidown is a modern, background-resilient Android video downloader built with Jetpack Compose, Material 3, and `yt-dlp`.

## Features
- **Core Downloader Engine**: Uses `youtubedl-android` and `ffmpeg` to download and merge high-quality video formats seamlessly.
- **Background Resiliency**: Active downloads are managed by WorkManager as an Expedited Foreground Service, ensuring the OS doesn't kill the task.
- **Auto-Updating**: Vidown periodically updates its own `yt-dlp` extractors in the background so downloads never suddenly break, and offers direct in-app OTA APK updates by querying GitHub Releases.
- **Dynamic Theming**: Features full Android 12+ Monet support matching your wallpaper, plus customizable Light/Dark/System themes via Jetpack DataStore.
- **MediaStore Native**: Downloads automatically persist to public `Videos/Vidown` and `Music/Vidown` directories reliably across Android 9+.

## Tech Stack
- Jetpack Compose
- Kotlin Coroutines & Flow
- WorkManager
- DataStore Preferences
- Material 3 Design
