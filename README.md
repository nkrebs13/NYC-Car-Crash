# NYC Crashes
Shows the car crashes in the last 3 months in the NYC area as reported by [NYC OpenData](https://data.cityofnewyork.us/Public-Safety/Motor-Vehicle-Collisions-Crashes/h9gi-nx95). This repository demonstrates modern Android development with: Koin, Coroutines, Flow, MVVM, Room, and Jetpack Compose.

### Requirements
- Android Studio or Android Build Tools
- 2 API keys defined in app/app.properties with keys `API_KEY` and `MAP_KEY`
  - `API_KEY` is for the crash data. This can be retrieved from [data.cityofnewyork.us](https://data.cityofnewyork.us/profile/edit/developer_settings).
  - `MAP_KEY` is an API key for Google maps [docs](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

### Known issues
These is currently a known issue with the TileOverlay colors. It is believed by the developer that this is because of a `HeatmapTileOverlay` compatibility issue with Jetpack Compose. This issue results in inconsistent coloring as the user continually interacts with the map. 