# DailyTrack

**Offline-first, privacy-first life tracking Android app.**

DailyTrack helps you track your time, food, sleep, exercise, mood, and growth — all stored locally on your device with no cloud accounts, no ads, no tracking.

## Features

- **Time Tracking**: Active timer with foreground service, manual logging, session management
- **Productivity Analysis**: Track productive, wasted, neutral, learning, and exercise time
- **Growth Score**: Transparent daily score from weighted components
- **Routine Loop Detection**: Detect repetitive patterns using cosine similarity
- **Food & Drink Logging**: Full nutrition tracking with macro and micronutrients
- **Hydration Tracking**: Water intake with configurable goals
- **Sleep & Recovery**: Sleep debt calculation and recovery warnings
- **Exercise & Movement**: Exercise logging and inactivity detection
- **Body Systems Dashboard**: Educational health indicator cards
- **Mood & Journal**: Quick check-ins and daily reflections
- **Life Events**: Life timeline and milestones
- **Insights Center**: Personalized warnings and suggestions
- **Analytics**: Charts and trend analysis
- **Export/Import**: JSON and CSV backup/restore
- **Settings**: Profile, targets, privacy controls

## Privacy

- All data stays on your device
- No cloud accounts
- No telemetry or analytics
- No ads
- No proprietary dependencies
- Sensitive health tracking is opt-in
- Medical disclaimer displayed throughout

## Medical Disclaimer

DailyTrack provides **educational insights and lifestyle indicators only**. It is **not medical advice**. If you have persistent symptoms, please consult a healthcare professional.

## Building

### Prerequisites

- JDK 17+
- Android SDK (compileSdk 35)
- Gradle 8.11.1 (included via wrapper)

### Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lintDebug
```

## F-Droid Submission

**Important**: Before publishing to F-Droid, replace the placeholder `applicationId` in `app/build.gradle.kts` with a unique reverse-domain applicationId that you control.

The current applicationId `io.github.dailytrack` is a placeholder.

## License

Apache License 2.0

## Third-Party Notices

See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) for dependency licenses.

## ☕ Support the Developer

If Soul Track helps you, consider buying me a coffee!

<a href="https://buymeacoffee.com/kusal630" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" alt="Buy Me A Coffee" style="height: 50px !important; width: 180px !important;"></a>
