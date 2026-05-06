pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    plugins { kotlin("multiplatform") version "2.3.21" }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0" }

dependencyResolutionManagement {
    repositories {
        google()
        maven("https://repo1.maven.org/maven2/")
        mavenCentral()
    }
}

rootProject.name = "starlark"

// NOTE: These dependencies are not available from Maven Central yet, so we build them
// from sibling repos via composite builds.
includeBuild("../cmp-any-kotlin")

includeBuild("../starlarkmap-kotlin") {
    dependencySubstitution {
        substitute(module("io.github.kotlinmania:starlarkmap-kotlin")).using(project(":"))
    }
}

includeBuild("../starlark-syntax-kotlin") {
    dependencySubstitution {
        substitute(module("io.github.kotlinmania:starlark-syntax-kotlin")).using(project(":"))
    }
}
