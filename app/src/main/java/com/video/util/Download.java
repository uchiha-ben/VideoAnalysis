package com.video.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Download {
    private static final Handler handler = new Handler();
    public static final String FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public void onStartDownload(final Context context) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "准备下载", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onDownloading(final Context context, final int progress) {

    }

    public void onErrorDownload(final Context context) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "下载失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onFinishDownload(final Context context, final String path) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "下载成功!文件保存目录:" + path, Toast.LENGTH_SHORT).show();
                File file = new File(path);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + file)));
            }
        });
    }

    private String getFileName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public void downloadMp4(final Context context, String url) throws IOException {
        Request request = new Request.Builder().url(url).addHeader("Connection", "keep-alive").addHeader("User-Agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1").build();
        Response response = okHttpClient.newCall(request).execute();
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        String FILE_NAME =getFileName();
        File file = new File(FILE_PATH + "/" + FILE_NAME + ".mp4");
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            is = response.body().byteStream();
            long total = response.body().contentLength();
            fos = new FileOutputStream(file);
            long sum = 0;
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
                sum += len;
                int progress = (int) (sum * 1.0f / total * 100);
                onDownloading(context, progress);
            }
            fos.flush();
            Log.e("TAG", "finish");
            onFinishDownload(context, file.getAbsolutePath());
        } catch (Exception e) {
            onErrorDownload(context);
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
