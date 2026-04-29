# ⛽ PumpManager Pro — Antigravity Vibecoding Blueprint

> **How to use this file:** Feed it entirely into Antigravity at the start of a session. Reference specific sections by name when prompting (e.g., *"Follow the Golden Rules and implement the Session 3 tasks"*). Every section is written for maximum AI context clarity.

---

## 0. Project Identity

| Field | Value |
|---|---|
| **App Name** | PumpManager Pro |
| **Package** | `com.yourcompany.pumpmanager` |
| **Platform** | Android (minSdk 26, targetSdk 35) |
| **Language** | Kotlin 2.0+ |
| **Purpose** | Fuel station management — sales, shifts, inventory, analytics |

---

## 1. Technology Stack

Select each tool for LLM reliability: well-documented, declarative, and minimal boilerplate.

| Layer | Library | Why |
|---|---|---|
| **UI** | Jetpack Compose + Material 3 | Declarative — LLMs write it cleanly |
| **Architecture** | MVI/MVVM + sealed interfaces | Single-state screens eliminate ambiguity |
| **DI** | Dagger Hilt | Standard, saturated in training data |
| **Database** | Room | Offline-first; SQL generation is highly reliable |
| **Navigation** | Jetpack Navigation Compose (type-safe) | Compile-time route safety |
| **Charts** | Vico (Compose-native) | No XML interop needed |
| **Async** | Kotlin Coroutines + Flow | Native Kotlin, no RxJava complexity |
| **Build** | Version Catalogs (`libs.versions.toml`) | Prevents Gradle sync issues |

> **Antigravity rule:** Always update `libs.versions.toml` when adding a dependency. Never hardcode version strings in `build.gradle.kts`.

---

## 2. Architecture: Feature-Sliced Design (FSD)

### Why FSD over Clean Architecture?

Traditional Clean Architecture scatters a single feature across `data/`, `domain/`, and `presentation/` folders — this **shatters AI context**. FSD collapses everything for one feature into one folder, so you can say: *"Focus on `feature/sales` and add a discount field"* and the AI has full context without hallucinating cross-folder dependencies.

### Directory Structure

```
com.yourcompany.pumpmanager/
│
├── core/                        # 🧠 THE BRAIN — set up once, rarely touch
│   ├── theme/
│   │   ├── Color.kt             # Genesis palette as Compose Color tokens
│   │   ├── Type.kt              # Typography scale
│   │   ├── Shape.kt             # Border radius tokens
│   │   └── Theme.kt             # MaterialTheme wrapper (light + dark)
│   ├── database/
│   │   ├── AppDatabase.kt       # Room @Database, lists all entities
│   │   └── TypeConverters.kt    # Date/Enum converters
│   ├── navigation/
│   │   ├── AppNavHost.kt        # Single NavHost, all routes wired here
│   │   └── Routes.kt            # Sealed class of all type-safe destinations
│   └── ui/                      # Shared Compose components
│       ├── PrimaryButton.kt
│       ├── StatsCard.kt
│       ├── FuelChip.kt
│       └── LoadingOverlay.kt
│
└── feature/                     # 🚀 THE FEATURES — vibecode here
    ├── auth/
    │   ├── AuthScreen.kt
    │   ├── AuthViewModel.kt
    │   ├── AuthState.kt         # Single source of truth for UI state
    │   └── AuthEvent.kt         # Sealed interface — all user actions
    │
    ├── dashboard/
    │   ├── DashboardScreen.kt
    │   ├── DashboardViewModel.kt
    │   ├── DashboardState.kt
    │   └── DashboardEvent.kt
    │
    ├── sales/                   # Most complex feature
    │   ├── SalesScreen.kt
    │   ├── SalesViewModel.kt
    │   ├── SalesState.kt
    │   ├── SalesEvent.kt
    │   ├── SaleEntity.kt        # DB entity lives inside the feature
    │   └── SaleDao.kt           # DAO lives inside the feature
    │
    ├── shift/
    │   ├── ShiftScreen.kt
    │   ├── ShiftViewModel.kt
    │   ├── ShiftState.kt
    │   ├── ShiftEvent.kt
    │   ├── ShiftEntity.kt
    │   └── ShiftDao.kt
    │
    ├── inventory/
    │   ├── InventoryScreen.kt
    │   ├── InventoryViewModel.kt
    │   ├── InventoryState.kt
    │   ├── InventoryEvent.kt
    │   ├── TankEntity.kt
    │   └── TankDao.kt
    │
    └── reports/
        ├── ReportsScreen.kt
        ├── ReportsViewModel.kt
        ├── ReportsState.kt
        └── ReportsEvent.kt
```

