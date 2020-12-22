package com.video.analysis;


import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.video.util.ClipBoardUtil;
import com.video.util.VideoDownload;


public class EmptyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置透明沉浸状态栏
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE); //使背景图与状态栏融合到一起，这里需要在setcontentview前执行
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);
        //设置1像素
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        params.x = 0;
        params.y = 0;
        params.height = 1;
        params.width = 1;
        window.setAttributes(params);
        getClipboardData();
    }

    //获取剪切板内容
    private void getClipboardData() {
        this.getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                //把获取到的内容打印出来
                String url = ClipBoardUtil.paste(getApplicationContext());
                VideoDownload.getInstance().parse(getApplicationContext(), url);
                ClipBoardUtil.clear(getApplicationContext());
                finish();
            }
        });
    }
}
