pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins { kotlin("multiplatform") version "2.3.20" }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" }

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

val starlarkMapBuild =
    listOf(
        file("deps/starlarkmap-kotlin"),
        file("../starlarkmap-kotlin"),
    ).firstOrNull { it.resolve("settings.gradle.kts").isFile }

if (starlarkMapBuild != null) {
    includeBuild(starlarkMapBuild) {
        dependencySubstitution {
            substitute(module("io.github.kotlinmania:starlarkmap")).using(project(":"))
        }
    }
}

rootProject.name = "starlark"
