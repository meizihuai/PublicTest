package com.getinfo.app.uniqoe;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.lang.UProperty;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.getinfo.app.uniqoe.utils.DeviceHelper;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//测试页面  包含网速测试、视频测试、网页测试等，支持一键测试、循环测试
public class FrmOneKeyTest extends Fragment {
    private   int slowlySpeedCountMax=8;
    private String netSpeed="";
    private OnFragmentInteractionListener mListener;
    private String TAG = "FrmOneKeyTest";
    private PhoneInfo pi;
    private Handler handler;
//    private String testUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
//    private long videoFileSize = 5510872;
//    private int videoSecond = 60;
    private AlertDialog alertDialog2;
    private String testUrl= "http://221.238.40.153:7062/video/720P_30s_DY__clip215.mp4"; //测试地址
    private long videoFileSize = 2410306; //测试地址的文件大小
    private int videoSecond = 30; //测试地址视频的播放时长，单位秒
    private TextView txt4gInfo;
    private TextView txt4gStrength, txt4gQuality, txtWIFIStrength, txtWIFIQuality;
    private ProgressBar pro4gStrength, pro4gQuality, proWIFIStrength, proWIFIQuality;
    private QoEVideoInfo qoEVideoInfo;
    private TextView txtNetSpeed, txtVideo, txtHTMLPage;
    private ProgressBar proNetSpeed, proVideo, proHTMLPage;

    private TextView txtPhoneModel, txtphoneOS, txtLon,txtLat,txtSiteCount, txtnetType, txtSignalType;

    private TextView txtScore;
    private CheckBox checKboxTestloop;
    private boolean autoTestLoop = false;

