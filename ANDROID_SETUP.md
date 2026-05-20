# Android SDK setup

This repo installs a project-local Android SDK from Gradle so contributors can
build the `android` Kotlin Multiplatform target without installing Android
Studio or mutating a shared system SDK.

If you only build non-Android targets such as `macosArm64Test`, `jsNodeTest`, or
`wasmJsNodeTest`, you can ignore this document. The setup is only needed when a
Gradle invocation touches Android tasks such as `compileAndroidMain`,
`assembleUnitTest`, `assembleAndroidTest`, `androidSourcesJar`, or Maven Central
publishing.

## What Gradle Does

`build.gradle.kts` owns the installer. During configuration it:

1. Downloads Google's command-line tools revision `14742923` for the host OS.
2. Expands them under `.android-sdk/cmdline-tools/latest/`.
3. Accepts SDK licenses with a finite input stream.
4. Installs `platform-tools`, `platforms;android-34`, and `build-tools;36.0.0`.
5. Writes `local.properties` with `sdk.dir=<repo>/.android-sdk`.
6. Records `.android-sdk/.install-complete` so later runs are fast.

The `.android-sdk/` tree and `local.properties` are local build artifacts and
are ignored by git.

## Run It

```bash
./gradlew setupAndroidSdk --no-daemon --console=plain
```

Then prove the Android target:

```bash
./gradlew compileAndroidMain --no-daemon --console=plain
```

`setupAndroidSdk` is intentionally a Gradle task, not an external shell or batch
entrypoint. CI workflows that run Android-related work should call it in the
same Gradle invocation before the Android task:

```bash
./gradlew setupAndroidSdk compileAndroidMain --no-configuration-cache
```

## Troubleshooting

- **Gradle cannot find the SDK**: run `./gradlew setupAndroidSdk --no-daemon
  --console=plain`, then confirm `local.properties` contains a `sdk.dir` value
  pointing at this repo's `.android-sdk` directory.
- **The command-line tools download fails**: verify connectivity to
  `dl.google.com`; the configured tools revision is pinned in
  `build.gradle.kts` as `androidCommandLineToolsRevision`.
- **The SDK layout looks stale**: delete `.android-sdk/` and rerun
  `./gradlew setupAndroidSdk --no-daemon --console=plain`.
- **A workflow needs Android work**: include `setupAndroidSdk` before
  `compileAndroidMain`, `assembleUnitTest`, `assembleAndroidTest`,
  `androidSourcesJar`, or publish tasks in the Gradle command.
