package org.emu293.android;

import org.libsdl.app.SDLActivity;

public final class Emu293SDLActivity extends SDLActivity {
    static String bootPath;
    static String sdPath;

    @Override protected String[] getLibraries() {\n        // SDL is linked statically into libmain.so.\n        return new String[] { "main" };\n    }\n\n    @Override protected String[] getArguments() {
        return new String[] {
            "--boot-image", bootPath,
            "--sd-card-image", sdPath,
            "--save-dir", getFilesDir().getAbsolutePath()
        };
    }
}
