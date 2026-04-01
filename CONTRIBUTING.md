# Contributing to Vidown

Thank you for your interest in contributing to Vidown! We welcome bug fixes, feature requests, and UI improvements.

## Development Setup

1. **Prerequisites**:
   - Android Studio Ladybug or newer.
   - JDK 21.
2. **Setup**:
   - Clone the repository.
   - Run a Gradle Sync.
   - Build and run the `app` module on an emulator or physical device.

## Project Structure

Vidown follows a modern Android architecture using Jetpack Compose:

- `app.vidown.ui`: Contains all UI screens, components, and themes.
- `app.vidown.data`: DataStore, Repository, and Room Database implementations.
- `app.vidown.domain`: Business logic, background workers (`yt-dlp` integration), and core models.
- `app.vidown.ui.viewmodel`: State management for all screens.

## Guidelines

- **Kotlin Style**: Follow the official Kotlin coding style (`kotlin.code.style=official`).
- **Compose**: Use the existing `GlassSurface` and `VidownTheme` components for all new UI to maintain the glassmorphism aesthetic.
- **Localization**: Never hardcode strings in the UI. Always add them to `strings.xml` and ideally provide an Indonesian translation in `values-in/strings.xml`.
- **yt-dlp**: If modifying download logic, ensure it remains compatible with the `youtubedl-android` wrapper and handles `FFmpeg` merging correctly.

## Workflow

1. Fork the repository.
2. Create a new branch (`feat/your-feature` or `fix/your-fix`).
3. Commit your changes with clear, imperative messages (e.g., "Add support for dark mode toggle").
4. Push to your fork and submit a Pull Request.

## Environment & Tooling

The project uses GitHub Actions for continuous integration. Ensure your code passes lint and unit tests locally before submitting a PR:

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
```

By contributing, you agree that your code will be licensed under the MIT License.
