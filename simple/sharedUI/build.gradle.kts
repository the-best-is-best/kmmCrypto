import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

kotlin {
    jvmToolchain(17)



    jvm()

    js {
        browser()
        binaries.executable()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)

            implementation(project(":kmmCrypto"))
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ui.test)
        }

        androidMain.dependencies {
            implementation(libs.ui.tooling)
            implementation(libs.androidx.activityCompose)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }

        iosMain.dependencies {
        }

    }
    android {
        namespace = "io.github.sample"
        compileSdk = 36
        minSdk = 24
    }

}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.github.sample.desktopApp"
            packageVersion = "1.0.0"
        }
    }
}

