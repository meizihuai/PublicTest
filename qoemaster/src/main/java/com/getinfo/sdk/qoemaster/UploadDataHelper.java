package com.getinfo.sdk.qoemaster;


import android.util.Log;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

//数据统一出口，此类必须单例使用，所有上传到服务器的数据都通过此类
public class UploadDataHelper {
    // private String serverURL="";
    private String serverURL = GlobalInfo.serverUrl;
    private static UploadDataHelper uploadDataHelper;
    private Object lock = new Object();

    public synchronized static UploadDataHelper getInstance() {
        if (uploadDataHelper == null) {
            uploadDataHelper = new UploadDataHelper(1);
        }
        return uploadDataHelper;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public UploadDataHelper(int i) {

    }

    public void UploadDataToServer(PhoneInfo pi) {
        if (pi == null) return;
        Log.i("hasaki", "上传新PhoneInfo到服务器");
        final PhoneInfo fpi = pi;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UploadData(fpi);
                } catch (Exception e) {

                }
            }
        }).start();
    }


    public void UploadDataToServer(QoEVideoInfo qoEVideoInfo) {
        if (qoEVideoInfo == null) return;
        Log.i("hasaki", "上传新QoEVideoInfo到服务器");
        final QoEVideoInfo qoe = qoEVideoInfo;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UploadData(qoe);
                } catch (Exception e) {

                }
            }
        }).start();
    }

    public void UploadObjectToServer(final Object object, final String objectName) {
        if (object == null) return;
        Log.i("Upload" + objectName, "上传新" + objectName + "到服务器");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UploadObject(object, objectName);
                } catch (Exception e) {

                }
            }
        }).start();
    }

    private void UploadObject(Object object, final String objectName) {
        Gson gson = new Gson();
        String str = gson.toJson(object);
        Log.i("Upload" + objectName, str);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "Upload" + objectName);
            JSONObject j = new JSONObject(str);
            json.put("data", j);
            final String requestBody = json.toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAsynHttp(requestBody, 0, "Upload" + objectName, "Upload" + objectName);
                }
            }).start();
        } catch (Exception e) {

        }
    }


    private boolean isUploadOldDataing = false;

    private List<LocalTestInfoHelper.LocalTestInfo> localNomal = new ArrayList<>();//正常离线数据集合
    private List<LocalTestInfoHelper.LocalTestInfo> localBP = new ArrayList<>();//黑点线数据 集合

    private void UploadOldData() {
        // Log.i("UploadofflineDatas","isUploadOldDataing = "+isUploadOldDataing);
        if (isUploadOldDataing) return;
        isUploadOldDataing = true;
        //TODO 查询数据库 查询出所有离线数据
        LocalTestInfoHelper localTestInfoHelper = LocalTestInfoHelper.getInstance();
        LocalTestInfoHelper.LocalTestInfo[] localTestInfos = localTestInfoHelper.getLocalTestInfo();

        if (localTestInfos == null) {
            Log.i("UploadofflineDatas", "localTestInfos is null");
            isUploadOldDataing = false;
            return;
        }
        if (localTestInfos.length == 0) {
            Log.i("UploadofflineDatas", "localTestInfos.length=0");
            isUploadOldDataing = false;
            return;
        }
        //区分正常离线数据和黑点数据
        separatedData(localTestInfos);
        if (localBP.size() > 0) {
            AginUpBPData();//上传黑点数据
        }
        if (localNomal.size() > 0) { //上传正常离线数据
            uploadNomalLD();
        }
    }

    //区分正常离线数据 和 黑点数据
    private void separatedData(LocalTestInfoHelper.LocalTestInfo[] localTestInfos) {
        localBP.clear();
        localNomal.clear();
        if (null != localTestInfos && localTestInfos.length > 0) {
            for (LocalTestInfoHelper.LocalTestInfo localTestInfo : localTestInfos) {
                if ("QoEBlackPoint".equals(localTestInfo.type)) {//黑点数据
                    localBP.add(localTestInfo);
                } else {//非黑点数据  正常离线数据
                    localNomal.add(localTestInfo);
                }
            }
        }
    }

    //上传正常的离线数据
    private void uploadNomalLD() {
        int maxUploadLength = 100;
        if (localNomal.size() < maxUploadLength) {
            maxUploadLength = localNomal.size();
        }
        Log.i("UploadofflineDatas", "localNomal.size()=" + localNomal.size());
        int[] ids = new int[maxUploadLength];
        LocalTestInfoHelper.LocalTestInfo[] tmpTestInfos;
        tmpTestInfos = new LocalTestInfoHelper.LocalTestInfo[maxUploadLength];
        for (int i = 0; i < maxUploadLength; i++) {
            ids[i] = localNomal.get(i).id;
            tmpTestInfos[i] = localNomal.get(i);
        }
        Log.i("UploadofflineDatas", "UploadofflineDatas.length=" + maxUploadLength);
        Gson gson = new Gson();
        String str = gson.toJson(tmpTestInfos);
        final int[] handleIds = ids;
        try {
            JSONObject json = new JSONObject();
            json.put("func", "UploadofflineDatas");
            // JSONObject j = new JSONObject(str);
            json.put("data", str);
            final String requestBody = json.toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        Log.i("UploadofflineDatas", "asycUploadOldData ing ...");
                        asycUploadOldData(requestBody, 0, "UploadofflineDatas", handleIds);
                        isUploadOldDataing = false;
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.i("UploadofflineDatas", "err-->" + e.getMessage());
            isUploadOldDataing = false;
        }
    }

    //上传黑点数据
    private void AginUpBPData() {
        QoEBlackPoint qoEBlackPoint=null;
        for (LocalTestInfoHelper.LocalTestInfo localTestInfo : localBP) {
            if ("QoEBlackPoint".equals(localTestInfo.type)) {//是黑点数据
                try {
                    qoEBlackPoint = new Gson().fromJson(localTestInfo.json, QoEBlackPoint.class);
                    if (qoEBlackPoint != null) {
                        qoEBlackPoint.UploadBPToServer(localTestInfo.id);//上传黑点数据
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    //上传历史离线数据
    private void asycUploadOldData(final String requestBody, int count, final String TAG, final int[] ids) {
        if (count >= 1) return;
        try {
            final int uploadCount = count;
            final String requestBodyTmp = requestBody;
            Log.i(TAG, "serverURL=" + serverURL);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.HOURS)
                    .readTimeout(10, TimeUnit.HOURS)
                    .build();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                    .build();

            Call call = okHttpClient.newCall(request);
            Log.i("UploadofflineDatas", "send request");
            Response response = call.execute();
            if (response.isSuccessful()) {
                final String result = response.body().string();
                try {
                    Log.i("UploadofflineDatas", result);
                    JSONObject jsonObject = new JSONObject(result);
                    boolean flag = jsonObject.getBoolean("result");
                    if (flag) {

                    }
                    LocalTestInfoHelper localTestInfoHelper = LocalTestInfoHelper.getInstance();
                    localTestInfoHelper.deleteById(ids);
                    isUploadOldDataing = false;
                } catch (Exception e) {
                    Log.i("UploadofflineDatas", "err3-->" + e.getMessage());
                    isUploadOldDataing = false;
                }
            } else {
                Log.i("UploadofflineDatas", "response.isSuccessful = false");
                isUploadOldDataing = false;
            }

//            call.enqueue(new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    Log.i("UploadofflineDatas","err4-->"+ e.getMessage());
//                    isUploadOldDataing=false;
////                    String tmp = requestBodyTmp;
////                    int iTmp = uploadCount;
////                    iTmp++;
////                    asycUploadOldData(tmp, iTmp,TAG,ids);
//                }
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    if (response.isSuccessful()) {
//                        final  String result = response.body().string();
//                        try{
//                            JSONObject jsonObject=new JSONObject(result);
//                            boolean flag=jsonObject.getBoolean("result");
//                            if(flag){
//                                LocalTestInfoHelper localTestInfoHelper=LocalTestInfoHelper.getInstance();
//                                localTestInfoHelper.deleteById(ids);
//                            }
//                            isUploadOldDataing=false;
//                        }catch (Exception e){
//                            isUploadOldDataing=false;
//                            Log.i("UploadofflineDatas","err3-->"+e.getMessage());
//                        }
//                    }else{
//                        isUploadOldDataing=false;
//                        Log.i("UploadofflineDatas","response.isSuccessful = false "+response.code());
//                    }
//                }
//            });
        } catch (Exception e) {
            isUploadOldDataing = false;
            Log.i("UploadofflineDatas", "err2->" + e.getMessage());
        }
    }


    private void UploadData(PhoneInfo npi) {
        npi.ADJ_SIGNAL="";
        npi.sigNalInfo="";
        Gson gson = new Gson();
        String str = gson.toJson(npi);
        Log.i("UploadPhoneInfo", str);
        try {
            JSONObject json = new JSONObject();
            json.put("func", "UploadPhoneInfo");
            JSONObject j = new JSONObject(str);
            json.put("data", j);

            final String requestBody = json.toString();
            Log.i("UploadPhoneInfo","字节长度="+requestBody.length());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAsynHttp(requestBody, 0, "UploadPhoneInfo", "UploadPhoneInfo");
                }
            }).start();
        } catch (Exception e) {

        }
    }

    private void UploadData(QoEVideoInfo qoEVideoInfo) {
        Log.i("UploadQoEVideoInfo", "1");
        try {
            qoEVideoInfo.pi.ADJ_SIGNAL="";
            qoEVideoInfo.pi.sigNalInfo="";
            Gson gson = new Gson();
            String str = gson.toJson(qoEVideoInfo);
            Log.i("UploadQoEVideoInfo", str);
            JSONObject json = new JSONObject();
            json.put("func", "UploadQoEVideoInfo");
            JSONObject j = new JSONObject(str);
            json.put("data", j);
            final String requestBody = json.toString();
            Log.i("UploadQoEVideoInfo","字节长度="+requestBody.length());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    getAsynHttp(requestBody, 0, "UploadQoEVideoInfo", "UploadQoEVideoInfo");
                }
            }).start();
        } catch (Exception e) {
            Log.i("UploadQoEVideoInfo", e.getMessage());
        }
    }

    private void getAsynHttp(final String requestBody, final int count, final String TAG, final String type) {
        if (count == 0) {
            try {
                UploadOldData();
            } catch (Exception e) {

            }
        }
        if (count >= 2) return;
        try {
            final int uploadCount = count;
            final String requestBodyTmp = requestBody;
            Log.i("getAsynHttp", "serverURL=" + serverURL);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.MINUTES)
                    .readTimeout(5, TimeUnit.MINUTES)
                    .build();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
            Request request = new Request.Builder()
                    .url(serverURL)
                    .post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                    .build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("getAsynHttpERR", "onFailure," + e.getMessage());
                    LocalTestInfoHelper localTestInfoHelper = LocalTestInfoHelper.getInstance();
                    localTestInfoHelper.addLocalTestInfo(requestBody, type);
//                    Log.i(TAG, e.getMessage());
//                    String tmp = requestBodyTmp;
//                    int iTmp = uploadCount;
//                    iTmp++;
//                    getAsynHttp(tmp, iTmp,TAG,type);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        final String result = response.body().string();
                        Log.i(TAG, result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            boolean flag = jsonObject.getBoolean("result");
                            if (!flag) {
                                LocalTestInfoHelper localTestInfoHelper = LocalTestInfoHelper.getInstance();
                                localTestInfoHelper.addLocalTestInfo(requestBody, type);
                            }
                        } catch (Exception e) {

                        }
                    } else {
//                        Log.i("getAsynHttpERR", "response.isSuccessful=false");
//                        LocalTestInfoHelper localTestInfoHelper=LocalTestInfoHelper.getInstance();
//                        localTestInfoHelper.addLocalTestInfo(requestBody,type);
                    }
                }
            });
        } catch (Exception e) {
            Log.i("getAsynHttpERR", "Exception," + e.getMessage());
            LocalTestInfoHelper localTestInfoHelper = LocalTestInfoHelper.getInstance();
            localTestInfoHelper.addLocalTestInfo(requestBody, type);
        }
    }

    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }
}
