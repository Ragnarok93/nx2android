package org.nx2a;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** Imports the player's own Lossless Scaling DLL into private app storage; the DLL is never bundled. */
final class LosslessPicker {
    private static final int REQUEST = 0x4c53;
    private LosslessPicker() {}

    static void show(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_TITLE, "Select your Lossless.dll");
        activity.startActivityForResult(intent, REQUEST);
    }

    static boolean onResult(Activity activity, int request, int result, Intent data) {
        if (request != REQUEST) return false;
        if (result != Activity.RESULT_OK || data == null || data.getData() == null) {
            Toast.makeText(activity, "Lossless.dll selection cancelled", Toast.LENGTH_SHORT).show();
            return true;
        }
        File target = new File(activity.getFilesDir(), "Lossless.dll");
        File staged = new File(activity.getFilesDir(), "Lossless.dll.new");
        try {
            Uri uri = data.getData();
            try (InputStream in = activity.getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(staged)) {
                if (in == null) throw new java.io.IOException("Could not open the selected file");
                byte[] buffer = new byte[64 * 1024];
                int n;
                while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
                out.getFD().sync();
            }
            if (staged.length() < 2) throw new java.io.IOException("The selected file is empty");
            byte[] magic = new byte[2];
            try (java.io.FileInputStream in = new java.io.FileInputStream(staged)) {
                if (in.read(magic) != 2 || magic[0] != 'M' || magic[1] != 'Z')
                    throw new java.io.IOException("The selected file is not a Windows DLL (PE)");
            }
            if (!staged.renameTo(target)) throw new java.io.IOException("Could not save Lossless.dll");
            File external = activity.getExternalFilesDir(null);
            if (external == null) throw new java.io.IOException("Game settings folder is unavailable");
            File env = new File(external, "env.txt"), temp = new File(external, "env.txt.new");
            List<String> lines = env.isFile() ? Files.readAllLines(env.toPath(), StandardCharsets.UTF_8) : new ArrayList<>();
            List<String> updated = new ArrayList<>();
            for (String line : lines) if (!line.trim().startsWith("DM_LSFG_DLL=")) updated.add(line);
            updated.add("DM_LSFG_DLL=" + target.getAbsolutePath());
            Files.write(temp.toPath(), updated, StandardCharsets.UTF_8);
            try {
                Files.move(temp.toPath(), env.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException unsupported) {
                Files.move(temp.toPath(), env.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            Toast.makeText(activity, "Lossless.dll imported. Restart the game to load it.", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            staged.delete();
            Toast.makeText(activity, "Could not import Lossless.dll: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
