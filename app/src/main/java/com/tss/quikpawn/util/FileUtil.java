package com.tss.quikpawn.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class FileUtil {

    public static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

}
