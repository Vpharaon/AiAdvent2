// ==================================================================================================
// BUILD CONFIGURATION
// ==================================================================================================
// Multiplatform Kotlin project with Compose Desktop UI
// Architecture: MVI (MVIKotlin) + Decompose for navigation
// ==================================================================================================

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.kotlin.compose)
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

// ==================================================================================================
// KOTLIN MULTIPLATFORM CONFIGURATION
// ==================================================================================================

kotlin {
    jvmToolchain(17)

    // --------------------------------------------------------------------------------------------------
    // Target Platforms
    // --------------------------------------------------------------------------------------------------
    jvm("desktop")

    // --------------------------------------------------------------------------------------------------
    // Source Sets
    // --------------------------------------------------------------------------------------------------
    sourceSets {
        // Common - Shared code for all platforms
        val commonMain by getting {
            dependencies {
                // Async & Concurrency
                implementation(libs.coroutines.core)

                // Networking
                implementation(libs.bundles.ktor.client)

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // Dependency Injection
                implementation(libs.bundles.koin)

                // Architecture - MVI
                implementation(libs.bundles.mvikotlin)

                // Architecture - Navigation
                implementation(libs.bundles.decompose)
            }
        }

        // Common Tests
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Desktop (JVM) - Platform-specific code
        val desktopMain by getting {
            dependencies {
                // Compose Desktop UI
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.runtime)

                // Networking - Engine
                implementation(libs.ktor.client.cio)

                // Async - Swing support
                implementation(libs.coroutines.swing)

                // Dependency Injection - Compose integration
                implementation(libs.koin.compose)

                // Logging
                implementation(libs.logback.classic)

                // UI - Markdown rendering
                implementation(libs.markdown)
            }
        }

        // Desktop Tests
        val desktopTest by getting {
            dependencies {
            }
        }
    }
}

// ==================================================================================================
// COMPOSE DESKTOP CONFIGURATION
// ==================================================================================================

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}