package com.hyj.privacy_protectedcrowdsensingsystem.util;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by hyj on 2018/12/28.
 */

public class P2CSApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
    }

}
