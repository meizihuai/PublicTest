package com.getinfo.sdk.qoemaster.MyUtils;

import android.util.Log;

public class MyCatchUtils implements Thread.UncaughtExceptionHandler {
    private String TAG = "MyCatchUtils";
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.i(TAG,""+e.getMessage());
        //TODO 捕获异常后的逻辑操作
    }
}
