plugins {
    `kotlin-dsl`
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApp") {
            id = "nyccarcrash.android.app"
            implementationClass = "AndroidAppConventionPlugin"
        }
    }
}
