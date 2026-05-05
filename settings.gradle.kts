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

// Until cmp-any-kotlin publishes to Maven Central, build it from a sibling
// path. The composite build uses substitution to resolve the
// `io.github.kotlinmania:cmp-any-kotlin` dependency declared in the
// dependencies block of build.gradle.kts.
includeBuild("../cmp-any-kotlin")

// Until starlarkmap-kotlin publishes (or if Maven resolution is unavailable
// locally), build it from a sibling path and substitute it for
// `io.github.kotlinmania:starlarkmap-kotlin`.
includeBuild("../starlarkmap-kotlin") {
    dependencySubstitution {
        substitute(module("io.github.kotlinmania:starlarkmap-kotlin")).using(project(":"))
    }
}

// Until starlark-syntax-kotlin publishes (or if Maven resolution is
// unavailable locally), build it from a sibling path and substitute it for
// `io.github.kotlinmania:starlark-syntax-kotlin`.
includeBuild("../starlark-syntax-kotlin") {
    dependencySubstitution {
        substitute(module("io.github.kotlinmania:starlark-syntax-kotlin")).using(project(":"))
    }
}
