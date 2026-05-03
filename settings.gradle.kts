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
