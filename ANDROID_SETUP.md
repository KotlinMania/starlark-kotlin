# Android SDK setup

This repo ships a project-local Android SDK installer so contributors can build
the `android` Kotlin Multiplatform target without installing Android Studio or
mutating a shared system SDK.

If you only build for non-Android targets (`macosArm64Test`, `jsNodeTest`,
`wasmJsNodeTest` — the `./gradlew test` gate) you can ignore this document
entirely. The setup is only needed when you actually invoke
`compileAndroidMain` or `androidSourcesJar`.

## What the script does

`setup-android-sdk.sh` follows Google's documented headless install
([reference](https://developer.android.com/tools/sdkmanager)):

1. Downloads `commandlinetools-<os>-<rev>_latest.zip` from Google.
2. Unzips into `.android-sdk/cmdline-tools/latest/` — the exact layout
   `sdkmanager` expects to locate itself.
3. Accepts every SDK licence non-interactively. (No `yes |`; the script uses
   a finite `printf` stream so `set -euo pipefail` doesn't kill the pipeline
   when `sdkmanager` closes its stdin.)
4. Installs `platform-tools`, `platforms;android-<COMPILE_SDK>` (default 34),
   `build-tools;<BUILD_TOOLS>` (default 34.0.0).
5. Writes `local.properties` pointing `sdk.dir` at `<repo>/.android-sdk`.

The `.android-sdk/` tree is `.gitignore`d, as is `local.properties`. Re-run the
script after a clean clone or when the SDK layout drifts; it is idempotent —
completed steps are skipped.

## Run it

```bash
./setup-android-sdk.sh
```

Defaults:

- `CMDLINETOOLS_REV=14742923` — bump when Google rotates revisions. The latest
  build number is listed at <https://developer.android.com/studio> under
  "Command line tools only".
- `COMPILE_SDK=34` — must match `compileSdk` in `build.gradle.kts`.
- `BUILD_TOOLS=34.0.0`.

Override per-run:

```bash
COMPILE_SDK=35 BUILD_TOOLS=35.0.0 CMDLINETOOLS_REV=11076708 ./setup-android-sdk.sh
```

## Verify

```bash
./gradlew compileAndroidMain
```

That step previously failed with `SDK location not found. Define a valid SDK
location with an ANDROID_SDK_ROOT environment variable or by setting the
sdk.dir path in your project's local properties file`. After the script runs,
`local.properties` carries the project-local SDK path and `compileAndroidMain`
configures cleanly.

## How `build.gradle.kts` cooperates

The build configures `local.properties` automatically whenever `ANDROID_SDK_ROOT`
or `ANDROID_HOME` is exported in the environment:

```kotlin
val androidSdkDir: String? =
    providers.environmentVariable("ANDROID_SDK_ROOT").orNull
        ?: providers.environmentVariable("ANDROID_HOME").orNull

if (androidSdkDir != null && file(androidSdkDir).exists()) {
    val localProperties = rootProject.file("local.properties")
    if (!localProperties.exists()) {
        val sdkDirPropertyValue = file(androidSdkDir).absolutePath.replace("\\", "/")
        localProperties.writeText("sdk.dir=$sdkDirPropertyValue")
    }
}
```

So there are two independent ways to get a working `compileAndroidMain`:

| Setup | When `local.properties` gets written | Where the SDK lives |
|---|---|---|
| `setup-android-sdk.sh` | The script writes it directly. | `<repo>/.android-sdk/` (per-repo) |
| `ANDROID_SDK_ROOT` / `ANDROID_HOME` env var | `build.gradle.kts` writes it on first configure. | The system / IDE-managed SDK the env var points at. |

CI uses the env-var path because GitHub Actions runners come with an Android
SDK pre-installed and `ANDROID_SDK_ROOT` exported.

## Replicating this in another `*-kotlin` repo

The script and this doc compose a portable pattern. To bring it to another
sibling that has `android { ... }` in its `build.gradle.kts`:

1. `cp /path/to/starlark-kotlin/setup-android-sdk.sh ./setup-android-sdk.sh`
2. `chmod +x setup-android-sdk.sh`
3. Confirm `.gitignore` already excludes `.android-sdk/` and `local.properties`
   (every starlark-template `*-kotlin` repo does).
4. Confirm `build.gradle.kts` carries the `androidSdkDir`/`local.properties`
   auto-write block shown above. (Every starlark-template `*-kotlin` repo
   does. If it's missing, copy it from this repo's `build.gradle.kts`.)
5. Copy this doc as `ANDROID_SETUP.md`. No substitutions needed — the doc is
   generic.
6. Commit on a feature branch and open a PR per the workspace's
   one-repo-at-a-time rule. **Never** loop this propagation across multiple
   sibling repos with a script; that pattern is the workspace's
   strongest-rule "do not do this" anti-pattern, born of the 2026-05-09
   bulk-rewrite disaster.

## Troubleshooting

- **"sdkmanager: command not found"** — Google's zip didn't unpack to
  `cmdline-tools/latest/`. Inspect `.android-sdk/cmdline-tools/` and confirm
  the binary lives at `.android-sdk/cmdline-tools/latest/bin/sdkmanager`.
  If not, delete `.android-sdk/` and re-run; the move into place is atomic
  in the script.
- **"Failed to download commandlinetools-…"** — verify connectivity to
  `dl.google.com` and that `CMDLINETOOLS_REV` is current.
- **`compileAndroidMain` still complains about `sdk.dir`** — confirm
  `local.properties` exists in the repo root and contains `sdk.dir=…/.android-sdk`.
  If the file points at a stale path (an old `~/Library/Android/sdk` from
  Android Studio, for example), delete `local.properties` and re-run the
  script.
- **The install log says "Warning: Could not create settings"** — the
  command-line tools couldn't reach the user-level `.android` directory.
  This is harmless for the project-local install and `sdkmanager` falls
  back to `.android-sdk/`.
