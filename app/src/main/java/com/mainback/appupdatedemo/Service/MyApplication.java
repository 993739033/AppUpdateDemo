package com.mainback.appupdatedemo.Service;

import android.app.Application;
import android.content.Context;
/**
 *
 */

/**
 * ┏┓　 ┏┓
 * ┏┛┻━┛┻━┓
 * ┃　　　    　┃
 * ┃　　        ┃
 * ┃┳┛　┗┳  ┃
 * ┃　　　　　  ┃
 * ┃　　　┻　  ┃
 * ┃　　　　　  ┃
 * ┗━┓　　　┏┛
 * ┃　　　┃   神兽保佑
 * ┃　　　┃   代码无BUG！
 * ┃　　　┗━━━┓
 * ┃　　　　　   ┣┓
 * ┃　　　　　   ┏┛
 * ┗┓┓┏━ ┳┓┏┛
 * ┃┫┫　 ┃┫┫
 * ┗┻┛　 ┗┻┛
 */

public class MyApplication extends Application {
    //<editor-fold desc="init">
    //初始化
    //</editor-fold>
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }


}
