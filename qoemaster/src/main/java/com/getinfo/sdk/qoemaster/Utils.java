package com.getinfo.sdk.qoemaster;

import android.content.Context;
import android.widget.Toast;

public class Utils {
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
