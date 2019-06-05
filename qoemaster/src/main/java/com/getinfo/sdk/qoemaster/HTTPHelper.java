package com.getinfo.sdk.qoemaster;



import android.util.Log;


import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HTTPHelper {
    public interface HTTPResponse{
        void OnNormolResponse(NormalResponse np);
    }
    public interface HTTPResponseTime{
        void OnResponseTime(long responseTime,long bufferTotalTime,long bufferSize);
    }
    public  static void GetH(final String url,final HTTPResponse httpResponse) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(1, TimeUnit.MINUTES)
                            .readTimeout(1, TimeUnit.MINUTES)
                            .build();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            httpResponse.OnNormolResponse(new NormalResponse(false,"请求失败"+e.getMessage(),url,""));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String result = response.body().string();
                                Gson gson=new Gson();
                                try{
                                    NormalResponse np=gson.fromJson(result,NormalResponse.class);
                                    httpResponse.OnNormolResponse(np);
                                }catch (Exception e){
                                    httpResponse.OnNormolResponse(new NormalResponse(false,e.getMessage(),url,""));
                                }
                            }else{
                                httpResponse.OnNormolResponse(new NormalResponse(false,"请求失败"+response.code(),url,""));
                            }
                        }
                    });
                }catch (Exception e) {
                    httpResponse.OnNormolResponse(new NormalResponse(false, e.getMessage(),url,""));
                }
            }
        }).start();
    }

    public  static void PostH(final String url,final Object obj,final HTTPResponse httpResponse) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final String requestBody = new Gson().toJson(obj);

                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(1, TimeUnit.MINUTES)
                            .readTimeout(1, TimeUnit.MINUTES)
                            .build();
                 //   MediaType mediaType = MediaType.parse("application/json; charset=utf-8;");
                    //.post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                    RequestBody req = FormBody.create(MediaType.parse("application/json; charset=utf-8")
                            , requestBody);


                    Request request = new Request.Builder()
                            .url(url)
                            .post(req)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            httpResponse.OnNormolResponse(new NormalResponse(false,"请求失败"+e.getMessage()));
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String result = response.body().string();
                                Gson gson=new Gson();
                                try{
                                    NormalResponse np=gson.fromJson(result,NormalResponse.class);
                                    httpResponse.OnNormolResponse(np);
                                }catch (Exception e){
                                    httpResponse.OnNormolResponse(new NormalResponse(false,e.getMessage()));
                                }
                            }else{
                                httpResponse.OnNormolResponse(new NormalResponse(false,"请求失败"+response.code()+","+requestBody));
                            }
                        }
                    });
                }catch (Exception e) {
                    httpResponse.OnNormolResponse(new NormalResponse(false, e.getMessage()));
                }
            }
        }).start();
    }

    public  static void TestResponseTime(final String url,final boolean needGetBufferTotalTime ,final HTTPResponseTime httpResponseTime) {
        final long startMillsecond=System.currentTimeMillis();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    OkHttpClient okHttpClient = new OkHttpClient.Builder()
                            .connectTimeout(1, TimeUnit.MINUTES)
                            .readTimeout(1, TimeUnit.MINUTES)
                            .build();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            httpResponseTime.OnResponseTime(System.currentTimeMillis()-startMillsecond,0,0);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            long responseTime=System.currentTimeMillis()-startMillsecond;
                            if(needGetBufferTotalTime){
                                if (response.isSuccessful()) {
                                    InputStream is=null;
                                    try{
                                        is=response.body().byteStream();
                                        long total = response.body().contentLength();//获取文件大小
                                        long totalBufferSize=0;
                                        if (is != null) {
                                            byte[] buf = new byte[1024];
                                            int ch = -1;
                                            int process = 0;
                                            while ((ch = is.read(buf)) != -1) {
                                                totalBufferSize+=ch;
                                            }
                                        } else {

                                        }
                                        long bufferTotalTime=System.currentTimeMillis()-startMillsecond;
                                        httpResponseTime.OnResponseTime(responseTime,bufferTotalTime,totalBufferSize);
                                        return;
                                    }catch (Exception e){

                                    }finally {
                                        if(is!=null){
                                            is.close();
                                        }
                                    }
                                    httpResponseTime.OnResponseTime(responseTime,responseTime,0);
                                    return;
                                }else{
                                    httpResponseTime.OnResponseTime(responseTime,0,0);
                                    return;
                                }
                            }else{
                                httpResponseTime.OnResponseTime(responseTime,0,0);
                                return;
                            }
                        }
                    });
                }catch (Exception e) {
                    httpResponseTime.OnResponseTime(System.currentTimeMillis()-startMillsecond,0,0);
                    return;
                }
            }
        }).start();
    }
}
