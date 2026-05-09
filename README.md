# PumpManager Pro

A modern fuel station management app built with **Kotlin**, **Jetpack Compose**, and **Feature-Sliced Design** — optimized for offline-first point-of-sale operations.

## Features

- **Sales**: Real-time total calculation, fuel type selection, multiple payment modes, custom numpad
- **Shift Management**: Start/end shift with meter reading validation and active shift tracking
- **Inventory**: Fuel tank stock monitoring with capacity tracking
- **Analytics**: Revenue trends and fuel distribution charts via Vico
- **Authentication**: PIN-based and biometric login
- **Offline-First**: Room database with reliable local persistence
- **CI/CD**: Automated builds, linting, Detekt analysis, and tests

## Tech Stack

| Layer | Choice |
|-------|--------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVI + Feature-Sliced Design |
| DI | Dagger Hilt |
| Database | Room |
| Navigation | Jetpack Navigation Compose |
| Charts | Vico |
| Async | Kotlin Coroutines + Flow |
| CI | GitHub Actions |

## Project Structure

```
app/src/main/java/com/yourcompany/pumpmanager/
├── core/               # Theme, database, navigation, shared UI
│   ├── theme/
│   ├── database/
│   ├── navigation/
│   └── ui/
└── feature/            # Domain features
    ├── auth/
    ├── dashboard/
    ├── sales/
    ├── shift/
    ├── inventory/
    └── reports/
```

## Requirements

- Android Studio Ladybug+
- JDK 17
- Android SDK 35
- Gradle 8.x

## Quick Start

```bash
git clone https://github.com/pallab-js/fuelstation.git
cd fuelstation
./gradlew assembleDebug
```

## Testing

```bash
./gradlew test                    # Unit tests
./gradlew koverHtmlReport         # Coverage report
```

## License

MIT