### MVI Contract (enforce this pattern for every feature)

```kotlin
// State — single immutable data class, entire screen derived from this
data class SalesUiState(
    val selectedFuel: FuelType = FuelType.PETROL,
    val volume: String = "",
    val calculatedTotal: Double = 0.0,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

// Events — sealed interface, one entry per user action
sealed interface SalesEvent {
    data class FuelSelected(val fuel: FuelType) : SalesEvent
    data class VolumeChanged(val input: String) : SalesEvent
    data class PaymentModeChanged(val mode: PaymentMode) : SalesEvent
    data object SaveSale : SalesEvent
    data object DismissError : SalesEvent
}
```

> **Antigravity rule:** Every ViewModel exposes exactly one `StateFlow<FeatureUiState>` and one `fun onEvent(event: FeatureEvent)`. No additional public functions.

---

## 3. Database Schema (Define All Entities in Session 1)

Define the full schema before building any UI. A solid data layer makes UI generation trivial.

```kotlin
// UserEntity (feature/auth)
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val role: String,           // "manager" | "attendant"
    val pinHash: String
)

// FuelTypeEntity (feature/inventory)
@Entity(tableName = "fuel_types")
data class FuelTypeEntity(
    @PrimaryKey val id: String,
    val name: String,           // "Petrol", "Diesel", "CNG"
    val pricePerLiter: Double,
    val isActive: Boolean = true
)

// TankEntity (feature/inventory)
@Entity(tableName = "tanks")
data class TankEntity(
    @PrimaryKey val id: String,
    val fuelTypeId: String,
    val capacityLiters: Double,
    val currentStockLiters: Double
)

// ShiftEntity (feature/shift)
@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val id: String,
    val attendantId: String,
    val startTime: Long,        // Unix epoch ms
    val endTime: Long?,
    val openingMeterReading: Double,
    val closingMeterReading: Double?,
    val status: String          // "active" | "closed"
)

// SaleEntity (feature/sales)
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String,
    val shiftId: String,
    val fuelType: String,
    val volumeLiters: Double,
    val pricePerLiter: Double,
    val totalAmount: Double,
    val paymentMode: String,    // "CASH" | "UPI" | "CARD"
    val timestamp: Long
)
```

---

## 4. Design System — Genesis Palette (Compose Tokens)

Translate the Genesis web design system into Compose. Apply these tokens rigorously — never use raw hex strings in screen files.

### `core/theme/Color.kt`

```kotlin
// Genesis Palette — translated for Compose
val IndigoPrimary     = Color(0xFF6366F1)   // CTAs, active states, focus rings
val IndigoDark        = Color(0xFF4F46E5)   // Hover / pressed state
val GreenBrand        = Color(0xFF20970B)   // Reserved for brand highlight only
val NeutralMuted      = Color(0xFF9C9C9C)   // Timestamps, placeholders, disabled
val BackgroundWarm    = Color(0xFFFAFAFA)   // Page / screen background
val SurfaceWhite      = Color(0xFFFFFFFF)   // Cards, panels, nav
val TextPrimary       = Color(0xFF0A0A0A)   // Headings, body — near-black
val TextSecondary     = Color(0xFF6B6B6B)   // Descriptions, metadata
val BorderSubtle      = Color(0xFFE8E8EC)   // Card borders, dividers, inputs
val SuccessGreen      = Color(0xFF10B981)   // Confirmed sales, published status
val WarningAmber      = Color(0xFFF59E0B)   // Pending states, low stock
val ErrorRed          = Color(0xFFEF4444)   // Errors, destructive actions

// Dark mode surfaces (derive from Genesis dark rules)
val SurfaceDark       = Color(0xFF111113)
val BackgroundDark    = Color(0xFF0A0A0B)
val BorderDark        = Color(0xFF2A2A2E)
```

### `core/theme/Type.kt`

