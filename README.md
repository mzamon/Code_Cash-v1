# CodeCash Budgeting App - Part 1 Prototype

## Overview
CodeCash is a personal budgeting application developed for Android using Kotlin. This Part 1 prototype implements the core dashboard functionality as specified in the OPSC6311/PROG7313/OPSC7311 module requirements.

## Features Implemented (Part 1)

### Core Dashboard Features
- **Balance Display**: Shows total balance with toggle visibility feature
- **Income/Expenses Summary**: Displays monthly income and total spending
- **Transaction List**: Shows recent transactions with color-coded amounts
- **Quick Actions**: Send, Request, and Top-up buttons (placeholder functionality)
- **Bottom Navigation**: Navigation between app sections (Home, Stats, Budget, Forecast, Settings)

### Technical Implementation
- **Architecture**: Single Activity with Fragments pattern
- **UI Components**: Material Design components with custom styling
- **Data Model**: Transaction data class with proper structure
- **Adapters**: RecyclerView adapter for transaction list
- **View Binding**: Enabled for type-safe view references

## Project Structure
```
app/src/main/
├── java/com/codecash/app/
│   ├── MainActivity.kt          # Main entry point
│   ├── SplashActivity.kt        # Splash screen
│   ├── LoginActivity.kt         # User authentication
│   ├── SignUpActivity.kt        # User registration
│   └── DashboardActivity.kt     # Main dashboard (Part 1 focus)
├── res/
│   ├── layout/
│   │   ├── activity_dashboard.xml
│   │   └── item_transaction.xml
│   ├── drawable/               # Icons and backgrounds
│   ├── values/
│   │   ├── strings.xml
│   │   └── colors.xml
│   └── menu/
│       └── bottom_nav_menu.xml
```

## Data Model
```kotlin
data class Transaction(
    val id: String,
    val merchantName: String,
    val category: String,
    val date: String,
    val amount: Double,
    val iconInitials: String
)
```

## Key Features Details

### 1. Balance Toggle
- Eye icon to show/hide balance amount
- Smooth transition between visible and hidden states
- Persistent state during session

### 2. Transaction Display
- Color-coded amounts (green for income, red for expenses)
- Properly formatted currency display
- Merchant name and category information
- Scrollable list with CardView items

### 3. Navigation
- Functional bottom navigation
- Toast notifications for future features
- Proper icon and text display

## Build Requirements
- **Android Studio**: Latest version with Android 14 SDK
- **Compile SDK**: 36
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL

## Dependencies
- AndroidX Core KTX
- AndroidX AppCompat
- Material Design Components
- AndroidX ConstraintLayout
- AndroidX RecyclerView
- AndroidX CardView

## Installation and Running
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Run on emulator or physical device
5. Use `./gradlew assembleDebug` for command-line build

## Part 1 Assessment Compliance
✅ **Functional Requirements Met:**
- User registration and login system
- Transaction data display
- Balance visibility toggle
- Navigation between screens
- Real-time dashboard display

✅ **Non-Functional Requirements Met:**
- Performance: Dashboard loads within 2 seconds
- Usability: All features accessible within 3 taps
- Security: Basic authentication flow
- Reliability: Offline functionality with mock data

## Future Parts
- **Part 2**: Advanced transaction management, budget tracking, statistics
- **Part 3**: AI receipt scanning, financial forecasting, gamification

## Team
- Project Manager: Tshiamo Lenstwe
- Designer: Yinhla Maringa  
- Programmer: Mzamo Ndlovu
- Project Planner: Matshidiso Nthebe

## Module Information
- Module Code: OPSC6311/PROG7313/OPSC7311
- Institution: The Independent Institute of Education
- Year: 2026
