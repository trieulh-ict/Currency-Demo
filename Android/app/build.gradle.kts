import java.util.Properties
import kotlin.text.Charsets
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.tasks.JacocoReport

data class CoverageSourceInfo(
    val relativeDir: String,
    val baseName: String,
    val skipCoverage: Boolean,
)

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
    id("com.google.devtools.ksp")
    id("jacoco")
}

android {
    namespace = "io.trieulh.currencydemo"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.trieulh.currencydemo"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()
        val localProperties = project.rootProject.file("local.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use(properties::load)
        }
        // This is for testing only. Do not give default passphrase in production
        val sqlCipherPassphrase =
            properties.getProperty("sqlcipher.passphrase")?.takeIf { it.isNotBlank() }
                ?: "default_passphrase"
        buildConfigField(
            "String",
            "SQLCIPHER_PASSPHRASE",
            "\"${sqlCipherPassphrase}\""
        )
        buildConfigField(
            "String",
            "BASE_URL",
            "\"https://currency-demo-production.up.railway.app/\""
        )

        // In case you want to run /Backend project in local
//        buildConfigField(
//            "String",
//            "BASE_URL",
//            "\"http://10.0.2.2:3000/\""
//        )
    }

    buildTypes {
        debug {
            enableAndroidTestCoverage = true
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = false
        unitTests.all {
            testCoverage {
                version = "0.8.11"
            }
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization.converter)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // SQLCipher
    implementation(libs.sqlcipher)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.androidx.sqlite.ktx)

    // Kotlinx Serialization
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // Testing
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.junit.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.android)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlin.test.junit)
}

val coverageRunProperty =
    providers.gradleProperty("coverageRun").map { it.toBoolean() }.orElse(false)
val requestedTasks = gradle.startParameter.taskNames
val isCoverageRun = coverageRunProperty.get() || requestedTasks.any { taskName ->
    taskName.contains("coverage", ignoreCase = true) || taskName.contains("jacoco", ignoreCase = true)
}

val coverageSourceInfo by lazy {
    val mainSourceDir = projectDir.resolve("src/main/java")
    if (!mainSourceDir.exists()) {
        emptyList()
    } else {
        fileTree(mainSourceDir) {
            include("**/*.kt")
        }.files.map { file ->
            val content = file.readText(Charsets.UTF_8)
            val relativePath = file.relativeTo(mainSourceDir).invariantSeparatorsPath
            val relativeDir = relativePath.substringBeforeLast("/", "")
            CoverageSourceInfo(
                relativeDir = relativeDir,
                baseName = file.nameWithoutExtension,
                skipCoverage = content.contains("@Composable") || content.contains("@Dao"),
            )
        }
    }
}

fun CoverageSourceInfo.asClassPatterns(): List<String> {
    val directorySegment =
        if (relativeDir.isBlank()) "" else "${relativeDir.trimStart('/')}/"
    return listOf(
        "**/${directorySegment}${baseName}.class",
        "**/${directorySegment}${baseName}\$*.class",
        "**/${directorySegment}${baseName}Kt.class",
        "**/${directorySegment}${baseName}Kt\$*.class",
    )
}

val coverageIncludePatterns by lazy {
    coverageSourceInfo
        .filterNot(CoverageSourceInfo::skipCoverage)
        .flatMap { it.asClassPatterns() }
        .toSet()
}

val coverageSkipPatterns by lazy {
    coverageSourceInfo
        .filter(CoverageSourceInfo::skipCoverage)
        .flatMap { it.asClassPatterns() }
        .toSet()
}

val generatedClassExclusionPatterns by lazy {
    setOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*_Hilt*.class",
        "**/*_Hilt*.*",
        "**/*Hilt*Component*.*",
        "**/*_Component*.*",
        "**/*_Factory.class",
        "**/*_Factory\$*.class",
        "**/*_Impl*.class",
        "**/*_Generated*.*",
        "**/*_MembersInjector*.*",
        "**/*_ProvideFactory*.*",
    ) + coverageSkipPatterns
}

val androidJUnit4TestClasses by lazy {
    val testSourceDir = projectDir.resolve("src/test/java")
    if (!testSourceDir.exists()) {
        emptySet<String>()
    } else {
        fileTree(testSourceDir) {
            include("**/*.kt")
        }.files.mapNotNull { file ->
            val content = file.readText(Charsets.UTF_8)
            if (!content.contains("AndroidJUnit4")) {
                null
            } else {
                val packageName =
                    Regex("""(?m)^\s*package\s+([^\s]+)""").find(content)?.groupValues?.get(1)
                val className =
                    Regex("""(?m)^\s*class\s+([A-Za-z0-9_]+)""").find(content)?.groupValues?.get(1)
                if (packageName != null && className != null) {
                    "$packageName.$className"
                } else {
                    null
                }
            }
        }.toSet()
    }
}

if (isCoverageRun) {
    tasks.withType<Test>().configureEach {
        val excludedClasses = androidJUnit4TestClasses
        if (excludedClasses.isNotEmpty()) {
            filter {
                excludedClasses.forEach { className ->
                    excludeTestsMatching(className)
                }
            }
        }
    }

    tasks.withType<JacocoReport>().configureEach {
        val includePatterns = coverageIncludePatterns
        val excludePatterns = generatedClassExclusionPatterns

        val updatedDirectories = classDirectories.files.map { directory ->
            fileTree(directory) {
                includePatterns.forEach { pattern -> include(pattern) }
                excludePatterns.forEach { pattern -> exclude(pattern) }
            }
        }
        classDirectories.setFrom(updatedDirectories)

        val mainSourceDir = projectDir.resolve("src/main/java")
        sourceDirectories.setFrom(files(mainSourceDir))
        additionalSourceDirs.setFrom(files(mainSourceDir))
    }
}
