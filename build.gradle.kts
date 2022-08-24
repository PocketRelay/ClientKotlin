import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.jacobtread.relay.app"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                // Netty Dependencies
                val nettyVersion: String by project
                implementation("io.netty:netty-handler:$nettyVersion")
                implementation("io.netty:netty-buffer:$nettyVersion")

                // Blaze Dependencies
                val blazeVersion: String by project
                implementation("com.jacobtread.blaze:blaze-core:$blazeVersion")
                implementation("com.jacobtread.blaze:blaze-annotations:$blazeVersion")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "Main"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PocketRelayClient"
            packageVersion = "1.0.0"
        }
    }
}
