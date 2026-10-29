package org.emu293.android;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public final class MainActivity extends Activity {
    private static final int PICK_BOOT = 1;
    private static final int PICK_SD = 2;
    private String bootPath;
    private String sdPath;
    private TextView status;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(28, 36, 28, 28);
        status = new TextView(this);
        status.setText("Selecione as imagens do sistema que você possui.");
        layout.addView(status, new LinearLayout.LayoutParams(-1, -2));

        Button boot = new Button(this);
        boot.setText("Selecionar imagem de boot");
        boot.setOnClickListener(v -> pick(PICK_BOOT));
        layout.addView(boot, new LinearLayout.LayoutParams(-1, -2));

        Button sd = new Button(this);
        sd.setText("Selecionar imagem do cartão SD");
        sd.setOnClickListener(v -> pick(PICK_SD));
        layout.addView(sd, new LinearLayout.LayoutParams(-1, -2));

        Button start = new Button(this);
        start.setText("Iniciar Emu293");
        start.setOnClickListener(v -> startEmulator());
        layout.addView(start, new LinearLayout.LayoutParams(-1, -2));
        setContentView(layout);
    }

    private void pick(int request) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, request);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (result != RESULT_OK || data == null || data.getData() == null) return;
        try {
            String name = request == PICK_BOOT ? "boot-image.bin" : "sd-card-image.bin";
            File target = new File(getFilesDir(), name);
            try (InputStream in = getContentResolver().openInputStream(data.getData());
                 OutputStream out = new FileOutputStream(target)) {
                if (in == null) throw new IllegalStateException("Não foi possível abrir o arquivo selecionado.");
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            }
            if (request == PICK_BOOT) bootPath = target.getAbsolutePath();
            if (request == PICK_SD) sdPath = target.getAbsolutePath();
            status.setText("Boot: " + (bootPath == null ? "não selecionado" : "selecionado") +
                    "\nCartão SD: " + (sdPath == null ? "não selecionado" : "selecionado"));
        } catch (Exception e) {
            Toast.makeText(this, "Falha ao copiar arquivo: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void startEmulator() {
        if (bootPath == null || sdPath == null) {
            Toast.makeText(this, "Selecione as duas imagens antes de iniciar.", Toast.LENGTH_LONG).show();
            return;
        }
        Emu293SDLActivity.bootPath = bootPath;
        Emu293SDLActivity.sdPath = sdPath;
        startActivity(new Intent(this, Emu293SDLActivity.class));
    }
}
