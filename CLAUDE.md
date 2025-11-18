# NYC Car Crashes - Claude Code Context

This document provides context for Claude Code to understand and work with this Android project effectively.

## Project Overview

**NYC Car Crashes** is an Android application that visualizes motor vehicle collision data from NYC OpenData. It displays crash locations on an interactive heatmap and shows hourly crash distribution charts.

### Core Functionality
- Fetches collision data from NYC OpenData API
- Caches data locally using Room database
- Displays crashes as a heatmap on Google Maps
- Shows hourly distribution chart using MPAndroidChart
- Supports light/dark themes

## Architecture

### Pattern: MVVM + Repository

```
UI (Compose) -> ViewModel -> Repository -> DataSources (Network + Local)
```

### Key Architectural Decisions
- **Offline-first**: Data is cached in Room database
- **Single source of truth**: Repository coordinates data from network and local sources
- **Reactive streams**: StateFlow for UI state, Flow for data streams
- **Dependency injection**: Koin for all dependencies

## Project Structure

### Source Code Locations

| Directory | Purpose | Key Files |
|-----------|---------|-----------|
| `app/src/main/java/.../db/` | Database layer | `CarCrashDatabase.kt`, `CarCrashDao.kt`, `CarCrashLocalDataSource*.kt` |
| `app/src/main/java/.../network/` | Networking | `NetworkingSingleton.kt`, `CarCrashNetworkDataSource*.kt` |
| `app/src/main/java/.../repository/` | Data coordination | `CarCrashRepository.kt`, `CarCrashRepositoryImpl.kt` |
| `app/src/main/java/.../ui/` | UI layer | `MainActivity.kt`, `CarCrashViewModel.kt` |
| `app/src/main/java/.../ui/compose/` | Composables | `MainScreen.kt`, `AppMap.kt`, `HourlyGraph.kt` |
| `app/src/main/java/.../ui/theme/` | Theming | `Theme.kt`, `Color.kt`, `Type.kt` |
| `app/src/main/java/.../model/` | Domain models | `CarCrashItem.kt` |

### Build Configuration

| File | Purpose |
|------|---------|
| `buildSrc/src/main/kotlin/AppDependencies.kt` | Centralized dependency versions |
| `build.gradle.kts` (root) | Project plugins and classpath |
| `app/build.gradle.kts` | App module configuration |
| `gradle.properties` | Gradle and Android settings |

### Configuration Files (Not in Git)

| File | Purpose |
|------|---------|
| `app/app.properties` | API keys (api_key, map_key) |
| `keystore.properties` | Release signing credentials |

## Development Workflows

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Clean build
./gradlew clean assembleDebug
```

### Testing

```bash
# Unit tests
./gradlew test

