package org.emu293.android;

import android.os.Bundle;
import android.view.ViewGroup;

import org.libsdl.app.SDLActivity;

public final class Emu293SDLActivity extends SDLActivity {
    static String bootPath;
    static String sdPath;

    @Override protected String[] getLibraries() {
        // SDL is linked statically into libmain.so.
        return new String[] { "main" };
    }

    @Override protected String[] getArguments() {
        return new String[] {
            "--boot-image", bootPath,
            "--sd-card-image", sdPath,
            "--save-dir", getFilesDir().getAbsolutePath()
        };
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().addContentView(
            new VirtualPadView(this),
            new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        );
    }

    // Called by the transparent Android touch overlay. The native side puts
    // matching SDL keyboard events into the same input path as physical pads.
    static native void nativeSendKey(int sdlScancode, boolean down);
}
