#pragma once

#include <string>

namespace Emu293 {

// Platform-neutral startup settings shared by desktop and Android frontends.
struct BootConfig {
    std::string webcam_device;
    std::string boot_image;
    std::string sd_card_image;
    std::string save_directory = "../roms";
    bool boot_from_nor = false;
    bool boot_igame = false;
};

} // namespace Emu293
