package com.game.magictower.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import com.game.magictower.res.TowerDimen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public final class BitmapUtil {
    
    private static final String TAG = "MagicTower:BitmapUtil";

    public static Bitmap loadBitmap(Context context, String assets) {
        Bitmap instance = null;
        AssetManager am = context.getAssets();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(am.open("image/" + assets));
            instance = scaleBitmap(bitmap, TowerDimen.TOWER_SCALE, TowerDimen.TOWER_SCALE);
        } catch (IOException e) {
            LogUtil.e(TAG, "loadBitmap() IO error assets = " + assets);
        }
        return instance;
    }
    
    public static Bitmap loadBitmap(Context context, String assets,
                                    int width, int height) {
        Bitmap instance = null;
        AssetManager am = context.getAssets();
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(am.open("image/" + assets));
            if (width != 0 && height != 0) {
                instance = scaleBitmap(bitmap, width, height);
            }
        } catch (IOException e) {
            LogUtil.e(TAG, "loadBitmap() 2 IO error assets = " + assets);
        }
        return instance;
    }
    
    public static Bitmap creatBitmap(Bitmap bitmap, float scaleX, float scaleY) {
        bitmap = scaleBitmap(bitmap, TowerDimen.TOWER_SCALE, TowerDimen.TOWER_SCALE);
        return bitmap;
    }
    
    public static Bitmap creatBitmap(int width, int height, float[] pointArray) {
        Path path = new Path();
        for (int i = 0; i < pointArray.length / 2; i++) {
            if (i == 0) {
                path.moveTo(width * pointArray[i * 2], height * pointArray[i * 2 + 1]);
            } else {
                path.lineTo(width * pointArray[i * 2], height * pointArray[i * 2 + 1]);
            }
        }
        path.close();
        return creatBitmap(width, height, path);
    }
    
    public static Bitmap scaleBitmap(Bitmap bitmap, int width, int height) {
        if (width <= 0 || height <= 0 ) {
            throw new IllegalArgumentException("target size must be positive!");
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
        bitmap.recycle();
        return newbmp;
    }
    
    public static Bitmap scaleBitmap(Bitmap bitmap, float scalew,
                                     float scaleh) {
        if (scalew <=0 || scaleh <= 0 ) {
            throw new IllegalArgumentException("scale rate must be positive!");
        }
        if (MathUtil.equals(scalew, 1.0f) && MathUtil.equals(scaleh, 1.0f)) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scalew, scaleh);
        Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return newbmp;
    }
    
    public static Bitmap creatBitmap(int width, int height, Path path) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(0xaaffffff);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawColor(0);
        canvas.drawPath(path, paint);
        return bitmap;
    }
    
    public static void saveBitmapToFile(String path, Bitmap bitmap) {
        saveBitmapToFile(path, bitmap, CompressFormat.PNG, 100);
    }

    public static void saveBitmapToFile(String path, Bitmap bitmap,
            CompressFormat format, int quality) {
        File dir = new File(path).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File photoFile = new File(path);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(photoFile);
            if (bitmap != null) {
                if (bitmap.compress(format, quality, fos)) {
                    fos.flush();
                }
            }
        } catch (FileNotFoundException e) {
            photoFile.delete();
            LogUtil.e(TAG, "saveBitmapToFile() FileNotFoundException path = " + path);
        } catch (IOException e) {
            photoFile.delete();
            LogUtil.e(TAG, "saveBitmapToFile() IOException path = " + path);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                LogUtil.e(TAG, "saveBitmapToFile() finally IOException path = " + path);
            }
        }
    }

}
