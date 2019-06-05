package org.xiaobai.trajectory.app;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

//app入口
public class MyApp extends Application {
    private static MyApp sInstance;
    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        sContext = getApplicationContext();
       //初始化LitePal 数据库
        LitePal.initialize(this);
    }

    //获取实例
    public synchronized static MyApp getInstance() {
        if (sInstance == null) {
            throw new RuntimeException("App is null or dead.");
        }
        return sInstance;
    }

    //获取Context
    public static Context getContext() {
        return sContext;
    }
}
