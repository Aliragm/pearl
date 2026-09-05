plugins {
    alias(libs.plugins.kotlin.multiplatform)
}

group = "gg.pearl.mobile"
version = "1.0.0-SNAPSHOT"

kotlin {
    jvm("desktop")

    // iOS Targets (iPhone / iPad)
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "PearlMobile"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.logback.classic)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        val desktopMain by getting {
            dependencies {
                // Desktop specific dependencies
            }
        }
    }
}
