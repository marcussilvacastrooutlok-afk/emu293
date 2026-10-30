#include "emulator_core.h"

#include <cstdio>
#include <jni.h>
#include <SDL2/SDL.h>
#include <string>

extern "C" int SDL_main(int argc, char *argv[]) {
    Emu293::BootConfig config;
    bool have_boot_image = false;
    bool have_sd_image = false;

    for (int i = 1; i < argc; ++i) {
        const std::string arg(argv[i] ? argv[i] : "");
        auto next_value = [&](const char *option) -> const char * {
            if (i + 1 >= argc || argv[i + 1] == nullptr) {
                std::fprintf(stderr, "Missing value for %s\n", option);
                return nullptr;
            }
            return argv[++i];
        };

        if (arg == "--boot-image") {
            const char *value = next_value("--boot-image");
            if (!value) return 2;
            config.boot_image = value;
            have_boot_image = true;
        } else if (arg == "--sd-card-image") {
            const char *value = next_value("--sd-card-image");
            if (!value) return 2;
            config.sd_card_image = value;
            have_sd_image = true;
        } else if (arg == "--save-dir") {
            const char *value = next_value("--save-dir");
            if (!value) return 2;
            config.save_directory = value;
        } else if (arg == "--nor") {
            config.boot_from_nor = true;
        } else if (arg == "--igame") {
            config.boot_igame = true;
        } else {
            std::fprintf(stderr, "Unknown Emu293 argument: %s\n", arg.c_str());
            return 2;
        }
    }

    if (!have_boot_image || !have_sd_image) {
        std::fprintf(stderr, "Emu293 requires --boot-image and --sd-card-image.\n");
        return 2;
    }

    return Emu293::RunEmulator(config);
}

extern "C" JNIEXPORT void JNICALL
Java_org_emu293_android_Emu293SDLActivity_nativeSendKey(
    JNIEnv *, jclass, jint scancode, jboolean down) {
    SDL_Event event;
    SDL_zero(event);
    event.type = down ? SDL_KEYDOWN : SDL_KEYUP;
    event.key.type = event.type;
    event.key.state = down ? SDL_PRESSED : SDL_RELEASED;
    event.key.repeat = 0;
    event.key.keysym.scancode = static_cast<SDL_Scancode>(scancode);
    event.key.keysym.sym = SDL_GetKeyFromScancode(
        static_cast<SDL_Scancode>(scancode));
    SDL_PushEvent(&event);
}