```kotlin
// Genesis Typography Scale — adapted for Compose sp units
// Fonts: DM Sans (body), JetBrains Mono (code/numbers)
// Note: General Sans (display) → substitute with DM Sans Bold at tight tracking for Android

val AppTypography = Typography(
    displayLarge  = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-1.5).sp),
    displayMedium = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-1.2).sp),
    headlineLarge = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.8).sp),
    headlineMedium= TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
    titleLarge    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium),
    bodyLarge     = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    labelSmall    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium,  letterSpacing = 0.5.sp)
)
```

### `core/theme/Shape.kt`

```kotlin
// Genesis Border Radius Tokens
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Tags, chips, badges, inline values
    small      = RoundedCornerShape(6.dp),   // Buttons, inputs, selects
    medium     = RoundedCornerShape(8.dp),   // Dropdowns, metadata cards, panels
    large      = RoundedCornerShape(12.dp),  // Kit/stat cards, search bar
    extraLarge = RoundedCornerShape(9999.dp) // Avatars, status dots, pill badges
)
```

### Spacing System (4px Base Grid)

```
4dp, 8dp, 12dp, 16dp, 20dp, 24dp, 32dp, 40dp, 48dp, 64dp, 80dp, 96dp
```

Always use multiples of 4dp. Never use arbitrary values like 7dp, 11dp, or 15dp.

---

## 5. UI Component Contracts (for Antigravity)

Prompt Antigravity to build each of these reusable components into `core/ui/` before building any feature screen.

### `PrimaryButton`
- Fill: `IndigoPrimary`, text: white, weight: Medium
- Height: 44dp (large), 38dp (medium), 32dp (small)
- Corner: `AppShapes.small` (6dp)
- Pressed: `IndigoDark` fill + 1dp lift offset
- Disabled: 38% alpha on fill
- Glow shadow on hover: `0 4dp 12dp rgba(99,102,241,0.35)`
- **One primary button per screen section max**

### `StatsCard`
- Surface: `SurfaceWhite`, border: 1px `BorderSubtle`, corner: `AppShapes.large` (12dp)
- Default elevation: 0 (flat)
- Hover/pressed: 2dp lift + `0 8dp 30dp rgba(0,0,0,0.08)` shadow
- Transition: 200ms

### `FuelChip` / `FilterChip`
- Shape: `AppShapes.extraLarge` (pill)
- Default: `Color(0xFFF3F3F3)` bg, `TextSecondary` text
- Selected: `IndigoPrimary` bg, white text
- Padding: 4dp vertical × 12dp horizontal
- Font: `bodyMedium` (13sp)

### `StatusBadge`
- Shape: pill (`extraLarge`)
- Active/Confirmed: `SuccessGreen` bg
- Pending: `WarningAmber` bg
- Error/Rejected: `ErrorRed` bg
- All: white text, `labelSmall` font

### `AppTextField`
- Border: 1px `BorderSubtle`, corner: `AppShapes.small` (6dp)
- Padding: 10dp vertical × 14dp horizontal
- Font size: 14sp (bodyMedium)
- Focused: border turns `IndigoPrimary` + 3dp rgba ring `rgba(99,102,241,0.12)`
- Error: border turns `ErrorRed`
- Placeholder: `NeutralMuted`

### `BottomNavBar`
- Background: `SurfaceWhite` with `backdrop-blur` equivalent (`Modifier.blur`)
- Height: 56dp
- Border-top: 1px `BorderSubtle`
- Active icon/label: `IndigoPrimary`
- Inactive: `NeutralMuted`

---

## 6. Vibecoding Workflow — Step by Step

Feed prompts to Antigravity in this sequence. Do **not** skip steps or batch sessions.

---

### 🔵 Session 1 — Scaffolding & Schema

**Goal:** A compiling app with navigation, Hilt, Room, and all entities defined.

**Prompt 1 — Project Bootstrap:**
> *"Initialize the Android project. Set up Hilt in `Application` class and all Gradle modules. Create the `core/theme/` with `Color.kt`, `Type.kt`, `Shape.kt`, and `Theme.kt` using the Genesis palette tokens provided in this blueprint. Create `AppNavHost.kt` with a splash screen route."*

**Prompt 2 — Define all DB Entities:**
> *"Create `AppDatabase.kt` in `core/database/`. Register all entities: `UserEntity`, `FuelTypeEntity`, `TankEntity`, `ShiftEntity`, `SaleEntity`. Define them as specified in the blueprint schema. Add `TypeConverters` for Long timestamps. Use Hilt to provide the database as a singleton."*

