package org.nx2a;

import android.app.AlertDialog;
import android.app.NativeActivity;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Optional Java text entry; the ordinary NativeActivity APK remains code-less. */
public class GameActivity extends NativeActivity {
    private final GameKeyboard keyboard = new GameKeyboard(this);

    private static String text(byte[] value) {
        return value == null ? "" : new String(value, StandardCharsets.UTF_8);
    }

    // Guest callers wait off the UI thread. Ordinary UTF-8, not JNI modified UTF-8.
    public byte[] readKeyboard(byte[] title, byte[] subtitle, byte[] guide, byte[] initial, int min, int max) {
        return keyboard.read(text(title), text(subtitle), text(guide), text(initial), min, max);
    }


    /** Pick one of the Vulkan drivers bundled in this game's APK. The host reads
     * env.txt only when it starts, so the selected driver takes effect next launch. */
    public void openDriverMenu() {
        runOnUiThread(this::showDriverMenu);
    }

    private void showDriverMenu() {
        File files = getExternalFilesDir(null);
        File env = files == null ? null : new File(files, "env.txt");
        File libDir = new File(getApplicationInfo().nativeLibraryDir);
        if (env == null || !env.isFile()) {
            Toast.makeText(this, "Game settings are not available", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("System default");
        File[] libs = libDir.listFiles();
        if (libs != null) {
            for (File lib : libs) {
                String name = lib.getName();
                if (name.startsWith("libvulkan") && name.endsWith(".so") && lib.isFile()) choices.add(name);
            }
        }
        if (choices.size() == 1) {
            Toast.makeText(this, "No custom Vulkan drivers are bundled with this game", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(this)
            .setTitle("Vulkan driver")
            .setItems(choices.toArray(new String[0]), (dialog, which) -> {
                String selected = which == 0 ? "" : choices.get(which);
                try {
                    List<String> lines = new ArrayList<>();
                    for (String line : java.nio.file.Files.readAllLines(env.toPath(), StandardCharsets.UTF_8)) {
                        if (!line.trim().startsWith("DM_VK_DRIVER=")) lines.add(line);
                    }
                    if (!selected.isEmpty()) lines.add("DM_VK_DRIVER=" + selected);
                    File temp = new File(files, "env.txt.new");
                    try (FileOutputStream out = new FileOutputStream(temp)) {
                        out.write((String.join("\n", lines) + "\n").getBytes(StandardCharsets.UTF_8));
                        out.getFD().sync();
                    }
                    if (!temp.renameTo(env)) {
                        temp.delete();
                        throw new java.io.IOException("could not replace env.txt");
                    }
                    Toast.makeText(this, "Driver saved. Restart the game to apply it.", Toast.LENGTH_LONG).show();
                } catch (Exception error) {
                    Toast.makeText(this, "Could not save driver: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override protected void onResume() { super.onResume(); keyboard.pause(false); }
    @Override protected void onPause() { keyboard.pause(true); super.onPause(); }
    @Override protected void onDestroy() { keyboard.pause(true); super.onDestroy(); }
}
