package org.nx2a;

import android.app.Activity;
import android.app.AlertDialog;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
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
        try (BufferedReader in = new BufferedReader(new InputStreamReader(activity.getAssets().open("vulkan-drivers.txt"), StandardCharsets.UTF_8))) {
            String name;
            while ((name = in.readLine()) != null) {
                name = name.trim();
                if (name.startsWith("lib") && name.endsWith(".so") && new File(libDir, name).isFile() && !choices.contains(name)) choices.add(name);
            }
        } catch (IOException ignored) {
            // APKs built with a single driver or the system driver have no picker manifest.
        }
        if (choices.size() == 1) {
            Toast.makeText(activity, "No custom Vulkan drivers are bundled with this game", Toast.LENGTH_LONG).show();
            return;
        }
        int checked = 0;
        try {
            for (String line : java.nio.file.Files.readAllLines(env.toPath(), StandardCharsets.UTF_8)) {
                if (line.startsWith("DM_VK_DRIVER=")) {
                    int selected = choices.indexOf(line.substring("DM_VK_DRIVER=".length()).trim());
                    if (selected >= 0) checked = selected;
                }
            }
        } catch (Exception error) {
            Toast.makeText(activity, "Could not read driver setting: " + error.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        new AlertDialog.Builder(activity)
            .setTitle("Vulkan driver")
            .setSingleChoiceItems(choices.toArray(new String[0]), checked, (dialog, which) -> {
                dialog.dismiss();
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
