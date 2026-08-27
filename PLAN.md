# DailyTrack - Development Plan

## Environment

- Java 21 (OpenJDK)
- Android SDK: platforms android-36, build-tools 36.0.0
- Gradle 8.11.1
- Kotlin 2.0.21
- AGP 8.8.0

## Dependency Versions

| Dependency | Version | Reason |
|---|---|---|
| Kotlin | 2.0.21 | Stable, Compose compiler compatible |
| AGP | 8.8.0 | Stable, compatible with JDK 21 |
| Compose BOM | 2024.12.01 | Latest stable Compose |
| Room | 2.6.1 | Stable Room with KSP support |
| WorkManager | 2.10.0 | Latest stable |
| Navigation Compose | 2.8.5 | Latest stable |
| DataStore | 1.1.1 | Latest stable |
| KSP | 2.0.21-1.0.28 | Kotlin 2.0.21 compatible |
| Coroutines | 1.9.0 | Latest stable |
| Lifecycle | 2.8.7 | Latest stable |
| Material3 | via Compose BOM | Included in BOM |
| CameraX | 1.4.1 | Latest stable (optional) |
| Health Connect | 1.1.0-alpha06 | Latest available (optional) |
| JUnit | 4.13.2 | Standard |
| Robolectric | 4.14.1 | Latest stable |
| Core library desugaring | 2.1.4 | Latest stable |

## F-Droid Compatibility Notes

- All dependencies are open source (Apache 2.0 or MIT)
- No Google Play Services required
- No Firebase
- CameraX and Health Connect are optional and disabled by default
- Core app works without any optional integrations

## Architecture

Single-module app for F-Droid simplicity. Package structure:
```
io.github.dailytrack
  ├── data/
  │   ├── db/          (Room entities, DAOs, database)
  │   ├── repository/  (Repository implementations)
  │   └── model/       (Domain models)
  ├── domain/
  │   └── usecase/     (Use cases for each module)
  ├── engine/          (Growth, insight, nutrition engines)
  ├── ui/
  │   ├── theme/       (Material3 theme)
  │   ├── navigation/  (Nav graph)
  │   ├── dashboard/   (Today screen)
  │   ├── timer/       (Timer screen)
  │   ├── sessions/    (Session management)
  │   ├── food/        (Food/drink/water)
  │   ├── nutrition/   (Nutrition analytics)
  │   ├── sleep/       (Sleep/recovery)
  │   ├── exercise/    (Exercise/movement)
  │   ├── body/        (Body systems dashboard)
  │   ├── growth/      (Growth & routine)
  │   ├── history/     (Calendar/history)
  │   ├── analytics/   (Charts/analytics)
  │   ├── insights/    (Insight center)
  │   ├── journal/     (Life events/journal)
  │   ├── settings/    (Settings)
  │   └── components/  (Shared composables)
  ├── service/         (Foreground service)
  ├── worker/          (WorkManager workers)
  └── DailyTrackApp.kt
```
