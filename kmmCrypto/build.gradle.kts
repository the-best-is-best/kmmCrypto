@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    id("maven-publish")
    id("signing")
    alias(libs.plugins.maven.publish)
}




apply(plugin = "maven-publish")
apply(plugin = "signing")


tasks.withType<PublishToMavenRepository> {
    val isMac = getCurrentOperatingSystem().isMacOsX
    onlyIf {
        isMac.also {
            if (!isMac) logger.error(
                """
                    Publishing the library requires macOS to be able to generate iOS artifacts.
                    Run the task on a mac or use the project GitHub workflows for publication and release.
                """
            )
        }
    }
}



mavenPublishing {
    coordinates("io.github.the-best-is-best", "kmm-crypto", "2.0.0")

    publishToMavenCentral(true)
    signAllPublications()

    pom {
        name.set("KMM Crypto")
        description.set("This package for encrypt or decrypt data in kotlin multiplatform")
        url.set("https://github.com/the-best-is-best/kmm-crypto")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
            }
        }
        issueManagement {
            system.set("Github")
            url.set("https://github.com/the-best-is-best/kmm-crypto/issues")
        }
        scm {
            connection.set("https://github.com/the-best-is-best/kmm-crypto.git")
            url.set("https://github.com/the-best-is-best/kmm-crypto")
        }
        developers {
            developer {
                id.set("MichelleRaouf")
                name.set("Michelle Raouf")
                email.set("eng.michelle.raouf@gmail.com")
            }
        }
    }

}


signing {
    useGpgCmd()
    sign(publishing.publications)
}
signing {
    useGpgCmd()
    sign(publishing.publications)
}

kotlin {
    jvmToolchain(17)
    androidTarget {
        //https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-test.html
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }


    jvm()

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    js {
        browser()
        binaries.executable()
    }

    wasmJs {
        browser()
        binaries.executable()
    }

    kotlin {
        // iOS targets configuration
        val iosTargets = listOf(
            iosX64(),        // Intel simulators
            iosArm64(),      // Apple Silicon (M1/M2) devices and simulators
            iosSimulatorArm64()  // Apple Silicon simulators (explicit)
        )

        iosTargets.forEach { target ->
            target.binaries.framework {
                baseName = "KMMCrypto"
            }

            target.compilations.getByName("main").cinterops {
                val kmmcrypto by creating {
                    // Use different .def files based on target
                    defFile(project.file("native/ios_crypto_interop.def"))
                    packageName("io.native.kmmcrypto")
                }
            }
        }
    }


//
//    cocoapods {
//        version = "1.0"
//        summary = "Some description for a Kotlin/Native module"
//        homepage = "Link to a Kotlin/Native module homepage"
//
//        // Optional properties
//        // Configure the Pod name here instead of changing the Gradle project name
//        name = "KMMCtypto"
//
//        framework {
//            baseName = "KMMCtypto"
//        }
//        noPodspec()
//        ios.deploymentTarget = "12.0"  // Adjust this version to match KServices
//
//        pod("KServices") {
//            version = "0.2.2"
//            extraOpts += listOf("-compiler-option", "-fmodules")
//
//        }
//
//    }
    sourceSets {

        commonMain.dependencies {

            implementation(libs.kotlinx.coroutines.core)
//            implementation(compose.runtime)
//            implementation(compose.foundation)
//            implementation(compose.material3)
//            implementation(compose.components.resources)
//            implementation(compose.components.uiToolingPreview)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
//            @OptIn(ExperimentalComposeLibrary::class)
//            implementation(compose.uiTest)
        }

        androidMain.dependencies {
//            implementation(compose.uiTooling)
//            implementation(libs.androidx.activityCompose)
            implementation(libs.androidx.startup.runtime)
            implementation(libs.androidx.annotation)

        }

        jvmMain.dependencies {
//            implementation(compose.desktop.currentOs)
        }

        jsMain.dependencies {
//            implementation(compose.html.core)
        }

        iosMain.dependencies {
        }

        macosMain.dependencies {

        }
        nativeMain.dependencies { }

        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser.wasm.js)

        }


    }
}

android {
    namespace = "io.github.kmmcrypto"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
    }
}
