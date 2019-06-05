package com.getinfo.app.uniqoe;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.RequiresApi;
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
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.PermissionCheckActivity;
import com.getinfo.app.uniqoe.utils.ScreenRecordUploadHelper;
import com.getinfo.app.uniqoe.utils.Utils;
import com.getinfo.sdk.qoemaster.APKVersionCodeUtils;
import com.getinfo.sdk.qoemaster.DeviceHelper;
import com.getinfo.sdk.qoemaster.GPSHelper;
import com.getinfo.sdk.qoemaster.GetPhoneInfoHelper;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.HTTPHelper;
import com.getinfo.sdk.qoemaster.Interfaces.InterfaceCls;
import com.getinfo.sdk.qoemaster.LightSensorManager;
import com.getinfo.sdk.qoemaster.LocationInfo;
import com.getinfo.sdk.qoemaster.LogHelper;
import com.getinfo.sdk.qoemaster.MyUtils.MyCatchUtils;
import com.getinfo.sdk.qoemaster.NormalResponse;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.getinfo.sdk.qoemaster.QoEVideoSource;
import com.getinfo.sdk.qoemaster.QoEWorker;
import com.getinfo.sdk.qoemaster.Setting;
import com.getinfo.sdk.qoemaster.UploadDataHelper;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//主要 Activity，包含TabPages,QOER数据的上传在此
public class MainActivity extends PermissionCheckActivity implements FrmMainTest.OnFragmentInteractionListener
        , FrmQoEVideoHTML.OnFragmentInteractionListener
        , FrmMe.OnFragmentInteractionListener
        , FrmOneKeyTest.OnFragmentInteractionListener
        , FrmHTTP.OnFragmentInteractionListener {
    private QoEWorker qoeWorker;
    private Activity mActivity;
    private boolean iswakeLock = true;
    private boolean askPermissoned = false;
    private String version = "";
    private int testCount = 0;
    private String token = "928453310";
    private FrmQoEVideoHTML frmQoEVideoHTML;


    private BottomBar bottomBar;
    private PowerManager.WakeLock wakeLock = null;
    private FrmMainTest frmMainTest;


    private FrmOneKeyTest frmOneKeyTest;
    private FrmMe frmMe;
    private FrmHTTP frmHTTP;

    private boolean isSetedImeiAndImsi = false;


    private final int REQUEST_CODE_PERMISSION = 0; //权限获取结果
    private List<String> needPermission;  //需要申请权限列表
    //本app需要的并且可能需要弹窗来获取权限的列表
    //android.permission.REQUEST_INSTALL_PACKAGES  //安装app的权限
    private String[] permissionArray = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION, //定位权限
            Manifest.permission.ACCESS_NETWORK_STATE,  //网络定位权限
            Manifest.permission.READ_PHONE_STATE,  //获取手机状态
            Manifest.permission.ACCESS_WIFI_STATE,  //获取wifi信息
            Manifest.permission.ACCESS_FINE_LOCATION,  //最后一次位置
            Manifest.permission.WRITE_EXTERNAL_STORAGE,  //写文件 权限
            Manifest.permission.READ_EXTERNAL_STORAGE,  //读文件 权限
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

    /*  @Override
      public boolean onKeyDown(int keyCode, KeyEvent event) {
          if (keyCode == KeyEvent.KEYCODE_BACK) {
              // moveTaskToBack(true);
              return true;//不执行父类点击事件
          } else {
              return super.onKeyDown(keyCode, event);
              // return super.dispatchKeyEvent(event);
          }
      }
  */
    private long firstTime;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        long secondTime = System.currentTimeMillis();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ( secondTime - firstTime < 2000) {
              /*  MyTutuApplication.getEditor().clear();
                MyTutuApplication.getEditor().commit();*/
                this.finish();
                System.exit(0);
            } else {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = System.currentTimeMillis();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static final String TAG = "startPage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity=this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //等同于 android:keepScreenOn="true"  让屏幕保持常亮
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    //竖屏
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);  //横屏


        closeAndroidPDialog();
        StatusBarUtil.setColor(this, 0x05ACED, 0);  //设置状态栏颜色，保持沉浸
        try {
            String versionName = APKVersionCodeUtils.getVerName(this);
            version = versionName;
            GlobalInfo.myVersion = version;
            String title = "UniQoE V" + version;
            setTitle(title);
            TextView tvTitle = findViewById(R.id.toolbar_title);
            tvTitle.setText(title);
        } catch (Exception e) {

        }
        iniUIs(); //初始化UI
        Log.i("getScreenSize",getScreenSize(this)+"");

        Log.i("sHA1", sHA1(this)); //打印本app的SHA码
        // 版本判断。当手机系统大于 23(android6.0) 时，才有必要去判断权限是否获取
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            iniPermission();
        }else{
            startWorkAfterPermissionCheck();
        }
    }
    public static float getScreenSize(Context mContext) {


        int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
        float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
        float density = mContext.getResources().getDisplayMetrics().density;
        float xdpi = mContext.getResources().getDisplayMetrics().xdpi;
        float ydpi = mContext.getResources().getDisplayMetrics().ydpi;
        int width = mContext.getResources().getDisplayMetrics().widthPixels;
        int height = mContext.getResources().getDisplayMetrics().heightPixels;

        // 这样可以计算屏幕的物理尺寸
        float width2 = (width / xdpi)*(width / xdpi);
        float height2 = (height / ydpi)*(width / xdpi);

        return (float) Math.sqrt(width2+height2);
    }


    private void iniPermission() {
        super.askMultiplePermission(permissionArray, new IPermissionResult() {
            @Override
            public void onSuccess() {
                Log.i("iniPermission","success");
                startWorkAfterPermissionCheck();
            }

            @Override
            public void onFail(List<String> failPermissions) {
                String str="";
                for(String itm:failPermissions){
                    str=str+itm+";";
                }
                Log.i("iniPermission","Fail: "+str);
                Utils.MsgBox(mActivity,"部分权限未同意，可能影响本APP正常运行",1);
            }
        });
    }

    @Override
    protected void onResume() {
        Log.i("Mainactivity", "onResume");
        // acquireWakeLock();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i("Mainactivity", "onPause");
//        if (wakeLock != null) {
//            wakeLock.release();
//        }
        //  android.os.Process.killProcess(android.os.Process.myPid());
        super.onPause();
    }

    private void closeAndroidPDialog() {
        try {
            Class aClass = Class.forName("android.content.pm.PackageParser$Package");
            Constructor declaredConstructor = aClass.getDeclaredConstructor(String.class);
            declaredConstructor.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread");
            declaredMethod.setAccessible(true);
            Object activityThread = declaredMethod.invoke(null);
            Field mHiddenApiWarningShown = cls.getDeclaredField("mHiddenApiWarningShown");
            mHiddenApiWarningShown.setAccessible(true);
            mHiddenApiWarningShown.setBoolean(activityThread, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void closeAll() {
        this.finish();
    }


    //初始化UI 主要初始化 bottomBar
    private void iniUIs() {
//        ImageView imgSetting=findViewById(R.id.imgSetting);
        final Activity that = this;
//        imgSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent=new Intent(that,SettingActivity.class);
//                startActivity(intent);
//            }
//        });
        LinearLayout divSetting = findViewById(R.id.divSetting);
        divSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(that, SettingActivity.class);
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
                .addItem(FrmHTTP.class,
                        "HTTP",
                        R.drawable.frmhttp_before,
                        R.drawable.frmhttp_after)
                .addItem(FrmMe.class,
                        "我的",
                        R.drawable.item3_before,
                        R.drawable.item3_after)
                .build();

        frmQoEVideoHTML = (FrmQoEVideoHTML) bottomBar.getFragment(0);
        frmMainTest = (FrmMainTest) bottomBar.getFragment(1);
        frmOneKeyTest = (FrmOneKeyTest) bottomBar.getFragment(2);
        frmHTTP = (FrmHTTP) bottomBar.getFragment(3);
        frmMe = (FrmMe) bottomBar.getFragment(4);


    }


    @SuppressLint("InvalidWakeLockTag")
    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "PostLocationService");
        if (iswakeLock) {
            wakeLock.acquire();
        }
    }

    //释放设备电源锁
    private void releaseWakeLock() {
        if (null != wakeLock) {
            wakeLock.release();
            wakeLock = null;
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




    //权限申请成功之后开始工作
    private void startWorkAfterPermissionCheck() {
        final Context context = this;

        //数据流量开关
        boolean dataswitch=DeviceHelper.isMobileEnableReflex(this);
        // Log.i("dataswitch","dataswitch="+dataswitch);

        qoeWorker = new QoEWorker();
        qoeWorker.init(this, new InterfaceCls.IQoEWorkerInfo() {
            @Override
            public void onError(String str, String module) {
                if (module.equals("gpsHelper")) {
                    AlertDialog alertDialog2 = new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("[" + module + "]" + str)
                            .setIcon(R.mipmap.ic_launcher)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加"Yes"按钮
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            }).create();
                    alertDialog2.show();
                } else {
                    Utils.MsgBox(context, "[" + module + "]" + str, 1);
                }
            }

            @Override
            public void onGetAID(String aid) {
                frmMe.SetMyAID(aid);
                frmMe.GetMyBonusPoints();
            }

            @Override
            public void onNewLocationInfo(LocationInfo la) {
                Log.i("GSPHelper",new Gson().toJson(la));
                if (frmMainTest != null)
                    frmMainTest.setLocations(la.Lon, la.Lat, la.Accuracy, la.Altitude, la.Speed, la.SatelliteCount);
                if (frmOneKeyTest != null)
                    frmOneKeyTest.setLocations(la.Lon, la.Lat, la.Accuracy, la.Altitude, la.Speed, la.SatelliteCount);
                if (frmQoEVideoHTML != null)
                    frmQoEVideoHTML.setLocations(la.Lon, la.Lat, la.Accuracy, la.Altitude, la.Speed, la.SatelliteCount);
            }

            @Override
            public void onNewPhoneInfo(PhoneInfo pi) {
                frmMainTest.OnRecivePhoneInfo(pi); //激发使用者获取到新的PhoneInfo  下同
                frmQoEVideoHTML.OnRecivePhoneInfo(pi);
                frmOneKeyTest.OnRecivePhoneInfo(pi);
                frmMe.OnRecivePhoneInfo(pi);
            }
        });
        qoeWorker.setApkVersion(version);
        qoeWorker.setApkName("UniQoE-QoEMaster");
        try{
            Thread.currentThread().setUncaughtExceptionHandler(new MyCatchUtils());
            qoeWorker.startWork(600);
        }catch (Exception e){
            e.printStackTrace();
        }


        //仅供调试使用 固定IP
//        Setting setting=GlobalInfo.getSetting(this);
//        if(setting!=null){
//            setting.serverUrl="http://123.207.31.37:7062/default.ashx";
//            GlobalInfo.setSetting(this,setting);
//        }

        if (frmMe != null) {
            frmMe.init();
        }else{
            Log.i("startActivity","frmMe is null");
        }
        try {
            Log.i("startActivity","开启录屏文件上传监测线程");
            LogHelper.log(this, "开启录屏文件上传监测线程...");
            ScreenRecordUploadHelper.StartWork(this);
        } catch (Exception e) {

        }

    }


    //获取系统时间
    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }



}
