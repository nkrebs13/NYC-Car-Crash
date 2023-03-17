object AppVersions {
    const val MIN = 24
    const val TARGET = 33
    const val COMPILE = 33
}

object AndroidX {
    const val CORE = "androidx.core:core-ktx:1.7.0"

    const val LIFECYCLE_RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:2.3.1"

    const val ACTIVITY_COMPOSE = "androidx.activity:activity-compose:1.3.1"

    const val VERSION_COMPOSE = "1.3.3"
    const val VERSION_COMPOSE_COMPILER = "1.4.3"
    const val COMPOSE_UI = "androidx.compose.ui:ui:$VERSION_COMPOSE"
    const val COMPOSE_UI_PREVIEW = "androidx.compose.ui:ui-tooling-preview:$VERSION_COMPOSE"
    // should be debug dependency only
    const val COMPOSE_UI_TOOLING = "androidx.compose.ui:ui-tooling:$VERSION_COMPOSE"
    // should be debug dependency only
    const val COMPOSE_UI_TEST_MANIFEST = "androidx.compose.ui:ui-tooling:$VERSION_COMPOSE"

    const val MATERIAL3 = "androidx.compose.material3:material3:1.0.1"
}

object Kotlin {
    const val VERSION = "1.8.10"
    const val SERIALIZATION = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1"
}

object Testing {
    const val JUNIT = "junit:junit:4.13.2"
    const val JUNIT_EXT = "androidx.test.ext:junit:1.1.5"
    const val ESPRESSO = "androidx.test.espresso:espresso-core:3.5.1"
    const val JUNIT_UI_COMPOSE = "androidx.compose.ui:ui-test-junit4:${AndroidX.VERSION_COMPOSE}"
}
