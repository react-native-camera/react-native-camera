package org.reactnative.camera.utils;

import android.content.Context;
import android.net.Uri;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jgfidelis on 23/01/18.
 */

public class RNFileUtils {

    public static File ensureDirExists(File dir) throws IOException {
        if (!(dir.isDirectory() || dir.mkdirs())) {
            throw new IOException("Couldn't create directory '" + dir + "'");
        }
        return dir;
    }

    public static String getOutputFilePath(File directory, String extension) throws IOException {
        ensureDirExists(directory);
        String filename = UUID.randomUUID().toString();
        return directory + File.separator + filename + extension;
    }

    public static Uri uriFromFile(File file) {
        return Uri.fromFile(file);
    }

}
