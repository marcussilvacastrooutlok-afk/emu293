# Android port status

This directory tracks the Android port separately from the desktop build.

## Current status

The Linux source build is verified by `.github/workflows/linux-build.yml`. An Android APK has **not** been produced yet.

The existing desktop executable cannot be built as-is for Android because its startup code depends on wxWidgets (`wxApp`, `wxMessageBox`, and `wxSingleChoiceDialog`) and its camera implementation depends on Linux V4L2/libv4l2. The desktop Makefile also invokes `wx-config` and links `-lv4l2 -lv4lconvert` unconditionally.

## Findings from source inspection

- `src/emu293.cpp` currently mixes command-line parsing, the wxWidgets platform picker, ROM/NOR/SD-card path resolution, core initialization, and the CPU execution loop in one file.
- The platform picker assumes the ROMs directory is available through relative filesystem paths. Android must instead resolve user-selected documents and keep save states in app-private storage.
- `src/video/csi.cpp` calls the webcam API even if the user is not actively using a camera. The Android target must compile `android/webcam_stub.cpp` rather than the Linux V4L2 implementation until a proper Android camera backend exists.
- SDL2 rendering/audio and the initial SDL game-controller mapping are present, but have only been validated by the Linux build. Android touch/controller behavior is not yet device-tested.

## Android port requirements

1. Use SDL2's Android project/activity and Android NDK/CMake; do not use the desktop Makefile.
2. Extract boot configuration and the emulator run loop from `src/emu293.cpp` into reusable core code, leaving the wxWidgets picker in a desktop-only frontend.
3. Replace the wxWidgets platform picker with an Android document picker or a small Android UI. Use Android's Storage Access Framework so users can select their own SD-card image and boot image without broad storage permissions.
4. Copy selected document content into app-private storage when necessary, because the emulator currently expects ordinary filesystem paths. Store save states in a writable app-private directory.
5. Exclude the Linux V4L2 webcam implementation on Android initially. Keep CSI camera emulation disabled/stubbed until an Android Camera2/NDK integration exists.
6. Add touch controls and SDL game-controller support without removing the existing keyboard mapping.
7. Build an ARM64 APK in GitHub Actions, upload it as an Actions artifact, and only then report an APK as available. A successful compile is not the same as a device runtime test.

## Immediate implementation order

- **Step 1 — boot configuration (partial):** `src/boot_config.h` now defines platform-neutral boot settings, and `src/emu293.cpp` uses them. This also initializes the NOR/iGame flags to `false`. The emulator execution loop is still coupled to the desktop startup file, so the reusable entry point is not complete yet.
- **Step 1b — extract the core entry point:** move core initialization, execution loop, and shutdown into a reusable function that accepts `BootConfig`. Keep argument parsing and the wxWidgets picker in a desktop-only frontend. Do not begin APK packaging until this compiles on Linux.
- **Step 2 — add Android startup:** connect SDL's Android activity to the reusable entry point and provide a file-selection flow for the boot image and SD-card image.
- **Step 3 — add the NDK build:** compile the core with CMake, SDL2 Android, and `android/webcam_stub.cpp`; exclude wxWidgets and V4L2.
- **Step 4 — validate:** run CI, fix compile/link errors, produce an ARM64 APK artifact, then test install/launch and controls on an Android device.

The repository currently disables GitHub Issues, so the porting checklist is kept here rather than in a new issue.

## Required user files

The app must not bundle copyrighted ROM/system images. Users supply the required SD-card image and ELF/NOR boot image for a supported platform themselves. Refer to the root README for the expected filenames and platform-specific details.
