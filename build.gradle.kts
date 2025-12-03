plugins {
    kotlin("multiplatform") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain(17)

    // Desktop target (JVM)
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
                implementation("io.ktor:ktor-client-core:3.0.1")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
                implementation("io.ktor:ktor-client-logging:3.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
                implementation("io.insert-koin:koin-core:4.0.0")

                // MVIKotlin
                implementation("com.arkivanov.mvikotlin:mvikotlin:4.0.0")
                implementation("com.arkivanov.mvikotlin:mvikotlin-main:4.0.0")
                implementation("com.arkivanov.mvikotlin:mvikotlin-extensions-coroutines:4.0.0")

                // Decompose
                implementation("com.arkivanov.decompose:decompose:3.0.0")
                implementation("com.arkivanov.decompose:extensions-compose:3.0.0")
                implementation("com.arkivanov.essenty:lifecycle:2.0.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.runtime)
                implementation("io.ktor:ktor-client-cio:3.0.1")
                implementation("io.insert-koin:koin-compose:4.0.0")
                implementation("ch.qos.logback:logback-classic:1.4.11")
                implementation("org.jetbrains:markdown:0.7.3")
            }
        }

        val desktopTest by getting {
            dependencies {
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}