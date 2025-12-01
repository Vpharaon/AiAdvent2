plugins {
    kotlin("multiplatform") version "2.2.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(17)

    // Desktop target (JVM)
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        val desktopMain by getting {
            dependencies {
            }
        }

        val desktopTest by getting {
            dependencies {
            }
        }
    }
}