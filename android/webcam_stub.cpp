// Android build fallback for the optional Subor A21 webcam path.
// The desktop implementation uses Linux V4L2/libv4l2, which is not available
// through the Android NDK. Camera support is intentionally disabled until it
// is integrated with Android Camera2/NDK APIs.
#include "../src/video/webcam.h"

namespace Emu293 {
void webcam_init(const std::string &) {
}

bool webcam_grab_frame_rgb565(uint8_t *, int, int) {
    return false;
}

void webcam_stop() {
}
}
