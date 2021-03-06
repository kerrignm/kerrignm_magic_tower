package com.game.magictower.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.content.Context;

public class FileUtil {
    
    private static final String TAG = "MagicTower:FileUtil";
    
    private static final int BUF_SIZE = 8 * 1024;
    
    private static final byte[] buffer = new byte[BUF_SIZE];
    
    public static String readInternal(Context context, String fileName) {
        try {
            FileInputStream fis = context.openFileInput(fileName);
            int hasRead;
            StringBuilder sb = new StringBuilder();
            while ((hasRead = fis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, hasRead));
            }
            fis.close();
            return sb.toString();
        } catch (Exception e) {
            LogUtil.e(TAG, "readInternal() Exception fileName = " + fileName);
        } 
        return null;
    }
    
    public static String readExternal(Context context, String fileName) {
        fileName = context.getExternalFilesDir(null).getPath() + File.separator + fileName;
        try {
            FileInputStream fis = new FileInputStream(fileName);
            int hasRead;
            StringBuilder sb = new StringBuilder();
            while ((hasRead = fis.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, hasRead));
            }
            fis.close();
            return sb.toString();
        } catch (Exception e) {
            LogUtil.e(TAG, "readExternal() Exception fileName = " + fileName);
        } 
        return null;
    }
    
    public static boolean writeInternal(Context context, String fileName, String content) {
        if(content == null) return false;
        try {
            FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(content.getBytes());
            fos.close();
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "writeInternal() Exception fileName = " + fileName);
        }
        return false;
    }
    
    public static boolean writeExternal(Context context, String fileName, String content) {
        return writeExternal(context, fileName, content, false);
    }
    
    public static boolean writeExternal(Context context, String fileName, String content, boolean append) {
        if(content == null) return false;
        File file = new File(context.getExternalFilesDir(null).getPath() + File.separator + fileName);
        try {
            if (append) {
                RandomAccessFile raf = new RandomAccessFile(file, "rw");
                raf.seek(file.length());
                raf.write(content.getBytes());
                raf.close();
            } else {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(content.getBytes());
                fos.close();
            }
            return true;
        } catch (Exception e) {
            LogUtil.e(TAG, "writeExternal() Exception fileName = " + fileName);
        }
        return false;
    }

    public static String loadAssets(Context context, String name) {
        InputStream is = null;
        StringBuilder sb = null;
        int byteCount;
        try {
            is = context.getAssets().open(name);
            sb = new StringBuilder();
            while ((byteCount = is.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, byteCount));
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "loadAssets() IOException name = " + name);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.e(TAG, "loadAssets() finally IOException name = " + name);
                }
            }
        }
        if (sb != null) {
            return sb.toString();
        }
        return null;
    }
}