# Unit tests with coverage
./gradlew testDebugUnitTest

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint checks
./gradlew lint
```

### Dependency Management

All dependencies are centralized in `buildSrc/src/main/kotlin/AppDependencies.kt`:
- Update versions in `AppDependencies.kt`
- Run `./gradlew dependencies` to verify

## Code Conventions

### Kotlin Style
- Use Kotlin idioms (scope functions, null safety, extension functions)
- Prefer immutable data (`val`, data classes)
- Use coroutines for async operations

### Compose Patterns
- Composables should be stateless when possible
- Use `remember` for expensive computations
- Pass callbacks down, state up
- Use Material Design components

### Architecture Patterns
- Interfaces for all data sources and repositories
- Implementation classes suffixed with `Impl`
- ViewModels expose StateFlow for UI state
- Use sealed classes for UI states

### Naming Conventions
- Packages: lowercase (e.g., `com.nathankrebs.nyccrash`)
- Classes: PascalCase (e.g., `CarCrashViewModel`)
- Functions: camelCase (e.g., `fetchCrashData`)
- Constants: SCREAMING_SNAKE_CASE

## Common Tasks

### Adding a New Feature

1. **Data layer**: Add entity/DAO if database storage needed
2. **Network layer**: Add API models and data source methods
3. **Repository**: Add repository methods to coordinate data
4. **ViewModel**: Add state and methods for UI
5. **UI**: Create Composables for display

### Adding a New Dependency

1. Add version constant to `AppDependencies.kt`
2. Add library constant with version
3. Add to `app/build.gradle.kts` dependencies
4. Sync Gradle

### Modifying the Database Schema

1. Update entity in `db/entity/`
2. Update DAO queries in `CarCrashDao.kt`
3. Increment database version in `CarCrashDatabase.kt`
4. Add migration if needed

### Updating API Integration

1. Modify `CarCrashApiItem.kt` for response model
2. Update `CarCrashNetworkDataSourceImpl.kt` for API calls
3. Update repository mapping if domain model differs

## Tech Stack Details

### Key Libraries

| Library | Purpose | Notes |
|---------|---------|-------|
| **Ktor 3.x** | HTTP client | Uses OkHttp engine, Kotlinx serialization |
| **Room 2.6.x** | SQLite abstraction | KSP for annotation processing |
| **Koin 4.x** | DI framework | `viewModel` from `org.koin.core.module.dsl` |
| **Compose BOM** | UI toolkit | Version managed by BOM |
| **Maps Compose 6.x** | Google Maps | `TileOverlay` for heatmap |

### Gradle Configuration

- **Gradle**: 8.7
- **AGP**: 8.5.2
- **Kotlin**: 2.0.21
- **JVM Target**: 17
- **Compose Compiler**: Bundled with Kotlin 2.0 (plugin-based)

## Testing Strategy

### Current State
- Basic unit test template exists
- Basic instrumented test template exists
- No comprehensive test coverage yet

### Recommended Testing

1. **Unit Tests**: ViewModel logic, Repository, DataSources
2. **Integration Tests**: Database operations
3. **UI Tests**: Compose screenshot tests

### Test Locations
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests

## Performance Considerations

### Known Issues
- `TileOverlay` with Compose has rendering performance issues
- Workaround: Hide overlay during map movement

### Optimization Opportunities
- Enable R8 minification for release builds
- Add ProGuard rules for libraries
- Implement paging for large datasets

## API Information

### NYC OpenData API
- Base URL: `https://data.cityofnewyork.us/`
- Dataset: Motor Vehicle Collisions - Crashes
- Authentication: App token in header

### Google Maps
- Requires API key with Maps SDK for Android enabled
- Key configured in `AndroidManifest.xml` via `manifestPlaceholders`

## Error Handling

### Current Patterns
- Network errors handled in Repository
- UI shows error state via ViewModel
- Local cache provides offline fallback

### Recommended Improvements
- Add retry logic for transient failures
- Implement exponential backoff
- Add detailed error reporting

## Security Considerations

### Sensitive Data
- API keys stored in `app.properties` (gitignored)
- Keystore credentials in `keystore.properties` (gitignored)

### Best Practices
- Never commit API keys or credentials
- Use Android Keystore for production secrets
- Enable network security config for HTTPS

## Useful Commands

```bash
# Check for dependency updates
./gradlew dependencyUpdates

# Generate dependency report
./gradlew dependencies --configuration releaseRuntimeClasspath

# Run specific test
./gradlew test --tests "*.ExampleUnitTest"

# Build and install on device
./gradlew installDebug

# Clear Gradle cache
./gradlew cleanBuildCache
```

## IDE Setup

### Android Studio Requirements
- Version: Hedgehog (2023.1.1) or newer
- JDK: 17 bundled with Android Studio

### Recommended Plugins
- Kotlin
- Android (bundled)
- Gradle (bundled)

## Contributing Guidelines

1. Follow existing code patterns and naming conventions
2. Add tests for new functionality
3. Update documentation for significant changes
4. Keep commits focused and well-described
5. Run lint and tests before submitting

## Quick Reference

### File Locations for Common Edits

| Task | File(s) |
|------|---------|
| Add new UI screen | `ui/compose/`, `CarCrashViewModel.kt` |
| Modify API calls | `network/CarCrashNetworkDataSourceImpl.kt` |
| Change database schema | `db/entity/`, `db/CarCrashDao.kt`, `db/CarCrashDatabase.kt` |
| Update dependencies | `buildSrc/src/main/kotlin/AppDependencies.kt` |
| Modify DI | `appModuleDi.kt` |
| Change theme | `ui/theme/` |
| Update API keys | `app/app.properties` |
