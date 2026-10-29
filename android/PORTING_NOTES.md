# Android port status

This directory tracks the Android port separately from the desktop build.

## Current status

The Linux source build is verified by `.github/workflows/linux-build.yml`. An Android APK has **not** been produced yet.

The existing desktop executable cannot be built as-is for Android because its startup code depends on wxWidgets (`wxApp`, `wxMessageBox`, and `wxSingleChoiceDialog`) and its camera implementation depends on Linux V4L2/libv4l2. The desktop Makefile also invokes `wx-config` and links `-lv4l2 -lv4lconvert` unconditionally.

## Android port requirements

1. Use SDL2's Android project/activity and Android NDK/CMake; do not use the desktop Makefile.
2. Keep the emulator core (CPU, DMA, storage, system, PPU, SPU and input protocol) separate from desktop startup/UI.
3. Replace the wxWidgets platform picker with an Android document picker or a small Android UI. Use Android's Storage Access Framework so users can select their own SD-card image and boot image without broad storage permissions.
4. Convert selected document URIs to accessible app-private files (or use SDL/Android file APIs carefully); the emulator currently expects filesystem paths and save-state paths.
5. Exclude the Linux V4L2 webcam implementation on Android initially. Keep CSI camera emulation disabled/stubbed until an Android Camera2/NDK integration exists.
6. Add touch controls and SDL game-controller support without removing the existing keyboard mapping.
7. Build an ARM64 APK in GitHub Actions, upload it as an Actions artifact, and only then report an APK as available. A successful compile is not the same as a device runtime test.

## Immediate implementation order

- Add an Android NDK build target and an SDL Android application shell.
- Split/guard desktop-only wxWidgets startup from the emulator core.
- Add a startup configuration path for selected boot image and SD-card image.
- Add Android input and save-state paths.
- Run CI and fix compiler/linker errors before distributing an APK.

## Required user files

The app must not bundle copyrighted ROM/system images. Users supply the required SD-card image and ELF/NOR boot image for a supported platform themselves. Refer to the root README for the expected filenames and platform-specific details.
