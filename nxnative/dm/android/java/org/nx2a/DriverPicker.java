package org.nx2a;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** The driver's library is loaded at Vulkan initialization; selections apply on the next game launch. */
final class DriverPicker {
    private DriverPicker() {}

    static void show(Activity activity) {
        File files = activity.getExternalFilesDir(null);
        File env = files == null ? null : new File(files, "env.txt");
        File libDir = new File(activity.getApplicationInfo().nativeLibraryDir);
        if (env == null || !env.isFile()) {
            Toast.makeText(activity, "Game settings are not available", Toast.LENGTH_LONG).show();
            return;
        }

        List<String> choices = new ArrayList<>();
        choices.add("System default");
        File[] libs = libDir.listFiles();
        if (libs != null) {
            for (File lib : libs) {
                String name = lib.getName();
                if (name.endsWith(".so") && lib.isFile()
                    && !name.equals("libdmhost.so") && !name.equals("libadrenotools.so")
                    && !name.equals("libhook_impl.so") && !name.equals("libmain_hook.so")
                    && !name.equals("libfile_redirect_hook.so") && !name.equals("libgsl_alloc_hook.so")) choices.add(name);
            }
        }
        if (choices.size() == 1) {
            Toast.makeText(activity, "No custom Vulkan drivers are bundled with this game", Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(activity)
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
                    Toast.makeText(activity, "Driver saved. Restart the game to apply it.", Toast.LENGTH_LONG).show();
                } catch (Exception error) {
                    Toast.makeText(activity, "Could not save driver: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
