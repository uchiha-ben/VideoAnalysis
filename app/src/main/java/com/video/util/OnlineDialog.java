package com.video.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import java.util.logging.Handler;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OnlineDialog {
    public static final OkHttpClient okHttpClient = new OkHttpClient();

    public static void init(final Context context, final String gtUrl) {
        try {
            final AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... strings) {
                    try {
                        Request request = new Request.Builder().url(gtUrl).addHeader("User-Agent",
                                "Mozilla/5.0 (iPhone; CPU iPhone OS 12_1_4 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/16D57 Version/12.0 Safari/604.1").build();
                        Response response = okHttpClient.newCall(request).execute();
                        String msg = response.body().string();
                        return msg;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(final String result) {
                    if (result != null && !result.isEmpty()) {
                        if (!VideoDownload.getInstance().getUpdate(context).equals(result)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("提醒")
                                    .setMessage(result).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            Pattern pattern = Patterns.WEB_URL;
                                            Matcher matcher = pattern.matcher(result);
                                            if (matcher.find()) {
                                                final String downloadUrl = matcher.group(0);
                                                Log.e("TAG", downloadUrl);
                                                ClipBoardUtil.copy(context, downloadUrl);
                                                Toast.makeText(context, "新版链接已保存至剪切板", Toast.LENGTH_SHORT).show();
                                                Uri uri = Uri.parse(downloadUrl);
                                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                                context.startActivity(intent);
                                            }
                                        }
                                    }).setNeutralButton("不再提醒", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            VideoDownload.getInstance().setUpdate(context, result);
                                        }
                                    }).setNegativeButton("取消", null);
                            builder.create().show();
                        }
                    }
                }
            };
            asyncTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //        new Thread() {
//            @Override
//            public void run() {
//                try {
//                    Request request = new Request.Builder().url(wyUrl).build();
//                    Response response = okHttpClient.newCall(request).execute();
//                    String msg = response.body().string();
////                    if (msg!=null && !msg.isEmpty())
//                    AlertDialog.Builder builder = new AlertDialog.Builder(context).setTitle("最普通dialog")
//                            .setMessage("提示").setPositiveButton("更新", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//
//                                }
//                            }).setNegativeButton("取消", null);
//                    builder.create().show();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();

    }
}
