# PumpManager Pro

A modern fuel station management app built with **Kotlin**, **Jetpack Compose**, and **Feature-Sliced Design** — optimized for offline-first point-of-sale operations.

## Features

- **Sales**: Real-time total calculation, fuel type selection, multiple payment modes, custom numpad
- **Shift Management**: Start/end shift with meter reading validation, confirmation dialog, and active shift tracking
- **Inventory**: Fuel tank stock monitoring with refill, overfill protection, low-stock warnings, and fuel price management
- **Sales History**: Paginated sale list with void functionality (restores stock automatically)
- **Analytics**: Revenue trends, fuel breakdown, and CSV export with period selection (Today/Week/Month)
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
app/src/main/kotlin/com/pallab/pumpmanager/
├── core/               # Theme, database, navigation, shared UI
│   ├── theme/
│   ├── database/
│   ├── navigation/
│   ├── session/
│   ├── ui/
│   └── util/
└── feature/            # Domain features
    ├── auth/
    ├── dashboard/
    ├── fuelprices/
    ├── inventory/
    ├── reports/
    ├── sales/
    ├── saleshistory/
    ├── shift/
    └── splash/
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
./gradlew testDebugUnitTest               # Unit tests
./gradlew compileDebugAndroidTestKotlin   # Compile instrumented tests
./gradlew koverHtmlReport                 # Coverage report
```

## License

MIT
