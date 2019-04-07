package com.getinfo.app.uniqoe.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class LogHelper {
    private static String rootPath = "/sdcard/ApublicTest/AppLogs/";
    private  static Object lock=new Object();
    private static  void checkAppDir(){

    }
    private static  void checkDir(){
        try{
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                File file=new File(rootPath);
                if( !file.exists()){
                    Log.i("LogHelper","mkdir");
                   boolean bool= file.mkdirs();
                    Log.i("LogHelper","mkdirs result="+bool);
                }else{
                    if(file.isFile()){
                        file.delete();
                        file.mkdirs();
                    }
                }
                String oldPath=rootPath+getOldDayFileString();
                file=new File(oldPath);
                if(file.exists()){
                    file.delete();
            }
            }
        }catch (Exception e){
              Log.i("LogHelper",e.getMessage());
        }

    }
    public static  String getOldDayFileString(){
        SimpleDateFormat sf = new SimpleDateFormat("yyyy_MM_dd");
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -3);
        return sf.format(c.getTime())+".txt";
    }
    public static synchronized void log(Context context, String content) {

        try{
            checkDir();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String fileName=simpleDateFormat.format(date)+".txt";;
            String path=rootPath+fileName;
//            File file=new File(path);
//            if(!file.exists()){
//                file.createNewFile();
//            }
            FileOutputStream outputStream = new FileOutputStream(path,true);
            SimpleDateFormat hhmmss = new SimpleDateFormat("[HH:mm:ss] ");
            content=hhmmss.format(date)+content+"\n";
            outputStream.write(content.getBytes());
            outputStream.close();
        }catch (Exception e){
            Log.i("LogHelper",e.getMessage());
        }
    }
}
