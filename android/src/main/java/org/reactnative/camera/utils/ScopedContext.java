package org.reactnative.camera.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by jgfidelis on 23/01/18.
 */

public class ScopedContext {

    private File cacheDirectory = null;
    private File filesDirectory = null;

    public ScopedContext(Context context) {
        createCacheDirectory(context);
        createFilesDirectory(context);
    }

    public void createCacheDirectory(Context context) {
        cacheDirectory = new File(context.getCacheDir() + "/Camera/");
    }

    public void createFilesDirectory(Context context) {
        filesDirectory = new File(context.getFilesDir() + "/Camera/");
    }

    public File getCacheDirectory() {
        return cacheDirectory;
    }

    public File getFilesDirectory() {
        return filesDirectory;
    }
}
