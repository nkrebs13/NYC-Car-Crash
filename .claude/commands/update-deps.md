# Update Dependencies

Check and update project dependencies.

## Instructions

1. First, show current dependency versions from `buildSrc/src/main/kotlin/AppDependencies.kt`

2. Check for updates:
   - Look up latest stable versions for key dependencies
   - Consider compatibility between dependencies (e.g., Kotlin version with Compose compiler)

3. If updating:
   - Update versions in `AppDependencies.kt`
   - Check for breaking changes in changelogs
   - Update source code if APIs changed
   - Run build to verify: `./gradlew assembleDebug`

4. Report:
   - List of updated dependencies
   - Any breaking changes encountered
   - Required code modifications

## Key Dependencies to Track

- Kotlin
- Compose BOM
- Room
- Ktor
- Koin
- Coroutines
- Google Maps Compose
- Android Gradle Plugin
- Gradle Wrapper
