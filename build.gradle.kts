plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    id("com.android.library") version "8.11.1" apply false
}

// =============================================================================
// AST Distance Tool Build Tasks
// =============================================================================

val astDistanceDir: File = project.file("tools/ast_distance")
val astDistanceBuildDir = astDistanceDir.resolve("build")
val astDistanceBinary = astDistanceBuildDir.resolve("ast_distance")
val astDistanceOutput: File = project.file("tools/ast_distance")

tasks.register<Exec>("configureAstTool") {
    description = "Configure AST distance tool with CMake"
    group = "build"

    workingDir = astDistanceBuildDir
    commandLine("cmake", "..")

    doFirst {
        astDistanceBuildDir.mkdirs()
    }

    onlyIf { !astDistanceBuildDir.resolve("Makefile").exists() }
}

tasks.register<Exec>("buildAstTool") {
    description = "Build AST distance tool"
    group = "build"

    dependsOn("configureAstTool")
    workingDir = astDistanceBuildDir
    commandLine("cmake", "--build", ".", "-j8")

    doLast {
        // Copy binary to tools folder for easy access
        if (astDistanceBinary.exists()) {
            astDistanceBinary.copyTo(astDistanceOutput.resolve("ast_distance"), overwrite = true)
            println("AST distance tool built: ${astDistanceOutput.resolve("ast_distance")}")
        }
    }
}

// =============================================================================
// Lint Tasks
// =============================================================================

val kotlinSrcDir: File = project.file("src/nativeMain/kotlin")

tasks.register<Exec>("portLint") {
    description = "Run port-lint checks on Kotlin codebase"
    group = "verification"

    dependsOn("buildAstTool")
    workingDir = astDistanceBuildDir

    commandLine(
        "./ast_distance", "--lint",
        kotlinSrcDir.absolutePath
    )

    isIgnoreExitValue = true

    doLast {
        println("\nPort lint completed. See above for any issues.")
    }
}

tasks.register<Exec>("portTodos") {
    description = "Scan for TODOs in ported Kotlin code"
    group = "verification"

    dependsOn("buildAstTool")
    workingDir = astDistanceBuildDir

    commandLine(
        "./ast_distance", "--todos",
        kotlinSrcDir.absolutePath
    )

    isIgnoreExitValue = true
}

tasks.register<Exec>("portStats") {
    description = "Show porting statistics"
    group = "verification"

    dependsOn("buildAstTool")
    workingDir = astDistanceBuildDir

    commandLine(
        "./ast_distance", "--stats",
        kotlinSrcDir.absolutePath
    )

    isIgnoreExitValue = true
}

tasks.register<Exec>("portDeep") {
    description = "Run deep porting analysis (Rust -> Kotlin)"
    group = "verification"

    dependsOn("buildAstTool")
    workingDir = astDistanceBuildDir

    val codexRs = project.file("codex-rs")

    commandLine(
        "./ast_distance", "--deep",
        codexRs.absolutePath, "rust",
        kotlinSrcDir.absolutePath, "kotlin"
    )

    isIgnoreExitValue = true
}

tasks.register<Exec>("portMissing") {
    description = "Show files missing from Kotlin port"
    group = "verification"

    dependsOn("buildAstTool")
    workingDir = astDistanceBuildDir

    val codexRs = project.file("codex-rs")

    commandLine(
        "./ast_distance", "--missing",
        codexRs.absolutePath, "rust",
        kotlinSrcDir.absolutePath, "kotlin"
    )

    isIgnoreExitValue = true
}

tasks.register("lint") {
    description = "Run all lint checks (Kotlin compilation + port lints)"
    group = "verification"

    dependsOn("compileKotlinMacosArm64", "portLint")

    doLast {
        println("\n=== All lint checks completed ===")
    }
}

tasks.register("portAnalysis") {
    description = "Run full porting analysis (stats, TODOs, lint, deep analysis)"
    group = "verification"

    dependsOn("portStats", "portTodos", "portLint", "portDeep")
}

repositories {
    mavenCentral()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xreturn-value-checker=full")
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    applyDefaultHierarchyTemplate()

    jvmToolchain(17)

    sourceSets.all {
    }

    macosArm64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }
    macosX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
    }

    // Define Linux target so we can confine certain dependencies/code to Linux only
    linuxX64()
    
    val androidSdkAvailable = System.getenv("ANDROID_HOME") != null ||
        System.getenv("ANDROID_SDK_ROOT") != null ||
        file("local.properties").exists()

    if (androidSdkAvailable) {
        project.apply(plugin = "com.android.library")
        androidTarget {
            publishLibraryVariants("release")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
                implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.5.4")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
                
                // Ktor HTTP client
                implementation("io.ktor:ktor-client-core:3.0.3")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")
                implementation("io.ktor:ktor-client-auth:3.0.3")

                // File I/O
                implementation("com.squareup.okio:okio:3.9.0")

                // Tree-sitter parsing library bindings
                implementation("io.github.tree-sitter:ktreesitter:0.24.1")

                // JWT library (from Maven Central)
                implementation("io.github.kotlinmania:jwt-kotlin:0.1.0")
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.1")
            }
        }

        val nativeMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:3.0.3")

                // Character encoding support (for legacy codepage conversion)
                // fleeksoft-io provides JDK-like IO classes for Kotlin Multiplatform
                implementation("com.fleeksoft.io:io-core:0.0.4")
                implementation("com.fleeksoft.io:io:0.0.4")
                implementation("com.fleeksoft.charset:charset:0.0.5")
                implementation("com.fleeksoft.charset:charset-ext:0.0.5")

                implementation("io.github.tree-sitter:ktreesitter-bash:0.23.3")

                // TUI libraries (from Maven Central)
                implementation("io.github.kotlinmania:ratatui-kotlin:0.1.1")
                implementation("io.github.kotlinmania:ansi-to-tui-kotlin:0.1.0")
                implementation("io.github.kotlinmania:anstyle-kotlin:0.1.0")
                implementation("io.github.kotlinmania:kasuari-kotlin:0.1.0")
                implementation("io.github.kotlinmania:roff-kotlin:0.1.0")
                implementation("io.github.kotlinmania:cansi-kotlin:0.1.0")
            }
        }
        
        val nativeTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
