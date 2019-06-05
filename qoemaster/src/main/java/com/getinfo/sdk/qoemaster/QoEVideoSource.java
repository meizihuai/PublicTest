package com.getinfo.sdk.qoemaster;



import android.util.Log;

import com.getinfo.sdk.qoemaster.Interfaces.InterfaceCls;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//QOE本地视频资源，V1.3.0版本后已经增加了动态获取视频资源的功能
public class QoEVideoSource {
    public   static  String wantType="全部";
    private static   boolean flagIsGetVideoSourceFromServer=false;
    private static QoEVideoSourceInfo oldQoEVideoSourceInfo=null;
    public static  String serverURL="http://111.53.74.132:7062/default.ashx";
    public static class QoEVideoSourceInfo{
        public  String name;
        public String url;
        public long fileSize;
        public int videoSecond;
        public  String type;
        public String movieName;
        public  int movieIndex;
        public  String video_clarity;
        public  boolean isRand;
        public  long secondGrad;
        public String wantType;
        public String imsi;
        public long qoe_total_times;
        public long qoe_total_E_times;
        public long qoe_today_times;
        public long qoe_today_E_times;
    }
    private static QoEVideoSourceInfo[] qoEVideoSourceInfos;
    //从数据获取新的视频资源
    public static QoEVideoSourceInfo getVideoSourceFromServer(String qoeMissionWantType){
        Log.i("getVideoSource","getVideoSourceFromServer");
        try{
            if(oldQoEVideoSourceInfo==null){
                oldQoEVideoSourceInfo=new QoEVideoSourceInfo();
            }
            oldQoEVideoSourceInfo.wantType=wantType;
            if(!"".equals(qoeMissionWantType)){
                oldQoEVideoSourceInfo.wantType=qoeMissionWantType;
            }
            oldQoEVideoSourceInfo.imsi= GlobalInfo.myDeviceImsi;
            PostMsg ps=new PostMsg("GetNewQoEVideoInfo",oldQoEVideoSourceInfo);
            final String requestBody = new Gson().toJson(ps);
            Log.i("getVideoSource","requestBody="+requestBody);
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
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    boolean flag = jsonObject.getBoolean("result");
                    Log.i("getVideoSource","flag="+flag+"  请求耗时="+jsonObject.getString("errmsg"));
                    if (flag) {
                        try{
                            Gson gson=new Gson();
                            QoEVideoSourceInfo tmp=gson.fromJson(jsonObject.getJSONObject("data").toString(),QoEVideoSourceInfo.class);
                            if(tmp!=null){
                                Log.i("getVideoSource",jsonObject.getJSONObject("data").toString());
                                oldQoEVideoSourceInfo=tmp;
                                flagIsGetVideoSourceFromServer=true;
                                return  oldQoEVideoSourceInfo;
                            }
                        }catch (Exception e){

                        }
                    }else{
                        Log.i("getVideoSource",result);
                    }
                } catch (Exception e) {
                    Log.i("getVideoSource","err1-->"+e.toString());
                }
            } else {
                Log.i("getVideoSource","response.isSuccessful=false");
            }
        }catch (Exception e){
            Log.i("getVideoSource","err2-->"+e.toString());
        }
        return  null;
    }
    public static synchronized QoEVideoSourceInfo getNewQoEVideoSourceInfo(){
        flagIsGetVideoSourceFromServer=false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getVideoSourceFromServer("");
            }
        }).start();
        while (true){
            if(flagIsGetVideoSourceFromServer && oldQoEVideoSourceInfo!=null){
                return oldQoEVideoSourceInfo;
            }
            try{
                Thread.sleep(300);
            }catch (Exception e){

            }
        }
    }
    public  static void getNewQoEVideoSourceInfoAsync(final InterfaceCls.IQoEVideoSource iQoEVideoSource,final String qoeMissionWantType){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i<5; i++) {
                    QoEVideoSourceInfo qoEVideoSourceInfo = getVideoSourceFromServer(qoeMissionWantType);
                    if(qoEVideoSourceInfo!=null){
                        iQoEVideoSource.onNewQoEVideoSourceInfo(qoEVideoSourceInfo);
                        return;
                    }
                }
                iQoEVideoSource.onError("视频地址请求失败");
            }
        }).start();
    }
}
