#include "emulator_core.h"
#include "cpu/cpu.h"
#include "loadelf.h"
#include "stor/sdcard.h"
#include "sys/timer.h"
#include "video/ppu.h"
#include "video/tve.h"
#include "video/webcam.h"
#include "video/csi.h"
#include "io/ir_gamepad.h"
#include "io/igame_rf.h"
#include "audio/spu.h"
#include "system.h"
#include <SDL2/SDL.h>
#include <algorithm>
#include <chrono>
#include <cstdio>

namespace Emu293 {
namespace {
std::string state_file(const std::string &directory, int slot) {
    return stringf("%s/slot_%d.sav", directory.c_str(), slot);
}
}

int RunEmulator(const BootConfig &config) {
  SDL_Init(SDL_INIT_EVERYTHING);
  uint32_t entryPoint, stackAddr;
  InitPPUThreads();
  SPUInitSound();
  InitCSIThreads();
  if (!SD_InitCard(config.sd_card_image.c_str())) {
    printf("Failed to load SD card image\n");
  }

  if (!config.webcam_device.empty()) {
    webcam_init(config.webcam_device);
  }

  CPU scoreCPU;

  scoreCPU.reset();

  //	scoreCPU.cr29 = 0x20000000;
  auto do_load_image = [&]() {
    if (config.boot_igame) {
      if (!LoadIGameToRAM(config.boot_image.c_str(), entryPoint)) {
        printf("Failed to load iGame binary\n");
        exit(1);
      }
    } else if (config.boot_from_nor) {
      if (!LoadNORToRAM(config.boot_image.c_str(), entryPoint, stackAddr)) {
        printf("Failed to load NOR\n");
        exit(1);
      }
      scoreCPU.r0 = stackAddr;
    } else {
      entryPoint = LoadElfToRAM(config.boot_image.c_str());
      if (entryPoint == 0) {
        printf("Failed to load ELF\n");
        exit(1);
      }
      printf("Loaded ELF to RAM (ep=0x%08x)!\n", entryPoint);
    }
    scoreCPU.pc = entryPoint;
  };
  do_load_image();

  system_init(&scoreCPU);
  if (config.boot_igame) {
    IGameRFInit();
  }
  write_memU32(0xFFFFFFEC, 1);
  int icount = 0;
  auto start = std::chrono::steady_clock::now();
  int64_t t32k_rate = 1000000000/32768;
  int64_t t32k_next = std::chrono::duration_cast<std::chrono::nanoseconds>(start.time_since_epoch()).count()
    + t32k_rate;

  while (1) {
    scoreCPU.step();
    icount++;

    if ((icount % 4) == 0) {
      TimerTick(false); // main PCLK/2
      // SDL_Delay(1);
    }

    if ((icount % 320) == 0) {
      if (config.boot_igame) {
        IGameRFTick();
      } else {
        IRGamepadTick();
      }
    }

    if ((icount % 200) == 0) {
      SPUUpdate();
    }


    if ((icount % 2000) == 1000) {
      PPUTick();

      // SDL_Delay(1);
    }

    if ((icount % 2000) == 1500) {
      TVETick();

      // SDL_Delay(1);
    }

    if ((icount % 100) == 0) {
      fflush(stdout);
      auto t = std::chrono::steady_clock::now();
      int64_t t_ns = std::chrono::duration_cast<std::chrono::nanoseconds>(t.time_since_epoch()).count();
      auto delta = std::chrono::duration<float>(t - start).count();

      if (t_ns >= t32k_next) {
        TimerTick(true); // 32kHz
        t32k_next = std::max(t_ns+t32k_rate/2, t32k_next+t32k_rate);
      }

      if (delta > 1) {
        printf("%.02fMIPS\n", (icount / 1000000.0) /
                                  delta);
        icount = 0;
        start = t;
        printf("PC=0x%08x\n", scoreCPU.pc);
      }
      if (softreset_flag) {
        system_softreset();
        do_load_image();
        softreset_flag = false;
      }
      if (shutdown_flag) {
        break;
      }
      if (savestate_flag != -1 || loadstate_flag != -1) {
        bool is_save = (savestate_flag != -1);
        auto file = state_file(is_save ? savestate_flag : loadstate_flag);
        SaveStater ss; 
        if(is_save ? ss.begin_save(file) : ss.begin_load(file)) {
          ss.i(icount);
          system_state(ss);
          ss.finalise();
          printf("%s state %s slot %d\n", (is_save ? "Saved" : "Loaded"), (is_save ? "to" : "from"),
            is_save ? savestate_flag : loadstate_flag);
        }
        savestate_flag = -1;
        loadstate_flag = -1;
      }
    }
    // SDL_Delay(1);
  }
  ShutdownCSI();
  ShutdownSPU();
  webcam_stop();
  SDL_Quit();

  return 0;
}
} // namespace Emu293