**Prompt 3 — Seed DAOs:**
> *"Create a DAO for each entity, placed inside its respective feature folder: `SaleDao`, `ShiftDao`, `TankDao`. Each DAO must include: `insert`, `getAll`, `getById`, and a query to fetch today's records filtered by timestamp. Use `Flow<List<Entity>>` return types for reactive queries."*

---

### 🔵 Session 2 — Auth & Dashboard

**Goal:** Biometric/PIN login screen and the main bottom-nav scaffold.

**Prompt 4 — Auth Feature:**
> *"Build `feature/auth/`. Create `AuthState.kt` (fields: `pinInput: String`, `isLoading: Boolean`, `errorMessage: String?`), `AuthEvent.kt` (events: `PinDigitEntered`, `PinDeleted`, `BiometricTriggered`, `DismissError`). Build `AuthViewModel.kt` exposing a single `StateFlow<AuthState>` and `onEvent(AuthEvent)`. Build `AuthScreen.kt` in Jetpack Compose using the Genesis component tokens. Include a `@Preview` with mock state data."*

**Prompt 5 — Dashboard Scaffold:**
> *"Build the `DashboardScreen.kt` with a `Scaffold` + `NavigationBar`. Bottom nav tabs: Dashboard, Sales, Shift, Inventory, Reports. Use `IndigoPrimary` for active tab. Wire to `AppNavHost`. Include stub screens for each tab."*

---

### 🔵 Session 3 — Core Loop: Sales

**Goal:** The most critical screen — real-time total calculation, fuel selection, numpad, payment mode.

**Prompt 6 — Sales State & ViewModel:**
> *"Build `feature/sales/SalesViewModel.kt`. `SalesUiState` must contain: `selectedFuel: FuelType`, `volume: String`, `pricePerLiter: Double`, `calculatedTotal: Double`, `paymentMode: PaymentMode`, `isLoading: Boolean`, `errorMessage: String?`. When `VolumeChanged` is fired, recalculate `calculatedTotal = volume.toDoubleOrNull() * pricePerLiter`. On `SaveSale`, insert via `SaleDao` inside a `viewModelScope` coroutine, catch DB errors, and expose them via `errorMessage`. On success, reset volume to empty string."*

**Prompt 7 — Sales UI:**
> *"Build `SalesScreen.kt` using Jetpack Compose. Layout top-to-bottom: (1) `FuelChip` row for fuel type selection — Petrol, Diesel, CNG. (2) A large display showing `calculatedTotal` formatted as currency — use `displayLarge` typography in `TextPrimary`. (3) A custom numpad using a `LazyVerticalGrid` for digit input. (4) A row of payment mode `FilterChip`s — Cash, UPI, Card. (5) A `PrimaryButton` 'Save Sale'. All components use Genesis design tokens. Refactor the numpad and payment mode selector into separate private composable functions in the same file. Add `@Preview` with mock state."*

---

### 🔵 Session 4 — Shift Management

**Goal:** Start shift, meter readings, end shift with summary.

**Prompt 8:**
> *"Build `feature/shift/`. `ShiftUiState` must contain: `activeShift: ShiftEntity?`, `openingMeter: String`, `closingMeter: String`, `isLoading: Boolean`, `errorMessage: String?`. Events: `StartShift`, `EndShift`, `OpeningMeterChanged`, `ClosingMeterChanged`. The ViewModel checks on init if there is an active shift in Room; if so, populate state. `ShiftScreen.kt` shows either a 'Start Shift' form (opening meter input + primary button) or an 'Active Shift' card (shift duration, live meter, 'End Shift' button). Use `StatsCard` component for the active shift summary."*

---

### 🔵 Session 5 — Analytics & Reports

**Goal:** Aggregate Room data into Vico charts with key KPIs.

**Prompt 9:**
> *"Build `feature/reports/ReportsViewModel.kt`. Aggregate from Room: (1) total sales revenue today, (2) sales count by fuel type today, (3) revenue by day for the last 7 days. Expose as `ReportsUiState`. In `ReportsScreen.kt`, render: a row of three `StatsCard`s for key KPIs, a Vico `ColumnChart` for fuel-type breakdown, and a Vico `LineChart` for 7-day revenue trend. Charts use `IndigoPrimary` as the primary series color and `SuccessGreen` as the secondary. Add a loading shimmer state."*

