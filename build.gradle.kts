plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
    id("org.jetbrains.kotlin.plugin.compose") version Kotlin.VERSION apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Kotlin.VERSION}")
        classpath("org.jetbrains.kotlin:kotlin-serialization:${Kotlin.VERSION}")
    }
}
