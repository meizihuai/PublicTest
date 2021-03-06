package com.getinfo.app.uniqoe.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
//PL播放器播放页面可能用到的一些封装方法
public class Utils {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Is the live streaming still available
     * @return is the live streaming is available
     */
    public static boolean isLiveStreamingAvailable() {
        // Todo: Please ask your app server, is the live streaming still available
        return true;
    }

    public static void showToastTips(final Context context, final String tips) {
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show();
    }
    /**
     * Toast弹窗
     * @param context
     * @param msg
     * @param showTime 0表示短，1表示长
     */
    public static void  MsgBox(Context context,String msg,int showTime){
        if(showTime!=0){
            showTime=1;
        }
        Toast.makeText(context, msg, showTime).show();
    }
}
