# NYC Crashes

Shows the car crashes in the last 3 months in the NYC area as reported by [NYC OpenData]
(https://data.cityofnewyork.us/Public-Safety/Motor-Vehicle-Collisions-Crashes/h9gi-nx95). This
repository demonstrates modern Android development using the latest best practices, tools, and
guidelines.

### Requirements

- Android Studio or Android Build Tools
- Create app/app.properties with 2 keys:
    - `API_KEY` is for the crash data. This can be retrieved
      from [data.cityofnewyork.us](https://data.cityofnewyork.us/profile/edit/developer_settings).
    - `MAP_KEY` is an API key for Google
      maps [docs](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

### Tech Stack

- MVVM app architecture
- Room
- Jetpack Compose
- Koin
- Coroutines + Flow
- Ktor
- Android
- MPAndroidChart

### Known issues

These is currently an issue using `TileOverlay` with Jetpack Compose that results in performance
issues and inconsistent loading of the map areas. Zoom in and out of the map can fix the map loading
issues. The performance issues can be slightly improved by ensuring to launch the "release" mode of
the application.