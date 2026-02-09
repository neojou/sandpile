import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform") version "2.3.0"
    id("org.jetbrains.compose") version "1.10.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0"
    id("org.jetbrains.dokka") version "2.1.0"
}

group = "com.neojou.sandpile"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js")
}

kotlin {
    jvm("desktop")
    jvmToolchain(25)

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("sandpile")
        browser { }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
            }
        }
        val commonTest by getting {
            dependencies { implementation(kotlin("test")) }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
            }
        }
        val wasmJsMain by getting {
            // NEW: 移除 implementation(kotlin("js"))，用 raw js() 替代
        }
    }
}

dokka {
    dokkaPublications {
        html {
            outputDirectory.set(layout.projectDirectory.dir("docs/api"))
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.neojou.MainKt"
    }
}

