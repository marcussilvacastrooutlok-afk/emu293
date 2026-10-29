cmake_minimum_required(VERSION 3.22)
project(emu293_android LANGUAGES C CXX)

# Build SDL2 from its official source tree in CI. This keeps the Android core
# independent from desktop wxWidgets and Linux V4L2 dependencies.
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS ON)

set(EMU293_ROOT "${CMAKE_CURRENT_LIST_DIR}/..")
set(SDL2_SOURCE_DIR "" CACHE PATH "Path to an SDL2 source checkout")

if(NOT SDL2_SOURCE_DIR OR NOT EXISTS "${SDL2_SOURCE_DIR}/CMakeLists.txt")
    message(FATAL_ERROR "Set SDL2_SOURCE_DIR to an SDL2 source checkout")
endif()

set(SDL_TEST OFF CACHE BOOL "" FORCE)
set(SDL_TESTS OFF CACHE BOOL "" FORCE)
set(SDL_SHARED OFF CACHE BOOL "" FORCE)
set(SDL_STATIC ON CACHE BOOL "" FORCE)
set(SDL_INSTALL OFF CACHE BOOL "" FORCE)
add_subdirectory("${SDL2_SOURCE_DIR}" "${CMAKE_BINARY_DIR}/SDL2" EXCLUDE_FROM_ALL)

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

if(TARGET SDL2::SDL2)
    target_link_libraries(emu293_core PUBLIC SDL2::SDL2 z)
elseif(TARGET SDL2-static)
    target_link_libraries(emu293_core PUBLIC SDL2-static z)
else()
    message(FATAL_ERROR "SDL2 source did not define a recognized SDL2 library target")
endif()

# SDLActivity loads libmain.so and calls SDL_main. This shared library is the
# native application entry point; Java Activity and APK packaging are still pending.
add_library(main SHARED "${CMAKE_CURRENT_LIST_DIR}/android_main.cpp")
target_link_libraries(main PRIVATE emu293_core)
set_target_properties(main PROPERTIES OUTPUT_NAME main)
