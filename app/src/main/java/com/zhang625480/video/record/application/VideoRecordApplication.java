package com.zhang625480.video.record.application;

import android.app.Application;
import android.util.DisplayMetrics;

/**
 * Created by zhangzhen on 2018/5/22.
 */

public class VideoRecordApplication extends Application{

    public int screenWidth, screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        initScreen();
    }

    private void initScreen() {
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }


}
