pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
    repositories {
        maven {
            url = uri("local-repo")
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "starlark-kotlin"
