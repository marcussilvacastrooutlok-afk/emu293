# Android port status

## Verified

- GitHub Actions Linux build passed on 2026-10-29 for commit `6432ac712f0b01747d696d38b4f8134ca5fca049`.
- The log confirms that the desktop `emu293` executable linked successfully after the core extraction.
- `src/boot_config.h`, `src/emulator_core.h`, and `src/emulator_core.cpp` provide shared boot settings and a reusable `Emu293::RunEmulator(const BootConfig&)` entry point.
- `android/CMakeLists.txt` defines the ARM64 core build using SDL2 built from source. It deliberately excludes the desktop wxWidgets frontend and Linux V4L2 webcam source, using `android/webcam_stub.cpp` for now.
- `.github/workflows/android-core-build.yml` successfully configured and compiled the core for `arm64-v8a` with Android NDK in GitHub Actions on 2026-10-29, commit `4ea4eea154a7247f2008b7f261f488ea8b458d17` (run [40924577126](https://github.com/marcussilvacastrooutlok-afk/emu293/actions/runs/40924577126)). The NDK installation, CMake configure, and core compile steps all passed.

This validates the Android ARM64 static core build only. No APK has been produced or device-tested yet.

## Android work still required

1. Add an Android app/activity, manifest, and JNI entry point that links SDL2 and calls `Emu293::RunEmulator`.
2. Add the Android app/activity and JNI entry point that calls `Emu293::RunEmulator`.
3. Replace the wxWidgets platform picker with an Android UI and Storage Access Framework document selection for the boot image and SD-card image.
4. Copy selected document contents into app-private storage when the core needs normal filesystem paths. Put save states in a writable app-private directory.
5. Keep the Linux V4L2 webcam implementation excluded initially. Camera emulation remains disabled until a proper Android Camera2/NDK backend is implemented.
6. Add touch controls while preserving SDL game-controller support for USB and Bluetooth controllers.
7. Add an Android ARM64 GitHub Actions build that uploads the APK as an artifact.
8. Install and test the resulting APK on an Android device; check startup, rendering, audio, touch, and external controllers before calling it functional.

## Current source constraints

- The desktop `src/emu293.cpp` still contains wxWidgets startup/selection code and is not part of the Android target.
- The Android CMake file currently builds only a core library. It expects an SDL2 CMake package to be supplied by the Android build integration and does not yet create an APK.
- The core expects ordinary filesystem paths for the SD-card image and boot image. Android document URIs cannot be passed directly without copying or adapting file access.
- The app must not bundle copyrighted ROM/system images. Users supply their own SD-card image and ELF/NOR boot image for a supported platform.

## Next implementation order

1. Add a minimal Android Gradle project, activity, and JNI bridge that links the validated core.
2. Add Storage Access Framework file selection for user-provided boot and SD images.
3. Add APK packaging in GitHub Actions and upload the generated APK as an artifact.
4. Fix actual Android link/runtime errors, then device-test and add touch controls.

Do not report an APK as available until a successful Android build produces a real artifact. Do not report runtime compatibility until the APK has been tested on a device.
