import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.android.kotlin.multiplatform.library") version "8.6.0"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.github.kotlinmania"
version = "0.1.0-SNAPSHOT"

// Android setup
val sdkDir = file(".android-sdk")
val licensesDir = sdkDir.resolve("licenses")
if (!licensesDir.exists()) licensesDir.mkdirs()
val licenseFile = licensesDir.resolve("android-sdk-license")
if (!licenseFile.exists()) {
    licenseFile.writeText(
        """
        8933bad161af4178b1185d1a37fbf41ea5269c55
        d56f5187479451eabf01fb74abc367c344559d7b
        24333f8a63b6825ea9c5514f83c2829b004d1fee
        """.trimIndent()
    )
}
val localProperties: File? = rootProject.file("local.properties")
if (localProperties?.exists() == false) {
    localProperties.writeText("sdk.dir=${sdkDir.absolutePath}")
}

kotlin {
    applyDefaultHierarchyTemplate()

    sourceSets.all { languageSettings.optIn("kotlin.time.ExperimentalTime") }

    val xcf = XCFramework("Starlark")

    macosArm64 {
        binaries.framework {
            baseName = "Starlark"
            xcf.add(this)
        }
    }
    macosX64 {
        binaries.framework {
            baseName = "Starlark"
            xcf.add(this)
        }
    }
    linuxX64()
    mingwX64()
    iosArm64 {
        binaries.framework {
            baseName = "Starlark"
            xcf.add(this)
        }
    }
    iosX64 {
        binaries.framework {
            baseName = "Starlark"
            xcf.add(this)
        }
    }
    iosSimulatorArm64 {
        binaries.framework {
            baseName = "Starlark"
            xcf.add(this)
        }
    }
    js {
        browser()
        nodejs()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    androidLibrary {
        namespace = "io.github.kotlinmania.starlark_kotlin"
        compileSdk = 34
        minSdk = 24
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
            }
        }

        val commonTest by getting { dependencies { implementation(kotlin("test")) } }
    }
    jvmToolchain(21)
}


mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "starlark-kotlin", version.toString())

    pom {
        name.set("starlark-kotlin")
        description.set("Kotlin Multiplatform port of facebook/starlark-rust - Starlark configuration language interpreter")
        inceptionYear.set("2026")
        url.set("https://github.com/KotlinMania/starlark-kotlin")

        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://opensource.org/licenses/Apache-2.0")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("sydneyrenee")
                name.set("Sydney Renee")
                email.set("sydney@solace.ofharmony.ai")
                url.set("https://github.com/sydneyrenee")
            }
        }

        scm {
            url.set("https://github.com/KotlinMania/starlark-kotlin")
            connection.set("scm:git:git://github.com/KotlinMania/starlark-kotlin.git")
            developerConnection.set("scm:git:ssh://github.com/KotlinMania/starlark-kotlin.git")
        }
    }
}
