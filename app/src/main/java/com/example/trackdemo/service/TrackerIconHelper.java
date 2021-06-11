package com.example.trackdemo.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrackerIconHelper {

    private final static String TAG = "ZJLog_iconH";

    /**
     * 截图保存到本地
     *
     * @param fragment
     * @return 保存成功返回 Uri, 失败返回 null
     */
    public static Uri saveSnapShot(Fragment fragment) {
        final View view = fragment.getView();
        Bitmap bitmap = generateViewSnapshot(view);
        //将bitMap保存到相册中
        return saveBitmap(bitmap, view.getContext());
    }

    /**
     * 对View控件里面的内容进行截图
     */
    private static Bitmap generateViewSnapshot(View view) {
        //使控件可以进行缓存
        view.setDrawingCacheEnabled(true);
        //获取缓存的 Bitmap
        Bitmap drawingCache = view.getDrawingCache();
        //复制获取的 Bitmap
        drawingCache = Bitmap.createBitmap(drawingCache);
        //关闭视图的缓存
        view.setDrawingCacheEnabled(false);

        return drawingCache;
    }


    //保存BitMap图片到本地文件
    public static Uri saveBitmap(Bitmap bitmap, Context context) {
        //获取需要存储到本地的路径
        File file = new File(context.getExternalFilesDir(null), "/train/");
        //如果文件夹不存在 就创建文件夹
        if (!file.exists()) {
            file.mkdirs();
        }
        File path = new File(file, System.currentTimeMillis() + ".png");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(path);
            // compress - 压缩的意思  将bitmap保存到本地文件中
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutputStream);
            //存储完成后需要清除相关的进程
            fileOutputStream.flush();
            fileOutputStream.close();
            bitmap.recycle();
            //保存图片后发送广播通知更新数据库
            return Uri.fromFile(path);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "saveBitmap: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "error: " + e.toString());
        }
        Log.i(TAG, "path = " + path.getPath());
        return null;
    }

}