---

### 🔵 Session 6 — Polish & Hardening

**Prompt 10 — Dark Mode:**
> *"Create a dark `ColorScheme` in `core/theme/Theme.kt` using `SurfaceDark`, `BackgroundDark`, and `BorderDark`. The `MaterialTheme` should switch based on `isSystemInDarkTheme()`. All feature screens must render correctly in both modes — test with `@Preview(uiMode = UI_MODE_NIGHT_YES)`."*

**Prompt 11 — Error Handling Sweep:**
> *"Review every ViewModel in the project. Every database or network call must be wrapped in `try/catch`. Errors must set `errorMessage` in the corresponding state. Every screen must observe `errorMessage` and show a `Snackbar` using `LaunchedEffect`. Dismiss on `DismissError` event."*

---

## 7. Golden Rules for Antigravity Sessions

Paste these at the top of every new Antigravity session to maintain consistency.

```
GOLDEN RULES — enforce for every file you generate:

1. IMMUTABILITY: All state is in a single immutable data class. Use StateFlow, never MutableState in ViewModels.
2. SINGLE EVENT HANDLER: Each ViewModel has exactly one `onEvent(Event)` function. No other public functions.
3. COMPONENTIZE: If a Compose file exceeds 200 lines, extract composables into private functions in the same file.
4. HANDLE ERRORS EXPLICITLY: Every DB/IO call has try/catch. Errors surface via errorMessage in state. No silent failures.
5. GENESIS DESIGN TOKENS: Never use raw hex strings. Always reference Color.kt, Type.kt, Shape.kt tokens.
6. 4DP SPACING GRID: All padding/margin/gap values must be multiples of 4dp.
7. VERSION CATALOGS: All dependency versions live in libs.versions.toml only.
8. PREVIEWS: Every screen composable must have a @Preview with hardcoded mock state.
9. NO LOGIC IN UI: Screens only call onEvent(). Zero business logic in Composables.
10. ONE PRIMARY BUTTON: Never place more than one filled indigo PrimaryButton in the same view section.
```

---

## 8. Entity Relationship Summary

```
UserEntity
    └─── has many ──▶ ShiftEntity (attendantId)
                          └─── has many ──▶ SaleEntity (shiftId)

FuelTypeEntity
    └─── has many ──▶ TankEntity (fuelTypeId)
    └─── referenced by ──▶ SaleEntity (fuelType name)
```

---

## 9. Route Map

```kotlin
sealed class Routes {
    object Splash    : Routes()
    object Auth      : Routes()
    object Dashboard : Routes()
    object Sales     : Routes()
    object Shift     : Routes()
    object Inventory : Routes()
    object Reports   : Routes()
}
```

Navigation flow: `Splash → Auth → Dashboard` (with bottom nav to Sales / Shift / Inventory / Reports)

---

## 10. Quick-Reference Prompt Templates

Copy-paste these to start any session cleanly:

| Task | Prompt Template |
|---|---|
| Add a field | *"In `feature/[name]/`, add `[field]: [Type]` to `[Entity].kt` and `[State].kt`. Update the DAO query and ViewModel logic. Regenerate the screen section that uses it."* |
| Fix a bug | *"In `feature/[name]/[ViewModel].kt`, the `[event]` handler is not [behavior]. Here is the current code: [paste]. Fix only this function. Do not change anything else."* |
| Extract component | *"Extract the `[composable block]` in `[Screen].kt` into a private composable named `[Name]`. It must accept the same parameters it currently reads from state."* |
| Add a chart | *"In `feature/reports/`, add a Vico `LineChart` to `ReportsScreen.kt`. Data source: `ReportsUiState.[fieldName]`. Use `IndigoPrimary` for the line color. Add the field to `ReportsUiState` and populate it in `ReportsViewModel` from the Room query `[describe query]`."* |
| Add error handling | *"Update `[ViewModel].kt`. Wrap the `[function]` in try/catch. On error, set `state.errorMessage`. In `[Screen].kt`, observe `errorMessage` with `LaunchedEffect` and show a `Snackbar`. Add a `DismissError` event."* |
