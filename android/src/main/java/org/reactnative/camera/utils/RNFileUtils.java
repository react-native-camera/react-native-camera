package org.reactnative.camera.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;


import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by jgfidelis on 23/01/18.
 */

public class RNFileUtils {

    public static File ensureDirExists(File dir) throws IOException {
        if (!(dir.isDirectory() || dir.mkdirs())) {
            Log.i("Debug","RNFileUtils ensureDirExists error...");
            throw new IOException("Couldn't create directory '" + dir + "'");
        }
        Log.i("Debug","RNFileUtils ensureDirExists create dir success");
        return dir;
    }

    public static String getOutputFilePath(File directory, String extension) throws IOException {
        ensureDirExists(directory);

        String filename = UUID.randomUUID().toString();
//        Log.i("Debug","RNFileUtils getoutputfilepath return="+directory + File.separator + filename + extension);
        return directory + File.separator + filename + extension;
    }
    public static String getOutputFilePathWithFileName(File directory,String fileName, String extension) throws IOException {
        ensureDirExists(directory);
//        Log.i("Debug","RNFileUtils getoutputfilepath return="+directory + File.separator + fileName + extension);
        return directory + File.separator + fileName + extension;
    }

    public static Uri uriFromFile(File file) {
        return Uri.fromFile(file);
    }

}
