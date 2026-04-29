# PumpManager Pro ⛽

**PumpManager Pro** is a modern, high-performance fuel station management application built with **Kotlin 2.0** and **Jetpack Compose**. It follows the **Feature-Sliced Design (FSD)** architecture and uses the **Genesis** design system for a premium, editorial-style interface.

## 🚀 Features

- **Authentication**: Secure PIN-based and biometric login.
- **Sales Management**: Real-time total calculation, fuel type selection, and multiple payment modes.
- **Shift Tracking**: Start/End shift management with meter reading validation.
- **Analytics & Reports**: Visual data insights using **Vico Charts**, tracking revenue trends and fuel distribution.
- **CI/CD**: GitHub Actions workflow for automated builds, linting, Detekt static analysis, and testing.
- **Offline First**: Powered by **Room** for reliable data persistence.
- **Premium UI**: Dark mode support, custom geometric interactions, and a strict 4dp spacing grid.

## 🛡️ Security & Compliance

- **Data Protection**: `android:allowBackup` is disabled to prevent unauthorized data extraction.
- **Code Hardening**: R8/ProGuard minification and obfuscation enabled for release builds.
- **Static Analysis**: Integrated **Detekt** for automated security smell detection and code quality enforcement.
- **Authentication**: Simulated secure PIN verification with biometric fallback support.

## 🧪 Testing

- **Unit Testing**: JUnit 4 with MockK for ViewModel and business logic verification.
- **Integration Testing**: **Robolectric** for database and persistence layer verification on the JVM.
- **CI Pipeline**: Automated checks running on every PR to ensure zero regressions.

## 🛠 Tech Stack

- **UI**: Jetpack Compose + Material 3
- **Architecture**: MVI (Model-View-Intent) + Feature-Sliced Design
- **Dependency Injection**: Dagger Hilt
- **Database**: Room
- **Navigation**: Type-safe Jetpack Navigation Compose
- **Charts**: Vico
- **Async**: Kotlin Coroutines + Flow

## 📂 Project Structure (FSD)

```
com.yourcompany.pumpmanager/
├── core/                        # Shared infrastructure (Theme, DB, Navigation)
│   ├── theme/                   # Genesis design system tokens
│   ├── database/                # Room configuration
│   └── ui/                      # Shared Compose components (StatsCard, etc.)
└── feature/                     # Domain-driven features
    ├── auth/                    # PIN/Biometric login logic
    ├── dashboard/               # Main app scaffold and navigation
    ├── sales/                   # Core sales recording loop
    ├── shift/                   # Attendant shift management
    └── reports/                 # Analytics and Vico visualizations
```

## 🎨 Design Principles (Genesis)

- **Colors**: Primary Indigo (`#6366F1`) for interaction; warm neutrals for surfaces.
- **Typography**: Editorial scale using **DM Sans** with tight tracking for display headings.
- **Elevation**: Minimal shadows; elevation communicated through lift and subtle tinted glows on interaction.
- **Grid**: Strict adherence to a 4dp base unit.

## 🛠 Installation

1. Clone the repository.
2. Open in Android Studio (Ladybug+ recommended).
3. Sync Gradle (Version Catalogs enabled).
4. Run on an API 26+ device.

---
Built with ❤️ by Antigravity.
