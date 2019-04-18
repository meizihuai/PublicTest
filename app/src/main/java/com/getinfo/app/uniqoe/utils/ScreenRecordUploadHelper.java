package com.getinfo.app.uniqoe.utils;

import android.app.Activity;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import com.getinfo.sdk.qoemaster.DeviceHelper;
import com.getinfo.sdk.qoemaster.FileHelper;

import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ScreenRecordUploadHelper {

    private static boolean isNeedWork=false;
    public static boolean isPlayingVideo=false;
    private static Thread workThread;
    public static  String serverURL="http://111.53.74.132:7062/default.ashx";
    private static  String TAG="ScreenRecordUploadHelper";
    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ApublicTest/screenRecord/";
    public static String GetNewScreenRecordFileName(){
         String uuid=DeviceHelper.GetNewUUID();
         String fileName=uuid+".mp4";
         return  fileName;
    }
    public static void StartWork(final Activity activity){
        isNeedWork=true;
         int sleepSeond=60;
         final   int sleepMillscond=sleepSeond*1000;
         workThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (isNeedWork){
                    boolean isDoWork=true;
                    if(isPlayingVideo){
                        isDoWork=false;
                    }
                    Log.i(TAG,"===========================分界线===========================");
                    if(isDoWork){
                        try{

                            boolean isWifiConnected= DeviceHelper.isWifiConnect(activity);
                            if(isWifiConnected){
                                Log.i(TAG,"wifi状态，checkfile");
                                CheckLocalFiles(activity);
                            }else{
                                Log.i(TAG,"非wifi状态，不checkfile");
                            }
                        }catch (Exception e){

                        }
                    }else{
                        Log.i(TAG,"设备正在播放视频或其他操作，不执行checkfile");
                    }
                    try{
                        Thread.sleep(sleepMillscond);
                    }catch (Exception e){

                    }
                }
            }
        });
        workThread.start();
    }
    public static void StopWork(){
        try{
            isNeedWork=false;
        }catch (Exception e){

        }
    }

    public static  void CheckLocalFiles(final Activity activity){
        try{
            List < String > fileList = FileHelper.GetFiles(path);
            if(fileList==null)return;
            if(fileList.size()==0)return;
            for(int i=0;i<fileList.size();i++){
                String fileName=fileList.get(i);
                CheckLocalFile(fileName,true,activity);
            }
        }catch (Exception e){

        }
    }
    private  static void CheckLocalFile(final String fileName,final boolean isUpload,final Activity activity){
        try{
            JSONObject json=new JSONObject();
            json.put("func","CheckScreenRecordFile");
            json.put("data",fileName);
            String requestBody=json.toString();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                    .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()) {
                final String result = response.body().string();
                Log.i(TAG,"CheckLocalFile fileName="+fileName+" result="+result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean flag = jsonObject.getBoolean("result");
                    if (flag) {
                        int data=jsonObject.getInt("data");
                        if(data==1){
                            if(isUpload){
                                UploadScreedRecordFile(fileName,activity);
                            }
                        }else{
                           String filePath=path+fileName;
                           File file=new File(filePath);
                           if(file.exists()){
                            //   file.delete();
                           }
                        }
                    }
                } catch (Exception e) {

                }
            }
        }catch (Exception e){

        }
    }
    private  static void UploadScreedRecordFile(String fileName,final Activity activity){
        try{
            Log.i(TAG,"正在UploadScreedRecordFile fileName="+fileName+" ...");
            String filePath=path+fileName;
            byte[] buffer=FileHelper.fileReadAllByte(activity,filePath);
            String base64=new String(Base64.encode(buffer, Base64.DEFAULT));

            JSONObject data=new JSONObject();
            data.put("fileName",fileName);
            data.put("filelenth",buffer.length);
            data.put("base64",base64);
            buffer=null;
            JSONObject ps=new JSONObject();
            ps.put("func","UploadScreenRecordFile");
            ps.put("data",data);
            String requestBody=ps.toString();
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                    .build();
            Call call = okHttpClient.newCall(request);
            Response response = call.execute();
            if (response.isSuccessful()) {
                final String result = response.body().string();
                Log.i(TAG,"UploadScreedRecordFile fileName="+fileName+" result="+result);
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean flag = jsonObject.getBoolean("result");
                    if (flag) {
                        File file=new File(filePath);
                        if(file.exists()){
                            file.delete();
                        }
                    }
                } catch (Exception e) {

                }
            }
        }catch ( Exception e){
            Log.i(TAG,e.toString());
        }
    }

}
