cmake_minimum_required(VERSION 3.22)
project(emu293_android LANGUAGES C CXX)

# Initial NDK build scaffold. SDL2 is expected to be supplied by the SDL Android
# project or an Android-compatible SDL2 CMake package; do not use desktop wxWidgets
# or Linux V4L2 dependencies in this target.
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

set(EMU293_ROOT "${CMAKE_CURRENT_LIST_DIR}/..")

set(EMU293_CORE_SOURCES
    "${EMU293_ROOT}/src/emulator_core.cpp"
    "${EMU293_ROOT}/src/helper.cpp"
    "${EMU293_ROOT}/src/loadelf.cpp"
    "${EMU293_ROOT}/src/system-d99plus.cpp"
    "${EMU293_ROOT}/src/cpu/cpu.cpp"
    "${EMU293_ROOT}/src/dma/apbdma.cpp"
    "${EMU293_ROOT}/src/dma/blndma.cpp"
    "${EMU293_ROOT}/src/stor/sdcard.cpp"
    "${EMU293_ROOT}/src/stor/sdperiph.cpp"
    "${EMU293_ROOT}/src/sys/bufctl.cpp"
    "${EMU293_ROOT}/src/sys/gpio.cpp"
    "${EMU293_ROOT}/src/sys/i2c.cpp"
    "${EMU293_ROOT}/src/sys/irq.cpp"
    "${EMU293_ROOT}/src/sys/miu.cpp"
    "${EMU293_ROOT}/src/sys/timer.cpp"
    "${EMU293_ROOT}/src/video/csi.cpp"
    "${EMU293_ROOT}/src/video/ppu.cpp"
    "${EMU293_ROOT}/src/video/tve.cpp"
    "${EMU293_ROOT}/android/webcam_stub.cpp"
    "${EMU293_ROOT}/src/io/igame_rf.cpp"
    "${EMU293_ROOT}/src/io/ir_gamepad.cpp"
    "${EMU293_ROOT}/src/audio/okiadpcm.cpp"
    "${EMU293_ROOT}/src/audio/spu.cpp"
)

add_library(emu293_core STATIC ${EMU293_CORE_SOURCES})
target_include_directories(emu293_core PUBLIC "${EMU293_ROOT}/src")
target_compile_options(emu293_core PRIVATE -Wall -Wextra)

find_package(SDL2 CONFIG REQUIRED)
target_link_libraries(emu293_core PUBLIC SDL2::SDL2 z)

# The Android app must provide its own SDL entry point/JNI bridge and call
# Emu293::RunEmulator with paths copied into app-private storage.
