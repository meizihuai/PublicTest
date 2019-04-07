package com.getinfo.app.uniqoe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.AudioRecordHelper;
import com.getinfo.app.uniqoe.utils.DeviceHelper;
import com.getinfo.app.uniqoe.utils.HTTPHelper;
import com.getinfo.app.uniqoe.utils.LightSensorManager;
import com.getinfo.app.uniqoe.utils.LocationInfo;
import com.getinfo.app.uniqoe.utils.LogHelper;
import com.getinfo.app.uniqoe.utils.NormalResponse;
import com.getinfo.app.uniqoe.utils.ScreenRecordUploadHelper;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.internal.http.HttpHeaders;

//主要 Activity，包含TabPages,QOER数据的上传在此
public class MainActivity extends AppCompatActivity implements FrmMainTest.OnFragmentInteractionListener
        , FrmQoEVideoTest.OnFragmentInteractionListener
        , FrmMe.OnFragmentInteractionListener
        , FrmOneKeyTest.OnFragmentInteractionListener
        , FrmAmap.OnFragmentInteractionListener
        , FrmAmapHTML.OnFragmentInteractionListener
        , FrmQoEVideoHTML.OnFragmentInteractionListener {
    boolean isBeta = false;
    boolean islogined = false;
    String betaVerison = "6";
    private String QOER_HTTP_URL = "http://111.53.74.132:7062/default.ashx/?func=Test"; //http://111.53.74.132:7062/default.ashx/?func=Test
    private long QOER_HTTP_Response_Time = 0;
    private long QOER_HTTP_BufferSize = 0;
    private boolean askPermissoned = false;
    private String version = "";
    private int testCount = 0;
    private String token = "928453310";
    private String dirName = "PublicTest";
    private String appName = "PublicTest.apk";
    private double myPhonelon, myPhonelat, myPhoneAccuracy, myPhoneAltitude, myPhoneSpeed;
    private int myPhoneSatelliteCount;
    private BottomBar bottomBar;
    private PowerManager.WakeLock wakeLock = null;
    private FrmMainTest frmMainTest;
    // private FrmQoEVideoTest frmQoEVideoTest;
    private FrmQoEVideoHTML frmQoEVideoHTML;
    private FrmOneKeyTest frmOneKeyTest;
    private FrmMe frmMe;
   // private FrmAmapHTML frmAmapHTML;
    private boolean isSetedImeiAndImsi = false;
    private String serverURL = "http://111.53.74.132:7062/default.ashx";
    private UploadDataHelper uploadDataHelper;
    private GPSHelper gpsHelper;
    private GetPhoneInfoHelper gpi;
    private Handler handler; //通过此handler来获取GPS信息和PhoneInfo
    private final int REQUEST_CODE_PERMISSION = 0; //权限获取结果
    private List<String> needPermission;  //需要申请权限列表
    //本app需要的并且可能需要弹窗来获取权限的列表
    private String[] permissionArray = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION, //定位权限
            Manifest.permission.ACCESS_NETWORK_STATE,  //网络定位权限
            Manifest.permission.READ_PHONE_STATE,  //获取手机状态
            Manifest.permission.ACCESS_WIFI_STATE,  //获取wifi信息
            Manifest.permission.ACCESS_FINE_LOCATION,  //最后一次位置
            Manifest.permission.WRITE_EXTERNAL_STORAGE,  //写文件 权限
            Manifest.permission.READ_EXTERNAL_STORAGE,  //读文件 权限
            Manifest.permission.REQUEST_INSTALL_PACKAGES, //安装app的权限 用于自升级
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, //允许应用程序访问额外的位置提供命令
            Manifest.permission.CHANGE_WIFI_STATE,  //改变wifi状态
            Manifest.permission.RECORD_AUDIO
    };

    //仅供测试使用，打印出本app的SHA，给到高德地图申请ak码使用
    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length() - 1);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // moveTaskToBack(true);
            return true;//不执行父类点击事件
        } else {
            return super.onKeyDown(keyCode, event);
            // return super.dispatchKeyEvent(event);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StatusBarUtil.setColor(this, 0x05ACED, 0);  //设置状态栏颜色，保持沉浸
        try {
            String versionName = APKVersionCodeUtils.getVerName(this);
            version = versionName;
            GlobalInfo.myVersion = version;

            String title = "UniQoE V" + version;
            if (isBeta) {
                title = title + " B" + betaVerison;
            }
            setTitle(title);
            TextView tvTitle = findViewById(R.id.toolbar_title);
            tvTitle.setText(title);
        } catch (Exception e) {

        }
        iniUIs(); //初始化UI
        Log.i("sHA1", sHA1(this)); //打印本app的SHA码
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while(true){
//                    String uuid= DeviceHelper.GetNewUUID();
//                    Log.i("uuid",uuid);
//                    try{
//                        Thread.sleep(1000);
//                    }catch (Exception e){
//
//                    }
//                }
//            }
//        }).start();
        // acquireWakeLock();
        askMultiplePermission();  //开始申请权限
        // TestThread();
    }

    public void closeAll() {
        this.finish();
    }


    //初始化UI 主要初始化 bottomBar
    private void iniUIs() {
//        ImageView imgSetting=findViewById(R.id.imgSetting);
        final Activity that=this;
//        imgSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(that,SettingActivity.class);
//                startActivity(intent);
//            }
//        });
        LinearLayout divSetting=findViewById(R.id.divSetting);
        divSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(that,SettingActivity.class);
                startActivity(intent);
            }
        });
        bottomBar = findViewById(R.id.bottom_bar);
        bottomBar.setContainer(R.id.fl_container)
                .setTitleBeforeAndAfterColor("#999999", "#ff5d5e")
                .addItem(FrmQoEVideoHTML.class,
                        "首页",
                        R.drawable.item1_before,
                        R.drawable.item1_after)
                .addItem(FrmMainTest.class,
                        "网络",
                        R.drawable.item2_before,
                        R.drawable.item2_after)
                .addItem(FrmOneKeyTest.class,
                        "测试",
                        R.drawable.item4_before,
                        R.drawable.item4_after)
