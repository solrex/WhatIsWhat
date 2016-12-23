package com.ooolab.whatiswhat;

import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by solrex on 2016/12/22.
 */

public class Utils {
    public static String TAG = "Utils";

    public static void copyAssetsToSdcard(AssetManager assets,String assetName, File dstFile) {
        copyAssetsToSdcard(assets, assetName, dstFile, false);
    }

    public static void copyAssetsToSdcard(AssetManager assets,String assetName, File dstFile, boolean overwrite) {
        InputStream is = null;
        FileOutputStream out = null;
        if (dstFile.exists() && (!overwrite)) {
            return;
        }
        try {
            is = assets.open(assetName);
            out = new FileOutputStream(dstFile);
            byte[] buffer = new byte[1024];
            int size = 0;
            while ((size = is.read(buffer, 0, 1024)) >= 0) {
                out.write(buffer, 0, size);
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG, "copyAssetsToSdcard: ", e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.w(TAG, "copyAssetsToSdcard: ", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.w(TAG, "copyAssetsToSdcard: ", e);
                }
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                Log.w(TAG, "copyAssetsToSdcard: ", e);
            }
        }
    }
}