    private View myView;
    private Button btnOneKeyTest;
    private boolean flagMissionAbort = false;
    private boolean flagGetFristPI = false;
    private ScoreHelper scoreHelper;
    private OneKeyTestInfo oneKeyTestInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_one_key_test, container, false);
        iniDialog();
        GetOneKeyTestUrl();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == -1) { //测试失败，网络问题
                    autoTestLoop = checKboxTestloop.isChecked();
                    Log.i(TAG, "测试失败，网络问题,autoTestLoop="+autoTestLoop);
                    if (autoTestLoop) {
                        btnOneKeyTest.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 10; i > 0; i--) {
                                    final int seond = i;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnOneKeyTest.setText("等待下载测试中," + seond + " 秒");
                                        }
                                    });
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        OneKeyTest();
                                    }
                                });
                            }
                        }).start();
                    }else{
                        btnOneKeyTest.setEnabled(true);
                        btnOneKeyTest.setText("一键测试");
                        iniTVS(false);

                    }
                }
                if (msg.what == 0) { //整体测试完毕
                    TestComplete();
                    UploadOneKeyTest();
                    autoTestLoop = checKboxTestloop.isChecked();
                    if (autoTestLoop) {
                        btnOneKeyTest.setEnabled(false);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 10; i > 0; i--) {
                                    final int seond = i;
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            btnOneKeyTest.setText("等待下载测试中," + seond + " 秒");
                                        }
                                    });
                                    try {
                                        Thread.sleep(1000);
                                    } catch (Exception e) {

                                    }
                                }
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        OneKeyTest();
                                    }
                                });
                            }
                        }).start();
                    }else{
                        ShowDetailInfoActivity();
                    }
                }
                if (msg.what == 1) {  //网速测试完了
                    VideoTest();
                }
                if (msg.what == 2) {     //视频测试完了
                    HTMLPageTest();
                }
                super.handleMessage(msg);
            }
        };
        btnOneKeyTest = myView.findViewById(R.id.btnOneKeyTest);
        btnOneKeyTest.setEnabled(false);
        scoreHelper = new ScoreHelper();
        oneKeyTestInfo = new OneKeyTestInfo();

        iniTVS(true);
        final GetPhoneInfoHelper gpi = new GetPhoneInfoHelper(getContext(), null);
        gpi.StartWork();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    final PhoneInfo pi = gpi.GetPhoneInfo();
                    if (pi == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            OnRecivePhoneInfo(pi);
                        }
                    });
                } catch (Exception e) {

                }
            }
        }).start();
        btnOneKeyTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OneKeyTest();
            }
        });
        return myView;
    }
    private  void ShowDetailInfoActivity(){
        try{
            if(oneKeyTestInfo==null)return;
            Gson gson=new Gson();
            String str=gson.toJson(oneKeyTestInfo);
            Intent intent = new Intent(getActivity(), OneKeyTestResultActivity.class);
            intent.putExtra("oneKeyTestInfo",str);
            startActivity(intent);
        }catch ( Exception e){

        }
    }
    private void GetOneKeyTestUrl(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String url=GlobalInfo.defaultServerUrl;
                    Setting setting=GlobalInfo.getSetting(getContext());
                    if(setting!=null){
                        url=setting.serverUrl;
                    }
                    url=url+"?func=GetNewVersionOneKeyTestUrl";
                    Log.i("GetOneKeyTestUrl","url="+url);
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url(url)//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
//                        Log.d("kwwl", "response.code()==" + response.code());
//                        Log.d("kwwl", "response.message()==" + response.message());
//                        Log.d("kwwl", "res==" + response.body().string());
                         String res=response.body().string();
                         Log.i("GetOneKeyTestUrl",res);
                        JSONObject jsonObject=new JSONObject(res);
                        boolean result=jsonObject.getBoolean("result");
                        JSONObject data=jsonObject.getJSONObject("data");
                        if(result){
                            testUrl=data.getString("url");
                            videoFileSize=data.getLong("filesize");
                            videoSecond=data.getInt("videoSecond");
                            Log.i("GetOneKeyTestUrl","testUrl="+testUrl+";videoFileSize="+videoFileSize+";videoSecond="+videoSecond);
                        }

                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnOneKeyTest.setEnabled(true);
                        }
                    });

                } catch (Exception e) {
                    Log.i("GetOneKeyTestUrl",e.toString());
                    e.printStackTrace();
                }
            }
        }).start();
    }
    //使用流量测试提示窗口
   private void iniDialog(){
       alertDialog2 = new AlertDialog.Builder(getContext())
               .setTitle("提示")
               .setMessage("正在使用移动数据进行循环测试，可能会产生流量费用，是否继续循环测试？")
               .setIcon(R.mipmap.site)
               .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {
                       btnOneKeyTest.setEnabled(false);
                       btnOneKeyTest.setText("正在测试……");
                       iniTVS(false);
                       TestWork();
                   }
               })

               .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加取消
                   @Override
                   public void onClick(DialogInterface dialogInterface, int i) {

                   }
               })
               .create();
   }
    //<editor-fold desc="Fragment自带相关">
    public FrmOneKeyTest() {

    }

    public static FrmOneKeyTest newInstance(String param1, String param2) {
        FrmOneKeyTest fragment = new FrmOneKeyTest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Uri uri);
    }

    //</editor-fold>
    //主进程发来位置信息的改变
    public void setLocations(double lon, double lat, double accuracy, double altitude, double speed, double satelliteCount) {
        if (!flagGetFristPI) return;
        if (pi == null) return;
        DecimalFormat decimalFormat = new DecimalFormat("0.000000");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String lonstr = decimalFormat.format(lon);//format 返回的是字符串
        String latstr = decimalFormat.format(lat);//format 返回的是字符串
        txtLon.setText("经度:" + lonstr);
        txtLat.setText("纬度:" + latstr);
        txtSiteCount.setText("卫星:"+(int)satelliteCount);
        pi.lon = lon;
        pi.lat = lat;
        pi.accuracy = accuracy;
        pi.altitude = altitude;
        pi.gpsSpeed = speed;
        pi.satelliteCount = (int) satelliteCount;
        if (qoEVideoInfo != null) {
                QoEVideoInfo.GPSPoint gpsPoint = qoEVideoInfo.new GPSPoint(lon, lat);
                if(qoEVideoInfo.GPSPointList.size()<5){
                    qoEVideoInfo.GPSPointList.add(gpsPoint);
                    if (pi != null) {
                       // qoEVideoInfo.SIGNALList.add(pi.RSRP + "");
                        qoEVideoInfo.SIGNALList.add(pi.sigNalInfo);
                        if(pi.xyZaSpeed!=null){
                            qoEVideoInfo.ACCELEROMETER_DATAList.add(pi.xyZaSpeed);
                        }
                    }
                }
        }
    }

    private void iniTVS(boolean flagFristTime) {
        if (flagFristTime) {
            btnOneKeyTest.setText("等待初始化...");
            btnOneKeyTest.setEnabled(false);

            txt4gInfo=myView.findViewById(R.id.txt4gInfo);
            txt4gStrength = myView.findViewById(R.id.txt4gStrength);
            txt4gQuality = myView.findViewById(R.id.txt4gQuality);
            txtWIFIStrength = myView.findViewById(R.id.txtWIFIStrength);
            txtWIFIQuality = myView.findViewById(R.id.txtWIFIQuality);

            txtNetSpeed = myView.findViewById(R.id.txtNetSpeed);
            txtVideo = myView.findViewById(R.id.txtVideo);
            txtHTMLPage = myView.findViewById(R.id.txtHTMLPage);

            txtPhoneModel = myView.findViewById(R.id.txtphoneModel);
            txtphoneOS = myView.findViewById(R.id.txtphoneOS);
            txtLon = myView.findViewById(R.id.txtLon);
            txtLat = myView.findViewById(R.id.txtLat);
            txtSiteCount = myView.findViewById(R.id.txtSiteCount);
            txtnetType = myView.findViewById(R.id.txtnetType);
            txtSignalType = myView.findViewById(R.id.txtSignalType);


            pro4gStrength = myView.findViewById(R.id.pro4gStrength);
            pro4gQuality = myView.findViewById(R.id.pro4gQuality);
            proWIFIStrength = myView.findViewById(R.id.proWIFIStrength);
            proWIFIQuality = myView.findViewById(R.id.proWIFIQuality);

            proNetSpeed = myView.findViewById(R.id.proNetSpeed);
            proVideo = myView.findViewById(R.id.proVideo);
            proHTMLPage = myView.findViewById(R.id.proHTMLPage);


            txtScore = myView.findViewById(R.id.txtScore);

            //checKbox = myView.findViewById(R.id.checKbox);
             checKboxTestloop = myView.findViewById(R.id.checkBoxTestloop);
//            checKboxTestloop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
//                @Override
//                public void onCheckedChanged(CompoundButton buttonView,
//                                             boolean isChecked) {
//                    if(isChecked){
//
//                    }else{
//
//                    }
//                }
//            });

            txtPhoneModel.setText("手机:");
            txtphoneOS.setText("系统:");
            txtLon.setText("经度:");
            txtLat.setText("纬度:");
            txtSiteCount.setText("卫星:0");
            txtnetType.setText("网络类型:");
            txtSignalType.setText("信号类型:");

            txt4gStrength.setText("待测");
            txt4gQuality.setText("待测");
            txtWIFIStrength.setText("待测");
            txtWIFIQuality.setText("待测");
            pro4gStrength.setProgress(0);
            pro4gQuality.setProgress(0);
            proWIFIStrength.setProgress(0);
            proWIFIQuality.setProgress(0);
        }


        txtNetSpeed.setText("待测,0.00 Kb/s");

        txtVideo.setText("待测,0.00 Kb/s");
        txtHTMLPage.setText("待测,0 ms");




        proNetSpeed.setProgress(0);
        proVideo.setProgress(0);
        proHTMLPage.setProgress(0);

        //checKbox.setChecked(true);
        txtScore.setText("待测试");

    }

    //主进程发来最新PhoneInfo
    public void OnRecivePhoneInfo(PhoneInfo pi) {
        try {
            if (pi == null) {
                return;
            }

            if (!flagGetFristPI) {
                btnOneKeyTest.setText("一键测试");
                btnOneKeyTest.setEnabled(true);
                flagGetFristPI = true;
            }
            this.pi = pi;
            txt4gInfo.setText("无线信号 (RSRP: "+pi.RSRP+" dBm , SINR: "+pi.SINR+" dB)");
            Log.i("FrmOnRecivePhoneInfo","pi.isScreenOn="+pi.isScreenOn);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String time = simpleDateFormat.format(date);
            txtPhoneModel.setText("手机:" + pi.phoneModel);
            txtphoneOS.setText("系统:" + pi.phoneOS);

            txtnetType.setText("网络类型:" + pi.netType);
            txtSignalType.setText("信号类型:" + pi.sigNalType);

            oneKeyTestInfo.net4gStrengthScore = scoreHelper.GetRSRPScore(pi.RSRP);
            oneKeyTestInfo.net4gQualityScore = scoreHelper.GetSINRScore(pi.SINR);

            if ("WiFi".equals(pi.netType)) {
                if (isWifiConnect()) {
                    int wifiScore = GetWiFiScore();
                    oneKeyTestInfo.wifiStrengthScore = scoreHelper.GetWifiStrengthScore(wifiScore);
                    oneKeyTestInfo.wifiQualityScore = scoreHelper.GetWifiStrengthScore(wifiScore);
                    oneKeyTestInfo.SetIsWiFi(true);
                } else {
                    oneKeyTestInfo.SetIsWiFi(false);
                }
            } else {
                oneKeyTestInfo.SetIsWiFi(false);
            }
            if (qoEVideoInfo != null) {

            }
            OneKeyTestInfo2UI();
        } catch (Exception e) {

        }
    }
    //判断wifi是否连接
    public boolean isWifiConnect() {
        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }
    //给wifi信号打分
    public int GetWiFiScore() {
        if (isWifiConnect()) {
            WifiManager mWifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            int wifi = mWifiInfo.getRssi();//获取wifi信号强度
            if (wifi >= -50 && wifi <= 0) {//最强
                return 5;
            } else if (wifi >= -70 && wifi <= -50) {//较强
                return 4;
            } else if (wifi >= -80 && wifi <= -70) {//较弱
                return 3;
            } else if (wifi >= -100 && wifi <= -80) {//微弱
                return 2;
            } else if (wifi < -100) {//微弱
                return 1;
            }
        } else {
            return 1;
        }
        return 1;
    }

    //一键测试物理开始
    private void OneKeyTest() {
        autoTestLoop = checKboxTestloop.isChecked();
        if(autoTestLoop){
            if(!isWifiConnect()){
                //正在使用流量循环测试
                alertDialog2.show();
                return;
            }
        }
        btnOneKeyTest.setEnabled(false);
        btnOneKeyTest.setText("正在测试……");
        iniTVS(false);
        TestWork();
    }

    //将测试结果展现到UI
    private void OneKeyTestInfo2UI() {
        if (oneKeyTestInfo == null) return;
        txt4gStrength.setText(oneKeyTestInfo.net4gStrengthScore.scoreName);
        txt4gQuality.setText(oneKeyTestInfo.net4gQualityScore.scoreName);
        txtWIFIStrength.setText(oneKeyTestInfo.wifiStrengthScore.scoreName);
        txtWIFIQuality.setText(oneKeyTestInfo.wifiQualityScore.scoreName);

//        txtNetSpeed.setText(oneKeyTestInfo.netSpeedScore.scoreName);
//        txtVideo.setText(oneKeyTestInfo.videoScore.scoreName);
//        txtHTMLPage.setText(oneKeyTestInfo.htmlPageScore.scoreName);

        pro4gStrength.setProgress(oneKeyTestInfo.net4gStrengthScore.progress);
        pro4gQuality.setProgress(oneKeyTestInfo.net4gQualityScore.progress);
        proWIFIStrength.setProgress(oneKeyTestInfo.wifiStrengthScore.progress);
        proWIFIQuality.setProgress(oneKeyTestInfo.wifiQualityScore.progress);

//        proNetSpeed.setProgress(oneKeyTestInfo.netSpeedScore.progress);
//        proVideo.setProgress(oneKeyTestInfo.videoScore.progress);
//        proHTMLPage.setProgress(oneKeyTestInfo.htmlPageScore.progress);

    }

    private long perSecondDownSize;
    private long totalDownSize;
    private int downSecond;
    private boolean flagDownComplete;
    private long httpResponseTime;
    //测试工作
    private void TestWork() {
        oneKeyTestInfo=new OneKeyTestInfo();
        oneKeyTestInfo.pi=pi;
        flagMissionAbort=false;
        perSecondDownSize = 0;
        totalDownSize = 0;
        downSecond = 0;
        flagDownComplete = false;
        txtNetSpeed.setTextColor(getResources().getColor(R.color.red));
        txtVideo.setTextColor(getResources().getColor(R.color.black));
        txtHTMLPage.setTextColor(getResources().getColor(R.color.black));
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetSpeedTest();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                NetSpeedUI();
            }
        }).start();
    }

    //测试完毕，开始综合评分
    private void TestComplete() {
        String togetherScoreString = scoreHelper.GetTogetherScoreString(oneKeyTestInfo);
        int totalScore=oneKeyTestInfo.netSpeedScore.score+oneKeyTestInfo.videoScore.score+oneKeyTestInfo.htmlPageScore.score;
        totalScore=(int)(totalScore/3);
        oneKeyTestInfo.togetherScore.score=totalScore;

        txtScore.setText(togetherScoreString);
        btnOneKeyTest.setText("一键测试");
        btnOneKeyTest.setEnabled(true);
    }
    private void UploadOneKeyTest(){
        Log.i("OneKeyTestInfo","UploadOneKeyTest");
        UploadDataHelper uploadDataHelper= UploadDataHelper.getInstance();
        uploadDataHelper.UploadObjectToServer(oneKeyTestInfo,"OneKeyTestInfo");
    }

    //网速测试UI层
    private void NetSpeedUI() {
        try {
            long startMillisecond = System.currentTimeMillis();
            int slowlySpeedCount=0;
            while (true) {
                try {
                    if(flagMissionAbort){
                        Message msg = new Message();
                        msg.what = -1; //告诉handler 网络有问题，退出测试
                        handler.sendMessage(msg);
                        return;
                    }
                    long perSpeed = perSecondDownSize;
                    netSpeed= DeviceHelper.GetNetSpeed(perSpeed);
                    perSecondDownSize = 0;
                    long myspeed=perSpeed / 1024;
                    Log.i(TAG, "实时下载速度:" + myspeed+ " Kb/s");
                    if(myspeed<10){
                        slowlySpeedCount++;
                        if(slowlySpeedCount>slowlySpeedCountMax){
                            Log.i(TAG,"连续网速慢，退出测试");
                            flagMissionAbort=true;
                        }
                    }else{
                        slowlySpeedCount=0;
                    }
                    totalDownSize = totalDownSize + perSpeed;
                    final ScoreInfo si = scoreHelper.GetNetSpeedProgress(perSpeed);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            proNetSpeed.setProgress(si.progress);
                            String txt=si.scoreName;
                            if("".equals(netSpeed)==false){
                                txt=txt+","+netSpeed;
                            }
                            txtNetSpeed.setText(txt);
                        }
                    });
                    if (flagDownComplete) {
                        Log.i(TAG, "网速测试完成");
                        downSecond = (int) ((System.currentTimeMillis() - startMillisecond) / 1000);
                        if (downSecond == 0) downSecond = 1;
                        final long avgSpeed = totalDownSize / downSecond;
                        Log.i(TAG, "总下载:" + totalDownSize);
                        Log.i(TAG, "耗时:" + downSecond + " s");
                        Log.i(TAG, "平均速度:" + (int) avgSpeed / 1024 + " Kb/s");
                        final ScoreInfo siAvg = scoreHelper.GetNetSpeedProgress(avgSpeed);
                        oneKeyTestInfo.netSpeedScore = siAvg;
                        oneKeyTestInfo.netSpeedTestSpeed= (int) (avgSpeed / 1024);
                        Message msg = new Message();
                        msg.what = 1; //告诉handler 网速测试完毕
                        handler.sendMessage(msg);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                proNetSpeed.setProgress(siAvg.progress);
                                String txt=si.scoreName+","+DeviceHelper.GetNetSpeed(avgSpeed);
                                txtNetSpeed.setText(txt);
                                txtNetSpeed.setTextColor(getResources().getColor(R.color.black));
                            }
                        });
                        return;
                    } else {
                        downSecond++;
                    }
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
            }
        } catch (Exception e) {

        }
    }

    //网速测试OKHTTP下载层
    private void NetSpeedTest() {
        final String url = testUrl;
        // final String url = "http://123.207.31.37:8082/update/PublicTest/PublicTest.apk";
        try {
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Encoding", "identity")
                    .build();
            final long startTime = System.currentTimeMillis();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    flagMissionAbort=true;
                    Log.i(TAG, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream is = null;//输入流
                    try {
                        httpResponseTime = System.currentTimeMillis() - startTime;
                        is = response.body().byteStream();//获取输入流
                        long total = response.body().contentLength();//获取文件大小
                        Log.i(TAG, "文件大小=" + total);
                        if (is != null) {
                            byte[] buf = new byte[1024];
                            int ch = -1;
                            int process = 0;
                            while ((ch = is.read(buf)) != -1) {
                                //ch为真实下载大小
                                perSecondDownSize = perSecondDownSize + ch;
                            }
                        } else {
                            Log.i(TAG, "response.body().byteStream() is null");
                        }
                        flagDownComplete = true;
                        is.close();
                    } catch (Exception e) {
                        flagMissionAbort=true;
                        flagDownComplete = true;
                    } finally {
                        try {
                            if (is != null)
                                is.close();
                        } catch (IOException e) {
                        }

                    }
                }
            });
        } catch (Exception e) {
            flagMissionAbort=true;
        }

    }

    //视频测试
    private void VideoTest() {
        perSecondDownSize = 0;
        totalDownSize = 0;
        downSecond = 0;
        flagDownComplete = false;
        final String url = testUrl;
        final long perSecondSpeedMin = videoFileSize / videoSecond;
        txtVideo.setTextColor(getResources().getColor(R.color.red));

        new Thread(new Runnable() {
            @Override
            public void run() {
                NetSpeedTest();
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int stallCount = 0;
                    qoEVideoInfo = new QoEVideoInfo(pi);
                    qoEVideoInfo.STARTTIME = GetSystemTime();
                    qoEVideoInfo.PHONE_ELECTRIC_START=pi.PHONE_ELECTRIC;
                    int packageIndex = -1;
                    long startMillisecond = System.currentTimeMillis();
                    long firstBufferMinSize = perSecondSpeedMin * 3;
                    long bufferedAndUnPlayTime = 0;
                    int sleepTime = 500;
                    int playedHalfSecond = 0;
                    boolean isAddPlaySecond = true;
                    long lastStallStartTime = System.currentTimeMillis();
                    boolean isStalling = false;
                    long stallStartTime = 0;
                    long stalledTime = 0;
                    long stallTotalTime = 0;
                    int workCount = 0;
                    long threeSecondDownSize = 0;
                    int slowlySpeedCount=0;
                    while (true) {
                        try {
                            if(flagMissionAbort){
                                Message msg = new Message();
                                msg.what = -1; //告诉handler 网络有问题，退出测试
                                handler.sendMessage(msg);
                                return;
                            }
                            workCount++;
                            if (pi != null)
                                qoEVideoInfo.CELL_SIGNAL_STRENGTHList.add((int) pi.RSRP);
                            long perSpeed = perSecondDownSize;
                            perSecondDownSize = 0;
                            threeSecondDownSize = threeSecondDownSize + perSpeed;
                            totalDownSize = totalDownSize + perSpeed;
                            boolean isLastPackage = false;
                            if (totalDownSize == videoFileSize) isLastPackage = true;
                            if (flagDownComplete) isLastPackage = true;
                            if (totalDownSize >= firstBufferMinSize) {
                                packageIndex++;
                            }
                            if (packageIndex == 0) {
                                //首次缓冲完成
                                qoEVideoInfo.VIDEO_PEAK_DOWNLOAD_SPEED = (perSpeed * 2) / 1024;
                                qoEVideoInfo.VIDEO_BUFFER_INIT_TIME = System.currentTimeMillis() - startMillisecond;
                                Log.i(TAG, "首次缓冲完成，耗时:" + qoEVideoInfo.VIDEO_BUFFER_INIT_TIME + ",首次下载量:" + totalDownSize);
                            }
                            perSpeed = perSpeed * 2; //由于500毫秒采集一次，所以速度换算到秒需要*2
                            long thisDownBufferTime = perSpeed / perSecondSpeedMin;
                            Log.i(TAG, "thisDownBufferTime:" + thisDownBufferTime);
                            bufferedAndUnPlayTime = bufferedAndUnPlayTime + thisDownBufferTime - sleepTime;
                            if (bufferedAndUnPlayTime > 0) { //大于0才能播放
                                playedHalfSecond++;
                                if (isStalling) {
                                    //之前还在卡顿状态，现在恢复播放
                                    stallTotalTime = stallTotalTime + stalledTime;
                                    QoEVideoInfo.STALLInfo stallInfo = qoEVideoInfo.new STALLInfo(stallStartTime, stalledTime);
                                    qoEVideoInfo.STALLlist.add(stallInfo);
                                }
                                isStalling = false;
                                stallStartTime = 0;
                                stalledTime = 0;
                            }
                            if (!isLastPackage && packageIndex > 0 && bufferedAndUnPlayTime == 0) {
                                //此处是卡顿点
                                if (isStalling) {
                                    stalledTime = stalledTime + sleepTime;
                                } else {
                                    stallCount++;
                                    isStalling = true;
                                    int playTime = playedHalfSecond / 2;
                                    stallStartTime = playTime;
                                    stalledTime = sleepTime;
                                }
                            }
                            //FrmOneKeyTest
                            //UploadQoEVideoInfo
                            long instanSpeed = perSpeed / 1024;
                            Log.i(TAG, "实时视频下载速度:" + instanSpeed + " Kb/s");
                            if(instanSpeed<10){
                                slowlySpeedCount++;

                                if(slowlySpeedCount>slowlySpeedCountMax){
                                    Log.i(TAG,"连续网速慢，退出测试");
                                    flagMissionAbort=true;
                                }
                            }else{
                                slowlySpeedCount=0;
                            }
                            qoEVideoInfo.VIDEO_ALL_PEAK_RATEList.add(instanSpeed); //每秒下载量
                            if (workCount % 6 == 0) {
                                qoEVideoInfo.INSTAN_DOWNLOAD_SPEEDList.add(threeSecondDownSize / 1024);
                                threeSecondDownSize = 0;
                            }
                            final ScoreInfo si = scoreHelper.GetNetSpeedProgress(perSpeed);
                            netSpeed=DeviceHelper.GetNetSpeed(perSpeed);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    int pro = (int) (100 * totalDownSize / videoFileSize);
                                    proVideo.setProgress(si.progress);
                                    String txt=si.scoreName+","+netSpeed;
                                    txtVideo.setText(txt);
                                }
                            });
                            if (flagDownComplete) {
                                Log.i(TAG, "视频测试完成");
                                downSecond = (int) ((System.currentTimeMillis() - startMillisecond) / 1000);
                                if (downSecond == 0) downSecond = 1;
                              final  long avgSpeed = totalDownSize / downSecond;
                                Log.i(TAG, "视频总下载:" + totalDownSize);
                                Log.i(TAG, "视频下载耗时:" + downSecond + " s");
                                Log.i(TAG, "视频下载平均速度:" + (int) (avgSpeed / 1024 )+ " Kb/s");
                                qoEVideoInfo.PHONE_ELECTRIC_END=pi.PHONE_ELECTRIC;
                                qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME = System.currentTimeMillis() - startMillisecond;
                                qoEVideoInfo.VIDEO_AVERAGE_PEAK_RATE = avgSpeed / 1024;
                                if (stallCount < 0) stallCount = 0;
                                qoEVideoInfo.VIDEO_STALL_NUM = stallCount;
                                qoEVideoInfo.VIDEO_STALL_TOTAL_TIME = 0;
                                qoEVideoInfo.VIDEO_STALL_TOTAL_TIME = stallTotalTime;
                                DecimalFormat df = new DecimalFormat("#.00");
                                qoEVideoInfo.VIDEO_STALL_DURATION_PROPORTION = 100 * stallTotalTime / (videoSecond * 1000);
                                if (videoFileSize - totalDownSize > 0)
                                    qoEVideoInfo.PACKET_LOSS = videoFileSize - totalDownSize;
                                qoEVideoInfo.HTTP_RESPONSE_TIME = httpResponseTime;
                                qoEVideoInfo.VIDEO_CLARITY = "360";
                                qoEVideoInfo.VIDEO_TOTAL_TIME = videoSecond * 1000;
                                qoEVideoInfo.VIDEO_PLAY_TOTAL_TIME = qoEVideoInfo.VIDEO_TOTAL_TIME + stallTotalTime;
                                qoEVideoInfo.BVRATE = 100 * qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME / qoEVideoInfo.VIDEO_PLAY_TOTAL_TIME;
                                qoEVideoInfo.FILE_SIZE = videoFileSize;
                                qoEVideoInfo.FILE_NAME = testUrl;
                                qoEVideoInfo.ISPLAYCOMPLETED = 1;
                                qoEVideoInfo.ISUPLOADDATATIMELY = 1;
                                Log.i(TAG, "视频卡顿次数:" + stallCount);
                                final ScoreInfo siAvg = scoreHelper.GetVideoScore(avgSpeed, stallCount, videoSecond);
                                oneKeyTestInfo.videoScore = siAvg;
                                oneKeyTestInfo.videoTestSpeed= (int) (avgSpeed / 1024);
                                Message msg = new Message();
                                msg.what = 2; //告诉handler 视频测试完毕
                                handler.sendMessage(msg);
                                Log.i(TAG, "上传QoEVideoInfo");
                                qoEVideoInfo.BUSINESSTYPE="QoEVideo From OneKeyTest";
                                UpdateLoadQoEVideoInfo(qoEVideoInfo);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        proVideo.setProgress(siAvg.progress);
                                        String txt=siAvg.scoreName+","+DeviceHelper.GetNetSpeed(avgSpeed);
                                        txtVideo.setText(txt);
                                        txtVideo.setTextColor(getResources().getColor(R.color.black));
                                    }
                                });
                                return;
                            } else {
                                downSecond++;
                            }
                            Thread.sleep(sleepTime);
                        } catch (Exception e) {

                        }
                    }
                } catch (Exception e) {

                }
            }
        }).start();
    }
    //获取当前系统时间
    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }
   //上传QoEVideoInfod到服务器
    private void UpdateLoadQoEVideoInfo(QoEVideoInfo qoEVideoInfo) {
        String versionName = APKVersionCodeUtils.getVerName(getContext());
        qoEVideoInfo.APKNAME = "众手测试";
        qoEVideoInfo.APKVERSION = versionName;
        UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
        uploadDataHelper.UploadDataToServer(qoEVideoInfo);
    }
     //HTTP测试
    private void HTMLPageTest() {
//        Message msg = new Message();
//        msg.what = 0;
//        handler.sendMessage(msg);
//        String[] httpTestingUrls = new String[]{
//                "https://www.baidu.com/",
//                "https://www.douyin.com/",
//                "https://www.toutiao.com/",
//                "https://weibo.com/",
//                "https://www.qq.com/",
//                "https://www.163.com/",
//                "https://www.mi.com/",
//                "https://wx.qq.com/",
//                "https://www.taobao.com/",
//                "https://www.jd.com/"
//        };
        String[] httpTestingUrls = new String[]{
                "https://www.baidu.com/",
                "https://www.douyin.com/",
                "https://www.toutiao.com/",
                "https://weibo.com/",
                "https://www.qq.com/"
        };
        QoEHTTPInfo qoEHTTPInfo = new QoEHTTPInfo(pi);
        HTTPTesting(httpTestingUrls, 0, 0, qoEHTTPInfo);
    }
    //单个网页测试
    private void HTTPTesting(String[] httpTestingUrls, int index, long responseTime, final QoEHTTPInfo qoEHTTPInfo) {
        if (httpTestingUrls == null) return;
        if (index < 0) index = 0;
        if (index >= httpTestingUrls.length) return;
        final String[] fhttpTestingUrls = httpTestingUrls;
        final int sumCount = httpTestingUrls.length;
        final String url = httpTestingUrls[index];
        final int fIndex = index;
        final long fResponseTime = responseTime;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int pro = 100 * fIndex / sumCount;
                proHTMLPage.setProgress(pro);
            }
        });
        try {
            final Date startDate = new Date(System.currentTimeMillis());
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build();
            MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
            final Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Accept-Encoding", "identity")
                    .build();
            Call call = okHttpClient.newCall(request);
            final String HTTPTestUrl = url;
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i(TAG, e.getMessage());
                    Message msg = new Message();
                    msg.what = -1; //告诉handler 网络有问题，退出测试
                    handler.sendMessage(msg);
                    return;
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        QoEHTTPInfo tmpQoEHttp = qoEHTTPInfo;
                        final Date endDate = new Date(System.currentTimeMillis());
                        final   long diff = endDate.getTime() - startDate.getTime();
                        QoEHTTPInfo.HTTPTestInfo httpTestInfo = tmpQoEHttp.new HTTPTestInfo();
                        httpTestInfo.URL = HTTPTestUrl;
                        httpTestInfo.RESPONSETIME=diff;
                        InputStream is = null;//输入流
                        is = response.body().byteStream();//获取输入流
                        long total = response.body().contentLength();//获取文件大小
                        total = 0;
                        long startTimeMillis =  startDate.getTime();
                        if (is != null) {
                            byte[] buf = new byte[1024];
                            int ch = -1;
                            int process = 0;
                            while ((ch = is.read(buf)) != -1) {
                                total = total + ch;
                            }
                        }
                        Log.i(TAG, "文件大小=" + total);
                        httpTestInfo.HTMLBUFFERSIZE = total;
                        httpTestInfo.TOTALBUFFERTIME = System.currentTimeMillis() - startTimeMillis;
                        if ((httpTestInfo.TOTALBUFFERTIME) > 0) {
                            double speed = (total * 1000 / httpTestInfo.TOTALBUFFERTIME) / 1024;
                            httpTestInfo.DOWNLOADSPEED = (int) speed;
                            Log.i("UploadQoEHTTPInfo", "UploadQoEHTTPInfo speed=" + httpTestInfo.DOWNLOADSPEED);
                        } else {
                            httpTestInfo.DOWNLOADSPEED = total / 1024;
                        }

                        tmpQoEHttp.HTTPTESTRESULTLIST.add(httpTestInfo);
                        boolean flagIsOver = false;
                        if (fIndex >= (sumCount - 1)) flagIsOver = true;
                        if (flagIsOver) {
                            long nResponseTime = diff + fResponseTime;
                            nResponseTime = nResponseTime / sumCount;
                           // httpTestInfo.RESPONSETIME = nResponseTime;

                            final ScoreInfo siAvg = scoreHelper.GetHTMLPageScore(nResponseTime);
                            oneKeyTestInfo.htmlPageScore = siAvg;
                            final long ttt=nResponseTime;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    proHTMLPage.setProgress(siAvg.progress);
                                    txtHTMLPage.setText(siAvg.scoreName+","+ttt+" ms");
                                    txtHTMLPage.setTextColor(getResources().getColor(R.color.black));
                                }
                            });
                            Message msg = new Message();
                            msg.what = 0;
                            handler.sendMessage(msg);
                            HandleQoEHTTP(tmpQoEHttp);
                        } else {
                            int nIndex = fIndex + 1;
                            long nResponseTime = diff + fResponseTime;
                            HTTPTesting(fhttpTestingUrls, nIndex, nResponseTime, tmpQoEHttp);
                        }
                    } catch (Exception e) {

                        Log.i("UploadQoEHTTPInfo", e.getMessage());
                        Message msg = new Message();
                        msg.what = -1; //告诉handler 网络有问题，退出测试
                        handler.sendMessage(msg);
                        return;
                    }
                }
            });
        } catch (Exception e) {
            Log.i("UploadQoEHTTPInfo", e.getMessage());
            Message msg = new Message();
            msg.what = -1; //告诉handler 网络有问题，退出测试
            handler.sendMessage(msg);
            return;
        }

    }
    //对测试出来的QOEHTTP结果进行总结
    private void HandleQoEHTTP(QoEHTTPInfo qoEHTTPInfo) {
        qoEHTTPInfo.DATETIME = GetSystemTime();
        qoEHTTPInfo.pi.businessType="OneKeyTest";
        long RESPONSETIME = 0;
        long TOTALBUFFERTIME = 0;
        long DNSTIME = 0;
        long DOWNLOADSPEED = 0;
        long WHITESCREENTIME = 0;
        int count = qoEHTTPInfo.HTTPTESTRESULTLIST.size();

        if (count > 0) {
            for (QoEHTTPInfo.HTTPTestInfo httpTestInfo : qoEHTTPInfo.HTTPTESTRESULTLIST) {
                RESPONSETIME=RESPONSETIME+httpTestInfo.RESPONSETIME;
                TOTALBUFFERTIME = TOTALBUFFERTIME + httpTestInfo.TOTALBUFFERTIME;
                DNSTIME = DNSTIME + httpTestInfo.DNSTIME;
                DOWNLOADSPEED = DOWNLOADSPEED + httpTestInfo.DOWNLOADSPEED;
                WHITESCREENTIME = WHITESCREENTIME + httpTestInfo.WHITESCREENTIME;
            }
            qoEHTTPInfo.RESPONSETIME=RESPONSETIME/count;
            oneKeyTestInfo.httpResonseTime=qoEHTTPInfo.RESPONSETIME;
//            ScoreInfo siAvg = scoreHelper.GetHTMLPageScore(qoEHTTPInfo.RESPONSETIME);
//            txtHTMLPage.setText(siAvg.scoreName+","+  qoEHTTPInfo.RESPONSETIME+" ms");       on
            Log.i("nTOTALBUFFERTIME", TOTALBUFFERTIME+"");
            qoEHTTPInfo.TOTALBUFFERTIME = TOTALBUFFERTIME / count;
            qoEHTTPInfo.DNSTIME = DNSTIME / count;
            qoEHTTPInfo.DOWNLOADSPEED = DOWNLOADSPEED / count;
            qoEHTTPInfo.WHITESCREENTIME = WHITESCREENTIME / count;
        }
        UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
        uploadDataHelper.UploadObjectToServer(qoEHTTPInfo, "QoEHTTPInfo");
    }

    private static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
