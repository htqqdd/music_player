package com.example.lixiang.music_player;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import io.objectbox.BoxStore;

/**
 * Created by lixiang on 2017/9/9.
 */

public class MyApplication extends Application {
    private boolean isNet = false;
    public boolean getIsNet() {
        return isNet;
    }
    public void setIsNet(boolean b) {
        isNet = b;
    }
//    public BoxStore boxStore;


    @Override
    public void onCreate() {
//        boxStore = MyObjectBox.builder().androidContext(this).build();
        Context context = getApplicationContext();
// 获取当前包名
        String packageName = context.getPackageName();
// 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
// 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
// 初始化Bugly
        Bugly.init(getApplicationContext(), "1d65abe1b1", false);
        Beta.initDelay = 1 * 1000;
//        Beta.upgradeDialogLayoutId = R.layout.upgrade_dialog;
        super.onCreate();
    }
//    public BoxStore getBoxStore(){
//        return boxStore;
//    }

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}
