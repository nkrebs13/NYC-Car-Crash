import java.io.FileInputStream
import java.util.Properties

plugins {
    id("nyccarcrash.android.app")
    kotlin("android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

val appPropertiesFile = File("${rootProject.rootDir}/app", "app.properties")
val appProperties = Properties().apply {
    if (appPropertiesFile.exists()) {
        load(FileInputStream(appPropertiesFile))
    }
}

val apiKey: String = (appProperties["api_key"] as? String) ?: ""
val mapKey: String = (appProperties["map_key"] as? String) ?: ""

// Warn about missing or placeholder API keys, but allow build to proceed for CI
if (apiKey.isEmpty() || apiKey.startsWith("CI_PLACEHOLDER")) {
    logger.warn("WARNING: api_key is not configured. The app will not function correctly.")
    logger.warn("See the README for information on configuring app.properties")
}
if (mapKey.isEmpty() || mapKey.startsWith("CI_PLACEHOLDER")) {
    logger.warn("WARNING: map_key is not configured. Google Maps will not display correctly.")
    logger.warn("See the README for information on configuring app.properties")
}

val keystorePropertiesFile = File(rootProject.rootDir, "keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

android {
    namespace = "com.nathankrebs.nyccrash"
    compileSdk = AppVersions.COMPILE

    // Only configure release signing if keystore.properties exists and has required values
    val hasSigningConfig = keystorePropertiesFile.exists() &&
        keystoreProperties["storeFile"] != null

    if (hasSigningConfig) {
        signingConfigs {
            create("release") {
                keyAlias = keystoreProperties["keyAlias"] as? String
                keyPassword = keystoreProperties["keyPassword"] as? String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as? String
            }
        }
    }

    defaultConfig {
        applicationId = "com.nathankrebs.nyccrash"
        minSdk = AppVersions.MIN
        targetSdk = AppVersions.TARGET

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "api_key", apiKey)
        manifestPlaceholders["MAP_KEY"] = mapKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
            if (hasSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = AndroidX.VERSION_COMPOSE_COMPILER
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(AndroidX.CORE)
    implementation(AndroidX.LIFECYCLE_RUNTIME)
    implementation(AndroidX.ACTIVITY_COMPOSE)
    implementation(AndroidX.COMPOSE_UI)
    implementation(AndroidX.COMPOSE_MATERIAL)
    implementation(AndroidX.COMPOSE_UI_PREVIEW)
    implementation(AndroidX.ROOM_RUNTIME)
    implementation(AndroidX.ROOM_KTX)
    implementation(AndroidX.GOOGLE_MAPS_COMPOSE)
    implementation(AndroidX.GOOGLE_PLAY_SERVICES_MAPS)
    implementation(AndroidX.GOOGLE_MAPS_UTILS)
    implementation(Kotlin.SERIALIZATION)
    implementation(Kotlin.COROUTINES)
    implementation(Kotlin.COROUTINES_ANDROID)
    implementation(Networking.KTOR_CORE)
    implementation(Networking.KTOR_CLIENT)
    implementation(Networking.KTOR_LOGGING)
    implementation(Networking.KTOR_CONTENT_NEGOTIATION)
    implementation(Networking.KTOR_SERIALIZATION)
    implementation(Koin.CORE)
    implementation(Koin.COMPOSE)
    implementation(MiscLibraries.MPANDROID_CHART)

    debugImplementation(AndroidX.COMPOSE_UI_TOOLING)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)

    ksp(AndroidX.ROOM_COMPILER)

    testImplementation(Testing.JUNIT)

    androidTestImplementation(Testing.JUNIT_EXT)
    androidTestImplementation(Testing.ESPRESSO)
    androidTestImplementation(Testing.JUNIT_UI_COMPOSE)
}
