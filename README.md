# NYC Car Crashes

An Android application that visualizes motor vehicle collision data from the last 3 months in the NYC area using data from [NYC OpenData](https://data.cityofnewyork.us/Public-Safety/Motor-Vehicle-Collisions-Crashes/h9gi-nx95). This repository demonstrates modern Android development using the latest best practices, tools, and guidelines.

## Features

- Interactive heatmap visualization of crash locations using Google Maps
- Hourly crash distribution chart
- Offline-first architecture with local caching
- Real-time data synchronization with NYC OpenData API
- Light and dark mode support

## Screenshots

| Light Mode | Dark Mode |
| :---: | :---: |
| ![Light Mode Screenshot](https://i.imgur.com/6Acntr3.png) | ![Dark Mode Screenshot](https://i.imgur.com/OF2Ru3x.png) |

## Requirements

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or higher
- **Minimum SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 35 (Android 15)

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/nkrebs13/NYC-Car-Crash.git
cd NYC-Car-Crash
```

### 2. Configure API Keys

Create a file `app/app.properties` with the following content:

```properties
api_key=YOUR_NYC_OPENDATA_API_KEY
map_key=YOUR_GOOGLE_MAPS_API_KEY
```

**API Key Sources:**
- **NYC OpenData API Key**: Register at [data.cityofnewyork.us](https://data.cityofnewyork.us/profile/edit/developer_settings)
- **Google Maps API Key**: Follow the [Google Maps Android SDK documentation](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

### 3. Configure Release Signing (Optional)

For release builds, create `keystore.properties` in the project root:

```properties
storeFile=path/to/your/keystore.jks
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

### 4. Build and Run

```bash
./gradlew assembleDebug
```

Or open the project in Android Studio and click Run.

## Architecture

This app follows **MVVM (Model-View-ViewModel)** architecture with the **Repository pattern** for clean separation of concerns.

### Architecture Diagram
![Architecture Diagram](https://i.imgur.com/xQUUfPf.png)

### Data Flow
1. **UI Layer** (Compose) observes ViewModel state via StateFlow
2. **ViewModel** requests data from Repository
3. **Repository** coordinates between Network and Local data sources
4. **Network DataSource** fetches fresh data from NYC OpenData API
5. **Local DataSource** caches data in Room database for offline access

## Tech Stack

| Category | Technology | Version |
|----------|------------|---------|
| **Language** | Kotlin | 2.0.21 |
| **UI Framework** | Jetpack Compose | BOM 2024.10.01 |
| **Architecture** | MVVM + Repository | - |
| **DI** | Koin | 4.0.0 |
| **Database** | Room | 2.6.1 |
| **Networking** | Ktor | 3.0.1 |
| **Async** | Coroutines + Flow | 1.9.0 |
| **Maps** | Google Maps Compose | 6.1.2 |
| **Charts** | MPAndroidChart | 3.1.0 |
| **Serialization** | Kotlinx Serialization | 1.7.3 |
| **Build System** | Gradle | 8.7 |
| **AGP** | Android Gradle Plugin | 8.5.2 |

## Project Structure

```
app/src/main/java/com/nathankrebs/nyccrash/
├── db/                          # Room database layer
│   ├── entity/                  # Database entities
│   ├── CarCrashDatabase.kt      # Room database definition
│   ├── CarCrashDao.kt           # Data Access Object
│   └── CarCrashLocalDataSource*.kt
├── network/                     # Networking layer
│   ├── CarCrashNetworkDataSource*.kt
│   ├── CarCrashApiItem.kt       # API response model
│   └── NetworkingSingleton.kt   # Ktor HttpClient config
├── repository/                  # Repository pattern
│   ├── CarCrashRepository.kt    # Interface
│   └── CarCrashRepositoryImpl.kt
├── ui/                          # UI Layer
│   ├── compose/                 # Composable functions
│   │   ├── MainScreen.kt
│   │   ├── AppMap.kt            # Google Maps integration
│   │   ├── HourlyGraph.kt       # Chart component
│   │   └── ...
│   ├── theme/                   # Material Design theme
│   ├── MainActivity.kt          # Entry point
│   └── CarCrashViewModel.kt     # ViewModel
├── model/                       # Domain models
├── AppApplication.kt            # Koin initialization
└── appModuleDi.kt               # Dependency injection module
```

## Building

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test                    # Unit tests
./gradlew connectedAndroidTest    # Instrumented tests
```

### Check Dependencies
```bash
./gradlew dependencies
```

### Build Configuration

#### Configuration Cache

This project has **Gradle Configuration Cache** enabled in `gradle.properties` for faster builds. This is an incubating Gradle feature that caches the result of the configuration phase.

**Compatibility Status:**
- ✅ **KSP (Kotlin Symbol Processing)**: Fully compatible
- ✅ **Room Database**: Compatible via KSP annotation processing
- ✅ **Kotlin Gradle Plugin**: Fully supported
- ✅ **Android Gradle Plugin 8.5.2**: Fully supported
- ✅ **Compose Compiler Plugin**: Fully supported

**If you encounter configuration cache issues:**
1. Disable it temporarily by setting `org.gradle.configuration-cache=false` in `gradle.properties`
2. Run with `--no-configuration-cache` flag: `./gradlew build --no-configuration-cache`
3. Check the [Gradle Configuration Cache documentation](https://docs.gradle.org/current/userguide/configuration_cache.html) for troubleshooting

**Known Limitations:**
- Custom build scripts that use deprecated Gradle APIs may not be compatible
- Some older third-party Gradle plugins may not support configuration cache
- Build scripts with mutable shared state during configuration phase may cause issues

## Known Issues

- **TileOverlay Performance**: There is a known issue using `TileOverlay` with Jetpack Compose that can result in performance issues and inconsistent loading of map areas. Workarounds:
  - Zoom in/out of the map to refresh tile loading
  - Use release builds for better performance

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is available for educational and demonstration purposes.

## Acknowledgments

- [NYC OpenData](https://opendata.cityofnewyork.us/) for providing the collision data
- [Google Maps Platform](https://developers.google.com/maps) for mapping services
