# Install on Device

Build and install the app on a connected device or emulator.

## Instructions

1. Check for connected devices: `adb devices`
2. Build and install: `./gradlew installDebug`
3. Optionally launch the app: `adb shell am start -n com.nathankrebs.nyccrash/.ui.MainActivity`

Report:
- Device the app was installed on
- Installation success or failure
- Any errors encountered

## Prerequisites
- Connected Android device or running emulator
- USB debugging enabled on device
- API keys configured in `app/app.properties`