//                .addItem(FrmAmapHTML.class,
//                        "地图",
//                        R.drawable.amap_before,
//                        R.drawable.amap_after)
                .addItem(FrmMe.class,
                        "我的",
                        R.drawable.item3_before,
                        R.drawable.item3_after)
                .build();

        frmQoEVideoHTML = (FrmQoEVideoHTML) bottomBar.getFragment(0);
        frmMainTest = (FrmMainTest) bottomBar.getFragment(1);
        frmOneKeyTest = (FrmOneKeyTest) bottomBar.getFragment(2);
//        frmAmapHTML = (FrmAmapHTML) bottomBar.getFragment(3);
        frmMe = (FrmMe) bottomBar.getFragment(3);

        iniOthers();
    }

    //用于测试守护进程
    private void TestThread() {
        final String tag = "ThreadDaemon";
        Thread workThread;
        ThreadDaemon threadDaemon = new ThreadDaemon(5);
        final ThreadDaemon.WorkFlag workFlag = threadDaemon.new WorkFlag();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.i(tag, "=============================Work Start=============================");
            }
        };
        workThread = new Thread(runnable);
        workThread.start();
        threadDaemon.setWorkFlag(workFlag);
        threadDaemon.setWorkThread(workThread);
        threadDaemon.setRunnable(runnable);
        threadDaemon.startWatching();
    }

    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        if (null == wakeLock) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
            if (null != wakeLock) {
                wakeLock.acquire();
            }
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    //开始循环ping百度官网
    private void StartPing() {
        final String pingbaidu = "pingthread";
        Log.i(pingbaidu, "start pingthread");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HTTPHelper.GetH(serverURL + "?func=GetPingIP", new HTTPHelper.HTTPResponse() {
                        @Override
                        public void OnNormolResponse(NormalResponse np) {
                            if (np.result) {
                                try {
                                    String address = "www.baidu.com";
                                    address = np.data.toString();
                                    Log.i(pingbaidu, "address=" + address);
                                    String pingComm = "ping -s 512 -i 3 " + address + "";
                                    pingComm = "ping -i 3 -s 512 -t 255 " + address;
                                    Process process = Runtime.getRuntime().exec(pingComm);
                                    InputStreamReader r = new InputStreamReader(process.getInputStream());
                                    LineNumberReader returnData = new LineNumberReader(r);
                                    String returnMsg = "";
                                    String line = "";
                                    while ((line = returnData.readLine()) != null) {
                                        Log.i(pingbaidu, line);
                                        returnMsg += line;
                                        if ("".equals(line) == false) {
                                            String[] st = line.split(" ");
                                            for (String s : st) {
                                                if ("".equals(s) == false) {
                                                    if (s.contains("=")) {
                                                        String key = s.split("=")[0];
                                                        String value = s.split("=")[1];
                                                        if ("time".equals(key)) {
                                                            GlobalInfo.PING_AVG_RTT = Float.parseFloat(value);
                                                            Log.i(pingbaidu, GlobalInfo.PING_AVG_RTT + "");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (returnMsg.indexOf("100% loss") != -1) {
                                        System.out.println("与 " + address + " 连接不畅通.");
                                    } else {
                                        System.out.println("与 " + address + " 连接畅通.");
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    });
                    //ping www.huawei.com -t -l 1


                } catch (Exception e) {
                    Log.i(pingbaidu, e.toString());
                }
            }
        }).start();
    }

    //处理handler接收到的新的经纬度，主要用于判断位移是否大于10米，若真，则立马上传一个新的QOER数据
    private void HandleNewGps(double lon, double lat) {
        double oldLon = myPhonelon;
        double oldlat = myPhonelat;
        double dis = GetGis(oldLon, oldlat, lon, lat);
        Log.i("GPSDistance", "dis=" + dis);
        if (dis >= 10) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (gpi != null) {
                        PhoneInfo pi = gpi.GetPhoneInfo();
                        UploadLoadDataToServer(pi);
                        Log.i("GPSDistance", "dis>10 upload QOER");
                    }
                }
            }).start();
        }
    }

    //经纬度计算距离
    private double GetGis(double lon1, double lat1, double lon2, double lat2) {
        if (lon1 == 0 || lat1 == 0 || lon2 == 0 || lat2 == 0) {
            return 0;
        }
        double R = 6378137.0;
        //将角度转化为弧度
        double radLat1 = (lat1 * Math.PI / 180.0);
        double radLat2 = (lat2 * Math.PI / 180.0);
        double radLog1 = (lon1 * Math.PI / 180.0);
        double radLog2 = (lon2 * Math.PI / 180.0);
        //纬度的差值
        double a = radLat1 - radLat2;
        //经度差值
        double b = radLog1 - radLog2;
        //弧度长度
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        //获取长度
        s = s * R;
        //返回最接近参数的 long。结果将舍入为整数：加上 1/2
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    //初始化handler
    private void iniOthers() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {  //GPSHelper上报经纬度
                    Log.i("GPSHelper上报", "上报经纬度到Handler");
                    Bundle b = msg.getData();
                    double lon = b.getDouble("lon");
                    double lat = b.getDouble("lat");
                    HandleNewGps(lon, lat);
                    myPhonelon = lon;
                    myPhonelat = lat;
                    myPhoneAccuracy = b.getDouble("accuracy"); //精度
                    myPhoneAltitude = b.getDouble("altitude"); //海拔
                    myPhoneSpeed = b.getDouble("speed");   //速度
                    myPhoneSatelliteCount = b.getInt("satelliteCount"); //卫星数
                    LocationInfo locationInfo = new LocationInfo();
                    locationInfo.Lon = lon;
                    locationInfo.Lat = lat;
                    locationInfo.Accuracy = myPhoneAccuracy;
                    locationInfo.Altitude = myPhoneAltitude;
                    locationInfo.Speed = myPhoneSpeed;
                    locationInfo.SatelliteCount = myPhoneSatelliteCount;
                    GlobalInfo.setLocationInfo(locationInfo);
                    if (frmMainTest != null)
                        frmMainTest.setLocations(myPhonelon, myPhonelat, myPhoneAccuracy, myPhoneAltitude, myPhoneSpeed, myPhoneSatelliteCount);
                    if (frmOneKeyTest != null)
                        frmOneKeyTest.setLocations(myPhonelon, myPhonelat, myPhoneAccuracy, myPhoneAltitude, myPhoneSpeed, myPhoneSatelliteCount);
                    if (frmQoEVideoHTML != null)
                        frmQoEVideoHTML.setLocations(myPhonelon, myPhonelat, myPhoneAccuracy, myPhoneAltitude, myPhoneSpeed, myPhoneSatelliteCount);
                }
                if (msg.what == 1) {  //GetPhoneInfoHelper上报手机以及网络信息
                    Log.i("GetPhoneInfoHelper上报", "GetPhoneInfoHelper上报手机以及网络信息");
                    Bundle b = msg.getData();
                    String json = b.getString("PhoneInfo");
                    OnRecivePhoneInfo(json);
                }
                if (msg.what == 2) {  //GetPhoneInfoHelper上报手机以及网络信息
                    Log.i("GetPhoneInfoHelper上报", "GPS开关没打开，本APP即将退出");
                    showGPSDialog();

                }
                super.handleMessage(msg);
            }
        };
    }

    private void showGPSDialog() {
        AlertDialog alertDialog2 = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("请打开手机GPS定位开关再打开本APP，谢谢")
                .setIcon(R.mipmap.ic_launcher)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }).create();
        alertDialog2.show();
    }

    //handler接收到新的PhoneInfo,此处将新的PhoneInfo转发到各个使用者
    private void OnRecivePhoneInfo(String phoneInfoJson) {
        if ("".equals(phoneInfoJson)) return;
        try {
            Gson gs = new Gson();
            PhoneInfo pi = gs.fromJson(phoneInfoJson, PhoneInfo.class);
            if (pi == null) return;
            pi.AID=GlobalInfo.AID;
            pi.lon = myPhonelon;
            pi.lat = myPhonelat;
            pi.accuracy = myPhoneAccuracy;
            pi.altitude = myPhoneAltitude;
            pi.gpsSpeed = myPhoneSpeed;
            pi.satelliteCount = myPhoneSatelliteCount;

            pi.businessType = "SDK";
            pi.apkName = "众手测试";
            pi.lon = myPhonelon;
            pi.lat = myPhonelat;
            pi.accuracy = myPhoneAccuracy;
            pi.altitude = myPhoneAltitude;
            pi.gpsSpeed = myPhoneSpeed;
            pi.satelliteCount = myPhoneSatelliteCount;

            pi.apkVersion = version;

            pi.PING_AVG_RTT = GlobalInfo.PING_AVG_RTT;
            pi.DATETIME = GetSystemTime();
            pi.isGPSOpen = isGPSOpen() ? 1 : 0;

            frmMainTest.OnRecivePhoneInfo(pi); //激发使用者获取到新的PhoneInfo  下同
            frmQoEVideoHTML.OnRecivePhoneInfo(pi);
            frmOneKeyTest.OnRecivePhoneInfo(pi);
            frmMe.OnRecivePhoneInfo(pi);
            GlobalInfo.setPi(pi);

            if (!isSetedImeiAndImsi) {

                isSetedImeiAndImsi = true;
                GlobalInfo.setDeviceImeiAndImsi(pi.IMEI, pi.IMSI);
                login(pi.IMSI, pi.IMEI);
            }
            // UploadLoadDataToServer(pi);
        } catch (Exception e) {

        }
    }

    //开启循环播放无声音乐，目的是 防止锁屏   ！！！已弃用！！！
    private void StartMusicPlayLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    PlayMusic();
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
    }

    private void PlayMusic() {
        MediaPlayer mMediaPlayer = MediaPlayer.create(this, R.raw.test);
        // mMediaPlayer.setLooping(true);
        // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.start();
        // mMediaPlayer.setVolume(0, 0);
        //  mMediaPlayer.start();
    }

    private void StartQOEHttpTestLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // QOER_HTTP_URL="";
                while (true) {
                    try {
                        HTTPHelper.TestResponseTime(QOER_HTTP_URL, true, new HTTPHelper.HTTPResponseTime() {
                            @Override
                            public void OnResponseTime(long responseTime, long bufferTotalTime, long bufferTotalSize) {
                                QOER_HTTP_Response_Time = responseTime;
                                QOER_HTTP_BufferSize = bufferTotalSize;
                                Log.i("needGetBufferTotalTime", "totalBufferSize=" + bufferTotalSize);
                            }
                        });
                    } catch (Exception e) {

                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
    }

    //权限申请成功之后开始工作
    private void startWorkAfterPermissionCheck() {

//        if(askPermissoned){
//            Log.i("askPermissoned",askPermissoned+"");
//            GlobalInfo.defaultSetting(this);
//        }
        LogHelper.log(this, "====程序启动，版本号 " + version + "====");
        boolean isHasSimCard = DeviceHelper.hasSimCard(this);
        if (!isHasSimCard) {
            Toast.makeText(getApplicationContext(), "没有监测到SIM卡，请检查SIM卡状态", Toast.LENGTH_LONG).show();
            return;
        }
        GlobalInfo.iniSetting(this);
        LogHelper.log(this, "初始化设置完毕");
        Setting setting = GlobalInfo.getSetting(this);
        LogHelper.log(this, "获取设置完毕");
        UploadDataHelper.getInstance().setServerURL(setting.serverUrl);
        QoEVideoSource.serverURL = setting.serverUrl;
        GlobalInfo.serverUrl = setting.serverUrl;


        LightSensorManager.getInstance().start(this);
        LogHelper.log(this, "开启QoER-HTTP线程...");
        StartQOEHttpTestLoop();
        LogHelper.log(this, "QoER-HTTP线程开启成功");

        //   LogHelper.log(this,"自动切换到第二屏(QOE流媒体测试)...");
//        try{
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    bottomBar.showFrmIndex(1);
//                }
//            });
//
//             LogHelper.log(this,"切换成功");
//        }catch (Exception e){
//            LogHelper.log(this,"切换到第二屏失败，"+e.getMessage());
//        }

//         new Thread(new Runnable() {
//             @Override
//             public void run() {
//                 QoEVideoSource.getVideoSourceFromServer();
//             }
//         }).start();
        //   LogHelper.log(this,"将设置应用到'我的'屏...");
        if (frmMe != null) {
            View view = frmMe.getMyView();
            if (view != null) {
                Switch switchQoEScore = frmMe.getMyView().findViewById(R.id.switchQoESocre);
                Switch switchQoEScreenRecord = frmMe.getMyView().findViewById(R.id.switchQoEScreenRecord);
                Spinner spinner_serverIp = frmMe.getMyView().findViewById(R.id.spinner_serverUrl);
                Spinner spinner_videoType = frmMe.getMyView().findViewById(R.id.spinner_videoType);
                Setting tmpSetting = GlobalInfo.getSetting(this);
                if (tmpSetting != null) {
                    switchQoEScore.setChecked(tmpSetting.switchQoEScore);
                    switchQoEScreenRecord.setChecked(tmpSetting.switchQoEScreenRecord);
                    String serverUrl = tmpSetting.serverUrl;
                    QoEVideoSource.wantType = tmpSetting.videoWantType;
                    for (int i = 0; i < spinner_serverIp.getAdapter().getCount(); i++) {
                        if (serverUrl.equals(spinner_serverIp.getAdapter().getItem(i).toString())) {
                            spinner_serverIp.setSelection(i);
                            break;
                        }
                    }
                    for (int i = 0; i < spinner_videoType.getAdapter().getCount(); i++) {
                        if (tmpSetting.videoWantType.equals(spinner_videoType.getAdapter().getItem(i).toString())) {
                            spinner_videoType.setSelection(i);
                            break;
                        }
                    }

                }
            }
        }
        LogHelper.log(this, "开启Ping线程...");
        StartPing();
        try {
            LogHelper.log(this, "开启录屏文件上传监测线程...");
            ScreenRecordUploadHelper.StartWork(this);
        } catch (Exception e) {

        }
        // PlayMusic();
        // StartMusicPlayLoop();
        LogHelper.log(this, "开启GPSHelper...");
        gpsHelper = new GPSHelper(handler, this);
        gpsHelper.Start();
        LogHelper.log(this, "开启GetPhoneInfoHelper线程...");
        gpi = new GetPhoneInfoHelper(this, handler);
        gpi.StartWork();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(2000);
//                    final PhoneInfo pi = gpi.GetPhoneInfo();
//                    pi.AID=GlobalInfo.AID;
//                    if (pi == null) return;
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (frmMainTest != null) frmMainTest.OnRecivePhoneInfo(pi);
//                            if (frmQoEVideoHTML != null) frmQoEVideoHTML.OnRecivePhoneInfo(pi);
//                            if (frmOneKeyTest != null) frmOneKeyTest.OnRecivePhoneInfo(pi);
//                        }
//                    });
//                } catch (Exception e) {
//
//                }
//            }
//        }).start();
        LogHelper.log(this, "开启QOER线程...");
        StartUploadDataToServer();
        LogHelper.log(this, "======APP启动完毕======");
    }

    private void login(String imsi, String imei) {
        if (islogined) return;
        islogined = true;
        HTTPHelper.GetH(GlobalInfo.serverUrl + "?func=GetAid&imei=" + imei + "&imsi=" + imsi, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                if (np.result) {
                    GlobalInfo.AID = np.data.toString();
                    frmMe.SetMyAID(GlobalInfo.AID);
                    frmMe.GetMyBonusPoints();
                }
            }
        });
    }

    //开启5秒上传一次QOER数据
    private void StartUploadDataToServer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (gpi != null) {
                            PhoneInfo pi = gpi.GetPhoneInfo();
                            pi.AID=GlobalInfo.AID;
                            pi.HTTP_RESPONSE_TIME = QOER_HTTP_Response_Time;
                            pi.HTTP_URL = QOER_HTTP_URL;
                            pi.HTTP_BUFFERSIZE = QOER_HTTP_BufferSize;
                            UploadLoadDataToServer(pi);
                        }
                    } catch (Exception e) {
                        Log.i("UploadLoadDataToServer", "error-->" + e.getMessage());
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
    }

    //获取系统时间
    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }

    //上传QOER数据到服务器
    private void UploadLoadDataToServer(PhoneInfo pi) {
        pi.AID=GlobalInfo.AID;
        pi.businessType = "Report";
        pi.apkName = "众手测试";
        pi.lon = myPhonelon;
        pi.lat = myPhonelat;
        pi.accuracy = myPhoneAccuracy;
        pi.altitude = myPhoneAltitude;
        pi.gpsSpeed = myPhoneSpeed;
        pi.satelliteCount = myPhoneSatelliteCount;
        pi.apkVersion = version;
        pi.PING_AVG_RTT = GlobalInfo.PING_AVG_RTT;
        pi.DATETIME = GetSystemTime();
        pi.isGPSOpen = isGPSOpen() ? 1 : 0;
        GlobalInfo.setPi(pi);
        Log.i("UploadLoadDataToServer","pi.aid="+pi.AID);
        uploadDataHelper = UploadDataHelper.getInstance();
        uploadDataHelper.UploadDataToServer(pi);
    }

    //判断GPS开关状态
    private boolean isGPSOpen() {
        LocationManager locationManager
                = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps;
//        if (gps || network) {
//            return true;
//        }
//        return false;
    }

    //<editor-fold desc="权限操作 Ctrl+ALt+T">
    public void askMultiplePermission() {
        needPermission = new ArrayList<>();
        for (String permissionName :
                permissionArray) {
            if (!checkIsAskPermission(this, permissionName)) {
                needPermission.add(permissionName);
            }
        }

        if (needPermission.size() > 0) {
            //

            ActivityCompat.requestPermissions(this, needPermission.toArray(new String[needPermission.size()]), REQUEST_CODE_PERMISSION);
        } else {
            //获取数据
            startWorkAfterPermissionCheck();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                Map<String, Integer> permissionMap = new HashMap<>();
                for (String name :
                        needPermission) {
                    permissionMap.put(name, PackageManager.PERMISSION_GRANTED);
                }
                for (int i = 0; i < permissions.length; i++) {
                    permissionMap.put(permissions[i], grantResults[i]);
                }
                if (checkIsAskPermissionState(permissionMap, permissions)) {
                    //获取数据
                    startWorkAfterPermissionCheck();
                } else {
                    startWorkAfterPermissionCheck();
                    //提示权限获取不完成，可能有的功能不能使用
                    //Toast.makeText(this, "部分权限未同意，程序功能将会受到影响", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public boolean checkIsAskPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }

    }

    public boolean checkIsAskPermissionState(Map<String, Integer> maps, String[] list) {
        for (int i = 0; i < list.length; i++) {
            if (maps.get(list[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }
    //</editor-fold>
}
