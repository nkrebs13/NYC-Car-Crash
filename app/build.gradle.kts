import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlinx-serialization")
    id("com.google.devtools.ksp")
}

val appProperties = Properties().apply {
    load(FileInputStream(File("${rootProject.rootDir}/app", "app.properties")))
}

val apiKey: String = (appProperties["api_key"] as? String) ?: ""
if(apiKey.isEmpty()) {
    throw IllegalStateException("You must input a valid api_key to the app.properties file. See " +
            "the README for more information")
}

val mapKey: String = (appProperties["map_key"] as? String) ?: ""
// TODO - Future commit
//if(mapKey.isEmpty()) {
//    throw IllegalStateException("You must input a valid map_key to the app.properties file. See " +
//            "the README for more information")
//}

android {
    namespace = "com.nathankrebs.nyccrash"
    compileSdk = AppVersions.COMPILE

    defaultConfig {
        applicationId = "com.nathankrebs.nyccrash"
        minSdk = AppVersions.MIN
        targetSdk = AppVersions.TARGET
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resValue("string", "api_key", apiKey)
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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
    implementation(AndroidX.COMPOSE_UI_PREVIEW)
    implementation(AndroidX.MATERIAL3)
    implementation(AndroidX.ROOM_RUNTIME)
    implementation(AndroidX.ROOM_KTX)
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

    debugImplementation(AndroidX.COMPOSE_UI_TOOLING)
    debugImplementation(AndroidX.COMPOSE_UI_TOOLING)
    debugImplementation(AndroidX.COMPOSE_UI_TEST_MANIFEST)

    ksp(AndroidX.ROOM_COMPILER)

    testImplementation(Testing.JUNIT)

    androidTestImplementation(Testing.JUNIT_EXT)
    androidTestImplementation(Testing.ESPRESSO)
    androidTestImplementation(Testing.JUNIT_UI_COMPOSE)
}