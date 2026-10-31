#pragma once
#include "boot_config.h"

namespace Emu293 {
// Runs the emulation session using paths and boot flags supplied by a frontend.
// Returns zero after a normal shutdown, nonzero when startup fails.
int RunEmulator(const BootConfig &config);
} // namespace Emu293
