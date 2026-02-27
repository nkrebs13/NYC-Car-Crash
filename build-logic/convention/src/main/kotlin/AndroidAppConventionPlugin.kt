import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.JavaVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.File
import java.time.LocalDate
import java.util.Properties

class AndroidAppConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            // --- CalVer versioning ---------------------------------------------------
            val versionProps = Properties().apply {
                val file = File(rootProject.rootDir, "version.properties")
                if (file.exists()) file.inputStream().use { load(it) }
            }
            val patch = (versionProps["VERSION_PATCH"] as? String)?.toIntOrNull() ?: 0

            val now = LocalDate.now()
            val year = now.year
            val month = now.monthValue

            // versionCode: YYYYMM00 + patch  (e.g. 20260200)
            val calVersionCode = year * 10000 + month * 100 + patch
            // versionName: YYYY.M.PATCH       (e.g. 2026.2.0)
            val calVersionName = "$year.$month.$patch"

            // --- Apply Android application plugin if not already applied -------------
            pluginManager.apply("com.android.application")

            extensions.configure<ApplicationExtension> {
                defaultConfig {
                    versionCode = calVersionCode
                    versionName = calVersionName
                }
                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }
            }

            // Configure Kotlin JVM target for all Kotlin compile tasks
            tasks.withType<KotlinCompile>().configureEach {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }

            // Register a printVersion task for CI verification
            tasks.register("printVersion") {
                doLast {
                    println("VERSION_CODE=$calVersionCode")
                    println("VERSION_NAME=$calVersionName")
                }
            }
        }
    }
}
