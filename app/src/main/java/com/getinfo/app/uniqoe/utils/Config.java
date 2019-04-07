package com.getinfo.app.uniqoe.utils;

import android.os.Environment;
//PLDroidPlayer播放器的设置，SD卡缓存位置
public class Config {

    public static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    public static final String DEFAULT_CACHE_DIR = SDCARD_DIR + "/PLDroidPlayer";
}
