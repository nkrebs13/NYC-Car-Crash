object AppVersions {
    const val MIN = 26
    const val TARGET = 35
    const val COMPILE = 35
}

object AndroidX {
    const val CORE = "androidx.core:core-ktx:1.15.0"

    const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.7"
    const val LIFECYCLE_VIEWMODEL_COMPOSE = "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7"

    const val ACTIVITY_COMPOSE = "androidx.activity:activity-compose:1.9.3"

    // Compose BOM manages all compose versions
    const val COMPOSE_BOM = "androidx.compose:compose-bom:2024.10.01"
    const val COMPOSE_UI = "androidx.compose.ui:ui"
    const val COMPOSE_MATERIAL = "androidx.compose.material:material"
    const val COMPOSE_MATERIAL3 = "androidx.compose.material3:material3"
    const val COMPOSE_UI_PREVIEW = "androidx.compose.ui:ui-tooling-preview"
    // should be debug dependency only
    const val COMPOSE_UI_TOOLING = "androidx.compose.ui:ui-tooling"
    // should be debug dependency only
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest"

    const val VERSION_ROOM = "2.6.1"
    const val ROOM_RUNTIME = "androidx.room:room-runtime:$VERSION_ROOM"
    const val ROOM_KTX = "androidx.room:room-ktx:$VERSION_ROOM"
    const val ROOM_COMPILER = "androidx.room:room-compiler:$VERSION_ROOM"

    const val GOOGLE_MAPS_COMPOSE = "com.google.maps.android:maps-compose:6.1.2"
    const val GOOGLE_PLAY_SERVICES_MAPS = "com.google.android.gms:play-services-maps:19.0.0"
    const val GOOGLE_MAPS_UTILS = "com.google.maps.android:maps-utils-ktx:5.1.1"
}

object Kotlin {
    const val VERSION = "2.0.21"
    const val SERIALIZATION = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3"
    const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0"
    const val COROUTINES_ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"
}

object Networking {
    const val VERSION_KTOR = "3.0.1"
    const val KTOR_CORE = "io.ktor:ktor-client-core:$VERSION_KTOR"
    const val KTOR_CLIENT = "io.ktor:ktor-client-okhttp:$VERSION_KTOR"
    const val KTOR_LOGGING = "io.ktor:ktor-client-logging:$VERSION_KTOR"
    const val KTOR_CONTENT_NEGOTIATION = "io.ktor:ktor-client-content-negotiation:$VERSION_KTOR"
    const val KTOR_SERIALIZATION = "io.ktor:ktor-serialization-kotlinx-json:$VERSION_KTOR"
}

object Koin {
    const val VERSION = "4.0.0"
    const val CORE = "io.insert-koin:koin-android:$VERSION"
    const val COMPOSE = "io.insert-koin:koin-androidx-compose:$VERSION"
}

object MiscLibraries {
    const val MPANDROID_CHART = "com.github.PhilJay:MPAndroidChart:v3.1.0"
}

object Testing {
    const val JUNIT = "junit:junit:4.13.2"
    const val JUNIT_EXT = "androidx.test.ext:junit:1.2.1"
    const val ESPRESSO = "androidx.test.espresso:espresso-core:3.6.1"
    const val JUNIT_UI_COMPOSE = "androidx.compose.ui:ui-test-junit4"
}
