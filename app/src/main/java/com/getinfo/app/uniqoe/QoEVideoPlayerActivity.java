package com.getinfo.app.uniqoe;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.AudioRecordHelper;
import com.getinfo.app.uniqoe.utils.Config;
import com.getinfo.app.uniqoe.utils.DeviceHelper;
import com.getinfo.app.uniqoe.utils.InterfaceCls;
import com.getinfo.app.uniqoe.utils.LocationInfo;
import com.getinfo.app.uniqoe.utils.ScreenRecordUploadHelper;
import com.getinfo.app.uniqoe.utils.ScreenRecorder;
import com.getinfo.app.uniqoe.widget.MediaController;
import com.google.gson.Gson;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLOnAudioFrameListener;
import com.pili.pldroid.player.PLOnBufferingUpdateListener;
import com.pili.pldroid.player.PLOnCompletionListener;
import com.pili.pldroid.player.PLOnErrorListener;
import com.pili.pldroid.player.PLOnInfoListener;
import com.pili.pldroid.player.PLOnPreparedListener;
import com.pili.pldroid.player.PLOnVideoFrameListener;
import com.pili.pldroid.player.PLOnVideoSizeChangedListener;
import com.pili.pldroid.player.widget.PLVideoView;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.PublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

//QoE视频播放界面  使用七牛云的PLVideoView播放器
public class QoEVideoPlayerActivity extends AppCompatActivity {
    private Handler handler;
    private QoEVideoPlayerActivity qoEVideoPlayerActivity;
    private PhoneInfo pi;
    private Object pilock = new Object();
    private double myPhonelon, myPhonelat, myPhoneAccuracy, myPhoneAltitude, myPhoneSpeed;
    private int myPhoneSatelliteCount;
    private static final String TAG = "QoEVideoPlayerTmp";
    private Date startTime;
    private boolean flagAutoPlayOpen = false;
    private String videoPath = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private long videoFileSize = 5510872;
    private Date stallStartTime;
    private Long stallStartTimeInt;
    private boolean isStalling = false;

    private long downloadVideoFileSize = 0;
    private List<Integer> bufferPercentList;
    //3930487
    private int videoSecond = 60;
    private String testUrl = "http://120.78.196.224/DataAnalysis/video1/MT33_30S_240P.mp4";
    private PLVideoView mVideoView;
    private int mDisplayAspectRatio = PLVideoView.ASPECT_RATIO_FIT_PARENT;
    private TextView mStatInfoTextView, myWatchTimes;
    private MediaController mMediaController;
    private boolean mIsLiveStreaming;
    private boolean flag_IsBufferingOver = false;
    private boolean flag_IsBufferInitOVer = false;
    private int Video_Stall_Num = 0;
    private long Video_Stall_TOTAL_TIME = 0;
    private long playMs = 0;
    private long bufferMs = 0;
    private Handler videoHandler;
    private QoEVideoInfo qoEVideoInfo;
    private boolean flag_isPlayOver = false;
    private float mPosX, mPosY, mCurPosX, mCurPosY;
    private String netSpeed = "";
    private long persendBit = 0;

    boolean isScreenRecording = false;//用来标记录屏的状态
    private Intent recordIntant;
    private int recordResultCode;
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;//录制视频的工具
    private int width, height, dpi;//屏幕宽高和dpi，后面会用到
    private String UplanScreenRecordFileName = "";
    private boolean isRecordScreen = false;
    private boolean isInit = false;
    private boolean isUploaded = false;
    private boolean isAskforScreenRecord = false;

    Thread screenRecordThread;//录视频要放在线程里去执行

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qo_evideo_player);
        StatusBarUtil.setColor(this, 0x27292E, 0);
        Intent getIntent = getIntent();
        String piJson = getIntent.getStringExtra("piJson");
        OnRecivePhoneInfo(piJson);
        qoEVideoPlayerActivity=this;
        init();
    }

    private void startRecord() {
        mediaProjectionManager = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        width = outMetrics.widthPixels;
        height = outMetrics.heightPixels;
        dpi = outMetrics.densityDpi;
        //录屏会弹出提示框，用户确认之后，触发onActivityResult
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            intent = mediaProjectionManager.createScreenCaptureIntent();
            isAskforScreenRecord = true;
            startActivityForResult(intent, 101);//正常情况是要执行到这里的,作用是申请捕捉屏幕
        } else {
            Toast.makeText(this, "Android版本太低，无法使用该功能", Toast.LENGTH_SHORT);
        }
        //  mediaProjection = mediaProjectionManager.getMediaProjection(0, this.getIntent());
    }

    private void stopRescord() {
        isScreenRecording = false;
        ScreenRecorder screenRecorder = GlobalInfo.getScreenRecorder();
        if (screenRecorder != null) {
            screenRecorder.stop();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Toast.makeText(this,"requestCode="+requestCode,Toast.LENGTH_SHORT);
        boolean isCanRecord = true;
        if (requestCode == 102) {
            Toast.makeText(this, "缺少读写权限", Toast.LENGTH_SHORT).show();
            isCanRecord = false;
        }
        if (requestCode == 103) {
            Toast.makeText(this, "缺少录音权限", Toast.LENGTH_SHORT).show();
            isCanRecord = false;
        }
        if (requestCode == 104) {
            Toast.makeText(this, "缺少相机权限", Toast.LENGTH_SHORT).show();
            isCanRecord = false;
        }
        if (requestCode != 101) {
            Log.i("HandDrawActivity", "error requestCode =" + requestCode);
            isCanRecord = false;
        }
        if (resultCode != RESULT_OK) {
            // Toast.makeText(this, "捕捉屏幕被禁止", Toast.LENGTH_SHORT).show();
            isCanRecord = false;
            // this.finish();
            // return;
        }
        if (isCanRecord) {
            ScreenRecorder screenRecorder = GlobalInfo.getScreenRecorder();
            if (screenRecorder != null) {
                screenRecorder.stop();
            }
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
            if (mediaProjection != null) {
                screenRecorder = new ScreenRecorder(width, height, mediaProjection, dpi);
            }
            if (mediaProjection != null) {
                screenRecorder = new ScreenRecorder(width, height, mediaProjection, dpi);
            } else {
                Toast.makeText(this, "录制视频工具初始化失败！", Toast.LENGTH_SHORT);
                finish();
                return;
            }
            GlobalInfo.setScreenRecorder(screenRecorder);
            UplanScreenRecordFileName = ScreenRecordUploadHelper.GetNewScreenRecordFileName();
            screenRecordThread = new Thread() {
                @Override
                public void run() {
                    ScreenRecorder screenRecorder = GlobalInfo.getScreenRecorder();
                    screenRecorder.recordFileName = UplanScreenRecordFileName;
                    screenRecorder.startRecorder();
                }
            };
            screenRecordThread.start();
            isScreenRecording = true;
            isRecordScreen = true;
        }
        SubStartPlayVideo();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ScreenRecorder screenRecorder = GlobalInfo.getScreenRecorder();
            isScreenRecording = false;
            if (screenRecorder != null) {
                screenRecorder.abort();
            }
        }
        return super.onKeyDown(keyCode, event);
        // return super.dispatchKeyEvent(event);
    }

    private void setThisPi(PhoneInfo npi) {
        synchronized (pilock) {
            this.pi = npi;
        }

    }

    private PhoneInfo getThisPi() {
        synchronized (pilock) {
            return this.pi;
        }
    }

    private void init() {
        Button btnNext = findViewById(R.id.btnNext);
        myWatchTimes = findViewById(R.id.myWatchTimes);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartPlayVideo();
            }
        });
        videoHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {  //更新FPS等信息
                    Bundle b = msg.getData();
                    String stat = b.getString("stat");
//                    String filter="Kb/s";
//                    String speedTxt=(int)(persendBit*8/1000)+" "+filter;
//                    stat=stat+","+speedTxt;
                    if ("".equals(netSpeed) == false) {
                        stat = stat + "," + netSpeed;
                    }
                    mStatInfoTextView.setText(stat);
                }
                if (msg.what == 1) {
                    // StartPlayVideo();
                }
                super.handleMessage(msg);
            }
        };
        IniPlayer();
        //  setGestureListener();
        mIsLiveStreaming = false;

        StartPlayVideo();


    }

    //设置手势滑动监听器，为后面滑动切换视频做准备
    private void setGestureListener() {
        mMediaController.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                Log.i("GestureHasaki", "onTouch");
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        //return false;
                        mMediaController.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();

                        break;
                    case MotionEvent.ACTION_UP:
                        if (mCurPosY - mPosY > 0
                                && (Math.abs(mCurPosY - mPosY) > 25)) {
                            //向下滑動
                            Log.i("GestureHasaki", "向下");

                        } else if (mCurPosY - mPosY < 0
                                && (Math.abs(mCurPosY - mPosY) > 25)) {
                            //向上滑动
                            Log.i("GestureHasaki", "向上");
                        }

                        break;
                }
                return true;
            }

        });
    }

    //将本地的handler传出去给使用者，让使用者主动上报PhoneInfo和GPS信息
    public Handler getHandler() {
        if (handler == null) {
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (msg.what == 0) {  //GPSHelper上报经纬度
                        // Log.i(TAG,"GPSHelper上报经纬度");
                        Bundle b = msg.getData();
                        myPhonelon = b.getDouble("lon");
                        myPhonelat = b.getDouble("lat");
                        myPhoneAccuracy = b.getDouble("accuracy"); //精度
                        myPhoneAltitude = b.getDouble("altitude"); //海拔
                        myPhoneSpeed = b.getDouble("speed");   //速度
                        myPhoneSatelliteCount = b.getInt("satelliteCount"); //卫星数
                        PhoneInfo newpi = getThisPi();
                        if (newpi != null) {
                            newpi.lon = myPhonelon;
                            newpi.lat = myPhonelat;
                            newpi.accuracy = myPhoneAccuracy;
                            newpi.altitude = myPhoneAltitude;
                            newpi.gpsSpeed = myPhoneSpeed;
                            newpi.satelliteCount = myPhoneSatelliteCount;
                            setThisPi(newpi);
                        }
                        //  Log.i("QoEVideoPlayerTmp","GPS上报myPhonelon="+myPhonelon+",myPhonelat="+myPhonelat);
                    }
                    if (msg.what == 1) {  //GetPhoneInfoHelper上报手机以及网络信息
                        Bundle b = msg.getData();
                        String json = b.getString("PhoneInfo");
                        OnRecivePhoneInfo(json);
                        //  Log.i("QoEVideoPlayerTmp","PhoneInfo上报");
                    }
                }
            };
        }
        return this.handler;
    }

    //接收到新的PhoneInfo数据
    private void OnRecivePhoneInfo(String phoneInfoJson) {
        if ("".equals(phoneInfoJson)) return;
        //  Log.i(TAG,"接收到新的PhoneInfo数据");
        try {
            Gson gs = new Gson();
            PhoneInfo newpi = gs.fromJson(phoneInfoJson, PhoneInfo.class);
            if (newpi == null) return;
            if (myPhonelon != 0 && myPhonelat != 0) {
                newpi.lon = myPhonelon;
                newpi.lat = myPhonelat;
                newpi.accuracy = myPhoneAccuracy;
                newpi.altitude = myPhoneAltitude;
                newpi.gpsSpeed = myPhoneSpeed;
                newpi.satelliteCount = myPhoneSatelliteCount;
            }
            setThisPi(newpi);
            //Log.i("SubStartPlayVideo","On RecivePhoneInfo "+ pi.lon+","+pi.lat);
        } catch (Exception e) {

        }
    }

    //获取系统时间
    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String tmpTime = simpleDateFormat.format(date);
        return tmpTime;
    }

    private void StartPlayVideo() {
        isRecordScreen = false;
        stopRescord();
        if (GlobalInfo.getSetting(this).switchQoEScreenRecord) {
            startRecord();
        } else {
            SubStartPlayVideo();
        }
    }

    //开始播放
    private void SubStartPlayVideo() {

        ScreenRecordUploadHelper.isPlayingVideo = true;
        Log.i(TAG, "startPlayVideo");
        isStalling = false;
        downloadVideoFileSize = 0;
        flag_IsBufferingOver = false;
        flag_isPlayOver = false;
        flag_IsBufferInitOVer = false;

        PhoneInfo gpi = GlobalInfo.getPi();
        if (gpi != null) {
            setThisPi(gpi);
        }
        PhoneInfo mypi = getThisPi();
      //Log.i("SubStartPlayVideo", "-->SubStartPlayVideo " + mypi.lon + "," + mypi.lat);
        qoEVideoInfo = new QoEVideoInfo(mypi);
         Log.i("SubStartPlayVideo", qoEVideoInfo.pi.lon+","+qoEVideoInfo.pi.lat);
        if (isRecordScreen) {
            qoEVideoInfo.SCREENRECORD_FILENAME = UplanScreenRecordFileName;
        } else {
            qoEVideoInfo.SCREENRECORD_FILENAME = "";
        }

        qoEVideoInfo.STARTTIME = GetSystemTime();
        bufferPercentList = new ArrayList<>();
        IniPlayer();
        //  QoEVideoSource.QoEVideoSourceInfo qoEVideoSourceInfo = QoEVideoSource.getNewQoEVideoSourceInfo();

        InterfaceCls.IQoEVideoSource iQoEVideoSource = new InterfaceCls.IQoEVideoSource() {
            @Override
            public void OnNewQoEVideoSourceInfo(final QoEVideoSource.QoEVideoSourceInfo qoEVideoSourceInfo) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (qoEVideoSourceInfo == null) {
                            mStatInfoTextView.setText("视频地址请求失败，请重试");
                            return;
                        }
                        videoPath = qoEVideoSourceInfo.url;
                        Log.i(TAG, "videoPath=" + videoPath);
                        mStatInfoTextView.setText("解析视频地址...");
                        String str = "打分次数 今日:" + qoEVideoSourceInfo.qoe_today_E_times + ";累计:" + qoEVideoSourceInfo.qoe_total_E_times;
                        myWatchTimes.setText(str);

                        videoFileSize = qoEVideoSourceInfo.fileSize;
                        videoSecond = qoEVideoSourceInfo.videoSecond;
                        qoEVideoInfo.FILE_SIZE = videoFileSize;
                        qoEVideoInfo.FILE_NAME = videoPath;
                        qoEVideoInfo.PHONE_ELECTRIC_START = pi.PHONE_ELECTRIC;
                        mVideoView.setVideoPath(videoPath);
                        mVideoView.start();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int flagSecond = 0;
                                long downedSizeSecond = 0;
                                long downedSize3Second = 0;
                                long downedSize100 = 0;
                                int totalSecond = 0;
                                int flag5Second = 0;

                                while (!flag_isPlayOver) {
                                    if (flag_IsBufferInitOVer) {
                                        flagSecond++;
                                        flag5Second++;
                                        if (flag5Second == 50) {
                                            flag5Second = 0;
                                            //5秒级别
                                            PhoneInfo npi = GlobalInfo.getPi();
                                            if (npi != null) {
                                                qoEVideoInfo.NETWORK_TYPEList.add(npi.netType);
                                            }
                                        }
                                        //0.1秒级别
                                        if (pi != null) {
                                            qoEVideoInfo.CELL_SIGNAL_STRENGTHList.add((int) pi.RSRP);
                                        }
                                        long dsize100 = downloadVideoFileSize - downedSize100;
                                        if (dsize100 < 0) {
                                            dsize100 = 0;
                                        } else {
                                            downedSize100 = downloadVideoFileSize;
                                        }
                                        Log.i("dsize100", "dsize100=" + dsize100 + ",downloadVideoFileSize=" + downloadVideoFileSize);
                                        qoEVideoInfo.INSTAN_DOWNLOAD_SPEEDList.add(dsize100);
                                        if (flagSecond == 10) {
                                            totalSecond++;
                                            flagSecond = 0;
                                            //秒级别
                                            long dSize = downloadVideoFileSize - downedSizeSecond;
                                            downedSizeSecond = downloadVideoFileSize;
                                            netSpeed = DeviceHelper.GetNetSpeed(dSize);
                                            persendBit = dSize;
                                            LocationInfo locationInfo = GlobalInfo.getLocationInfo();
                                            qoEVideoInfo.GPSPointList.add(qoEVideoInfo.new GPSPoint(locationInfo.Lon, locationInfo.Lat));
                                            qoEVideoInfo.LIGHT_INTENSITY_list.add(GlobalInfo.getLightIntensity());
                                            qoEVideoInfo.PHONE_SCREEN_BRIGHTNESS_list.add(DeviceHelper.getSystemBrightness(qoEVideoPlayerActivity));
                                            if (pi != null) {
                                                qoEVideoInfo.SIGNALList.add(pi.sigNalInfo);
                                                if (pi.xyZaSpeed != null) {
                                                    qoEVideoInfo.ACCELEROMETER_DATAList.add(pi.xyZaSpeed);
                                                }
                                            }
                                        }
                                    }
                                    try {
                                        Thread.sleep(100);
                                    } catch (Exception e) {

                                    }
                                }
                                long avgSpeed = totalSecond > 0 ? (downloadVideoFileSize / totalSecond) : downloadVideoFileSize;
                                netSpeed = DeviceHelper.GetNetSpeed(avgSpeed);
                                persendBit = avgSpeed;
                            }
                        }).start();
                    }
                });
            }
        };
        mStatInfoTextView.setText("请求视频地址...");
        QoEVideoSource.getNewQoEVideoSourceInfoAsync(iQoEVideoSource);
    }

    //初始化播放器
    private void IniPlayer() {
        mVideoView = findViewById(R.id.VideoView);

        View loadingView = findViewById(R.id.LoadingView);
        mVideoView.setBufferingIndicator(loadingView);

        View mCoverView = findViewById(R.id.CoverView);
        mVideoView.setCoverView(mCoverView);

        mStatInfoTextView = findViewById(R.id.StatInfoTextView);

        // 1 -> hw codec enable, 0 -> disable [recommended]
        int codec = AVOptions.MEDIA_CODEC_SW_DECODE;
        AVOptions options = new AVOptions();
        // the unit of timeout is ms
        options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
        // 1 -> hw codec enable, 0 -> disable [recommended]
        options.setInteger(AVOptions.KEY_MEDIACODEC, codec);
        options.setInteger(AVOptions.KEY_LIVE_STREAMING, mIsLiveStreaming ? 1 : 0);
        boolean disableLog = false;
//        options.setString(AVOptions.KEY_DNS_SERVER, "127.0.0.1");
        options.setInteger(AVOptions.KEY_LOG_LEVEL, disableLog ? 5 : 0);
        boolean cache = false;
        if (!mIsLiveStreaming && cache) {
            options.setString(AVOptions.KEY_CACHE_DIR, Config.DEFAULT_CACHE_DIR);
        }
        boolean vcallback = false;
        if (vcallback) {
            options.setInteger(AVOptions.KEY_VIDEO_DATA_CALLBACK, 1);
        }
        boolean acallback = false;
        if (acallback) {
            options.setInteger(AVOptions.KEY_AUDIO_DATA_CALLBACK, 1);
        }
        if (!mIsLiveStreaming) {
            int startPos = 0;
            options.setInteger(AVOptions.KEY_START_POSITION, startPos * 1000);
        }
        mVideoView.setAVOptions(options);
        //mVideoView.setZOrderOnTop(true); //视频黑频
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        mVideoView.setOnVideoFrameListener(mOnVideoFrameListener);
        mVideoView.setOnAudioFrameListener(mOnAudioFrameListener);
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setLooping(false);
        mMediaController = new MyMediaControl(this, !mIsLiveStreaming, mIsLiveStreaming);
        mMediaController.setOnClickSpeedAdjustListener(mOnClickSpeedAdjustListener);
        //  mMediaController.setVisibility(View.VISIBLE);
        mVideoView.setMediaController(mMediaController);
        mMediaController.setInstantSeeking(false);
        mMediaController.setEnabled(false);
    }

    private class MyMediaControl extends MediaController {
        public MyMediaControl(Context context, boolean useFastForward, boolean disableProgressBar) {
            super(context, useFastForward, disableProgressBar);
        }

        private boolean isShowed = true;

        @Override
        public boolean performClick() {
            return super.performClick();
        }

        @Override
        public void setEnabled(boolean enable) {
            super.setEnabled(false);
        }

        @Override
        public void setInstantSeeking(boolean enable) {
            super.setInstantSeeking(false);
        }
//        @Override
//        public void hide(){
//           Log.i("MyMediaControl","hide isShowing="+super.isShowing());
//        }
//        @Override
//        public  void show(){
//            Log.i("MyMediaControl","show  isShowing="+super.isShowing());
//            super.show();
//           // super.show(999999);
//        }

    }

    //上传测试数据到服务器
    private void UploadQoEVideoInfo() {
        if (isUploaded) return;
        isUploaded = true;
        int step = 0;
        try {
            Log.i(TAG, "UploadQoEVideoInfo");
            Log.i(TAG, "qoEVideoInfo.FILE_NAME=" + qoEVideoInfo.FILE_NAME);
            Log.i(TAG, "qoEVideoInfo.FILE_SIZE=" + qoEVideoInfo.FILE_SIZE);
            Log.i(TAG, "qoEVideoInfo.VIDEO_TOTAL_TIME=" + qoEVideoInfo.VIDEO_TOTAL_TIME);
            Log.i(TAG, "qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME=" + qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME);
            Log.i(TAG, "qoEVideoInfo.VIDEO_STALL_TOTAL_TIME=" + qoEVideoInfo.VIDEO_STALL_TOTAL_TIME);
            Log.i(TAG, "qoEVideoInfo.VIDEO_STALL_NUM=" + qoEVideoInfo.VIDEO_STALL_NUM);
            qoEVideoInfo.MOVE_SPEED = (long) GlobalInfo.getLocationInfo().Speed;
            qoEVideoInfo.PHONE_ELECTRIC_END = pi.PHONE_ELECTRIC;
            qoEVideoInfo.SCREEN_RESOLUTION_WIDTH = DeviceHelper.getPhoneWidth(this);
            qoEVideoInfo.SCREEN_RESOLUTION_LONG = DeviceHelper.getPhoneHeight(this);
            qoEVideoInfo.PHONE_PLACE_STATE = DeviceHelper.isPhoneVertical(this) ? 1 : 2;
            if (qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME == 0)
                qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME = 1000;
            if (qoEVideoInfo.FILE_SIZE > 0 && qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME > 0) {
                double second = qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME / 1000;
                double size = qoEVideoInfo.FILE_SIZE / 1024;
                if (second < 1) {
                    qoEVideoInfo.VIDEO_AVERAGE_PEAK_RATE = (int) size;
                } else {
                    qoEVideoInfo.VIDEO_AVERAGE_PEAK_RATE = (int) (size / second);
                }
                qoEVideoInfo.VIDEO_AVERAGE_PEAK_RATE = qoEVideoInfo.VIDEO_AVERAGE_PEAK_RATE * 1000;
            }
            if (qoEVideoInfo.VIDEO_TOTAL_TIME > 0) {
                if (qoEVideoInfo.FILE_SIZE > 0)
                    qoEVideoInfo.BVRATE = 100 * qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME / qoEVideoInfo.VIDEO_TOTAL_TIME;
            }
            qoEVideoInfo.BVRATE = GetTfloat((float) qoEVideoInfo.BVRATE);
            float DPRate = 0;
            if (qoEVideoInfo.VIDEO_TOTAL_TIME > 0 && qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME > 0) {
                DPRate = qoEVideoInfo.VIDEO_STALL_TOTAL_TIME / qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME;
            }
            qoEVideoInfo.VIDEO_STALL_DURATION_PROPORTION = GetTfloat(DPRate);
            if (pi != null) {
                qoEVideoInfo.setPhoneInfo(pi);
            }
            qoEVideoInfo.VIDEO_PLAY_TOTAL_TIME = qoEVideoInfo.VIDEO_STALL_TOTAL_TIME + qoEVideoInfo.VIDEO_TOTAL_TIME;
            String versionName = APKVersionCodeUtils.getVerName(this);
            qoEVideoInfo.APKNAME = versionName;
            qoEVideoInfo.ENVIRONMENTAL_NOISE = AudioRecordHelper.getNoiseValue() + "";
            qoEVideoInfo.UE_INTERNAL_IP = GlobalInfo.getIPAddress(this);
          //  qoEVideoInfo.LIGHT_INTENSITY = GlobalInfo.getLightIntensity();
            UpdateLoadQoEVideoInfo(qoEVideoInfo);
        } catch (Exception e) {
            Log.i(TAG, "UploadQoEVideoInfo--Err-->" + e.toString() + " step=" + step);
        }
    }

    private void UpdateLoadQoEVideoInfo(QoEVideoInfo qoEVideoInfo) {
//        ScreenRecorder screenRecorder = GlobalInfo.getScreenRecorder();
//        if (screenRecorder != null) {
//            screenRecorder.stop();
//        }
        ScreenRecordUploadHelper.isPlayingVideo = false;
        stopRescord();
        String versionName = APKVersionCodeUtils.getVerName(this);
        if (pi != null) {
            qoEVideoInfo.setPhoneInfo(pi);
        }
        qoEVideoInfo.BUSINESS_TYPE = "QoEVideo";
        qoEVideoInfo.BUSINESSTYPE = "QoEVideo";
        qoEVideoInfo.APKNAME = "众手测试";
        qoEVideoInfo.APKVERSION = versionName;
        String adj_signal = qoEVideoInfo.pi.ADJ_SIGNAL;
        Log.i(TAG, "adj_signal.length=" + adj_signal.length());
        Log.i("UploadQoEVideoInfo", "adj_signal.length=" + adj_signal.length());
        Setting setting = GlobalInfo.getSetting(this);
        boolean isScore = true;
        if (setting != null) {
            isScore = setting.switchQoEScore;
        }
        if (isScore) {
            Gson gson = new Gson();
            String json = gson.toJson(qoEVideoInfo);
            Log.i(TAG, json);
            Log.i("UploadQoEVideo", json);
            QoEVideoScoreActivity qoEVideoScoreActivity = new QoEVideoScoreActivity();
            Intent intent = new Intent(this, qoEVideoScoreActivity.getClass());
            intent.putExtra("QoEVideoInfo", json);
            startActivity(intent);
            // StartPlayVideo();
            // this.finish();
        } else {
            UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
            uploadDataHelper.UploadDataToServer(qoEVideoInfo);
            StartPlayVideo();
        }
    }

    //<editor-fold desc="PLVideoView相关">
    @Override
    public void onResume() {
        super.onResume();
        Log.i("QoEVideoPlayerStart", "onResume");
        if (isInit) {
            Log.i("QoEVideoPlayerStart", "StartPlayVideo");
            if (!isAskforScreenRecord) {
                mVideoView.stopPlayback();
                StartPlayVideo();
            } else {
                isAskforScreenRecord = false;
            }

        } else {
            isInit = true;
        }
        mVideoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMediaController.getWindow().dismiss();
        mVideoView.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        flag_isPlayOver = true;
        mVideoView.stopPlayback();
    }

    public void onClickSwitchScreen(View v) {
        mDisplayAspectRatio = (mDisplayAspectRatio + 1) % 5;
        mVideoView.setDisplayAspectRatio(mDisplayAspectRatio);
        switch (mVideoView.getDisplayAspectRatio()) {
            case PLVideoView.ASPECT_RATIO_ORIGIN:
                //Utils.showToastTips(myView.this, "Origin mode");
                break;
            case PLVideoView.ASPECT_RATIO_FIT_PARENT:
                //Utils.showToastTips(myView.this, "Fit parent !");
                break;
            case PLVideoView.ASPECT_RATIO_PAVED_PARENT:
                //Utils.showToastTips(myView.this, "Paved parent !");
                break;
            case PLVideoView.ASPECT_RATIO_16_9:
                //Utils.showToastTips(myView.this, "16 : 9 !");
                break;
            case PLVideoView.ASPECT_RATIO_4_3:
                //Utils.showToastTips(myView.this, "4 : 3 !");
                break;
            default:
                break;
        }
    }

    private PLOnInfoListener mOnInfoListener = new PLOnInfoListener() {
        @Override
        public void onInfo(int what, int extra) {
            //  Log.i(TAG, "OnInfo, what = " + what + ", extra = " + extra);
            switch (what) {
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_START:
                    //   Log.i(TAG, "Buffering start: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    // Log.i(TAG, "Buffering end: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    //Utils.showToastTips(myView.this, "first video render time: " + extra + "ms");
                    // Log.i(TAG, "first video render time= " + extra + "ms");
                    String str = mVideoView.getResponseInfo();
                    //  Log.i(TAG, "Response: " + str);
                    qoEVideoInfo.VIDEO_BUFFER_INIT_TIME = extra;  //首次缓冲完毕
                    flag_IsBufferInitOVer = true;
                    if (qoEVideoInfo.VIDEO_BUFFER_INIT_TIME > 1000) {
                        double second = qoEVideoInfo.VIDEO_BUFFER_INIT_TIME / 1000;
                        double d = downloadVideoFileSize / second;
                        qoEVideoInfo.VIDEO_PEAK_DOWNLOAD_SPEED = (long) d;
                    } else {
                        qoEVideoInfo.VIDEO_PEAK_DOWNLOAD_SPEED = downloadVideoFileSize;
                    }

                    try {
                        JSONObject obj = new JSONObject(str);
                        qoEVideoInfo.FILE_NAME = obj.getString("url");
                        qoEVideoInfo.FILE_SERVER_IP = obj.getString("ip");
                        qoEVideoInfo.HTTP_RESPONSE_TIME = obj.getLong("responseTime");
//                         qoEVideoInfo.=obj.getInt("responseCode");
//                         qoEVideoInfo.sendBytes=obj.getLong("sendBytes");
//                         qoEVideoInfo.RECI=obj.getLong("receiveBytes");
//                         qoEVideoInfo.contentType=obj.getString("contentType");
                        qoEVideoInfo.VIDEO_CLARITY = "360";
                    } catch (Exception e) {
                        return;
                    }
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_RENDERING_START:
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FRAME_RENDERING:
                    //   Log.i(TAG, "video frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_AUDIO_FRAME_RENDERING:
                    //  Log.i(TAG, "audio frame rendering, ts = " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_GOP_TIME:
                    //  Log.i(TAG, "Gop Time: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_SWITCHING_SW_DECODE:
                    //   Log.i(TAG, "Hardware decoding failure, switching software decoding!");
                    break;
                case PLOnInfoListener.MEDIA_INFO_METADATA:
                    Log.i(TAG, mVideoView.getMetadata().toString());
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_BITRATE:
                case PLOnInfoListener.MEDIA_INFO_VIDEO_FPS:
                    updateStatInfo();
                    break;
                case PLOnInfoListener.MEDIA_INFO_CONNECTED:
                    Log.i(TAG, "Connected !");
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    //   Log.i(TAG, "Rotation changed: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_LOOP_DONE:
                    //  Log.i(TAG, "Loop done");
                    break;
                case PLOnInfoListener.MEDIA_INFO_CACHE_DOWN:
                    Log.i(TAG, "Cache done");
                    break;
                default:
                    break;
            }
        }
    };

    private PLOnPreparedListener mOnPreparedListener = new PLOnPreparedListener() {
        @Override
        public void onPrepared(int preparedTime) {
            Log.i(TAG, "On Prepared ! prepared time = " + preparedTime + " ms");
            long f1 = preparedTime;
            qoEVideoInfo.APP_PREPARED_TIME = f1;
            long duration = mVideoView.getDuration();
            qoEVideoInfo.VIDEO_TOTAL_TIME = duration;
            startTime = new Date();
            Log.i(TAG, "duration= " + duration);
        }
    };
    private PLOnErrorListener mOnErrorListener = new PLOnErrorListener() {
        @Override
        public boolean onError(int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLOnErrorListener.ERROR_CODE_IO_ERROR:
                    /**
                     * SDK will do reconnecting automatically
                     */
                    mStatInfoTextView.setText("播放失败，视频地址无效，地址为" + videoPath);
                    Log.e(TAG, "IO Error!");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    mStatInfoTextView.setText("播放失败，播放器加载失败");
                    //Utils.showToastTips(myView.this, "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    mStatInfoTextView.setText("播放失败，视频播放失败");
                    //Utils.showToastTips(myView.this, "failed to seek !");
                    return true;
                case PLOnErrorListener.ERROR_CODE_CACHE_FAILED:
                    mStatInfoTextView.setText("播放失败，视频缓冲失败");
                    //Utils.showToastTips(myView.this, "failed to cache url !");
                    break;
                default:
                    //Utils.showToastTips(myView.this, "unknown error !");
                    break;
            }
            return true;
        }
    };


    private float GetTfloat(float f) {
        float num = (float) (Math.round(f * 100)) / 100;
        return num;
    }

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            flag_isPlayOver = true;
            //   Utils.showToastTips(myView.this, "Play Completed !");
            UploadQoEVideoInfo();
            if (!mIsLiveStreaming) {
                mMediaController.refreshProgress();
            }

            //return;
        }
    };

    //缓冲进度
    private PLOnBufferingUpdateListener mOnBufferingUpdateListener = new PLOnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(int precent) {
            if (bufferPercentList != null) {
                bufferPercentList.add(precent);
            }
            long currentPosition = mVideoView.getCurrentPosition();
            // Log.i(TAG, "currentPosition="+currentPosition);
            // sumHttpSize+=mVideoView.getHttpBufferSize().longValue();

            long bufferPosition = qoEVideoInfo.VIDEO_TOTAL_TIME * precent / 100;
            BigInteger fileSize = mVideoView.getHttpBufferSize(); //播放器已经缓冲的 byte 数

            downloadVideoFileSize = qoEVideoInfo.FILE_SIZE * precent / 100;

            if (bufferPosition <= currentPosition) {
                if (!isStalling) {
                    //卡顿开始点
                    isStalling = true;
                    stallStartTime = new Date();
                    stallStartTimeInt = currentPosition;
                    qoEVideoInfo.VIDEO_STALL_NUM = qoEVideoInfo.VIDEO_STALL_NUM + 1;
                    Log.i(TAG, "卡顿次数 加1 =" + qoEVideoInfo.VIDEO_STALL_NUM);
                }
            } else {
                if (isStalling) {
                    //卡顿结束点
                    long millsecond = 500;
                    if (stallStartTime != null) {
                        Date endTime = new Date();
                        millsecond = endTime.getTime() - stallStartTime.getTime();
                    }
                    qoEVideoInfo.VIDEO_STALL_TOTAL_TIME += millsecond;
                    QoEVideoInfo.STALLInfo stallInfo = qoEVideoInfo.new STALLInfo(stallStartTimeInt, millsecond);
                    qoEVideoInfo.STALLlist.add(stallInfo);
                    Log.i(TAG, "卡顿结束，本次卡顿=" + millsecond + ",总卡顿时间=" + qoEVideoInfo.VIDEO_STALL_TOTAL_TIME);
                }
                isStalling = false;
            }
            Log.i("bufferpercent", "buffer.percent=" + precent + ",flag_IsBufferingOver=" + flag_IsBufferingOver);
            if (precent == 100) {
                isUploaded = false;
                //  Log.i(TAG, " sumHttpSize="+ sumHttpSize);
                Log.i(TAG, "buffer.percent=100,flag_IsBufferingOver=" + flag_IsBufferingOver);
                if (!flag_IsBufferingOver) {
                    if (bufferPercentList != null) {
                        if (bufferPercentList.size() > 80) {
                            flag_IsBufferingOver = true;
                        }
                    }
                    if (flag_IsBufferingOver) {
                        if (startTime == null) {
                            startTime = new Date();
                        }
                        Log.i(TAG, "startTime=" + startTime.toString());
                        Date endTime = new Date();
                        Log.i(TAG, "endTime=" + endTime.toString());
                        long number = endTime.getTime() - startTime.getTime();
                        if (number == 0) number = 1000;
                        qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME = number;
                        if (qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME > qoEVideoInfo.VIDEO_TOTAL_TIME) {
                            long mathStallTime = qoEVideoInfo.VIDEO_BUFFER_TOTAL_TIME - qoEVideoInfo.VIDEO_TOTAL_TIME;
                            if (qoEVideoInfo.VIDEO_STALL_TOTAL_TIME < mathStallTime) {
                                qoEVideoInfo.VIDEO_STALL_TOTAL_TIME = mathStallTime;
                            }
                        }
                        if (qoEVideoInfo.VIDEO_STALL_TOTAL_TIME > 0) {
                            if (qoEVideoInfo.VIDEO_STALL_NUM == 0) {
                                qoEVideoInfo.VIDEO_STALL_NUM = 1;
                            }
                        }
                    }
                    // UploadQoEVideoInfo();
                }
            }
        }
    };

    private PLOnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLOnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            Log.i(TAG, "onVideoSizeChanged: width = " + width + ", height = " + height);
        }
    };

    private PLOnVideoFrameListener mOnVideoFrameListener = new PLOnVideoFrameListener() {
        @Override
        public void onVideoFrameAvailable(byte[] data, int size, int width, int height, int format, long ts) {
            //   Log.i(TAG, "onVideoFrameAvailable: " + size + ", " + width + " x " + height + ", " + format + ", " + ts);
            if (format == PLOnVideoFrameListener.VIDEO_FORMAT_SEI && bytesToHex(Arrays.copyOfRange(data, 19, 23)).equals("74733634")) {
                // If the RTMP stream is from Qiniu
                // Add &addtssei=true to the end of URL to enable SEI timestamp.
                // Format of the byte array:
                // 0:       SEI TYPE                    This is part of h.264 standard.
                // 1:       unregistered user data      This is part of h.264 standard.
                // 2:       payload length              This is part of h.264 standard.
                // 3-18:    uuid                        This is part of h.264 standard.
                // 19-22:   ts64                        Magic string to mark this stream is from Qiniu
                // 23-30:   timestamp                   The timestamp
                // 31:      0x80                        Magic hex in ffmpeg
                // Log.i(TAG, " timestamp: " + Long.valueOf(bytesToHex(Arrays.copyOfRange(data, 23, 31)), 16));
            }
        }
    };

    private PLOnAudioFrameListener mOnAudioFrameListener = new PLOnAudioFrameListener() {
        @Override
        public void onAudioFrameAvailable(byte[] data, int size, int samplerate, int channels, int datawidth, long ts) {
            //  Log.i(TAG, "onAudioFrameAvailable: " + size + ", " + samplerate + ", " + channels + ", " + datawidth + ", " + ts);
        }
    };

    private MediaController.OnClickSpeedAdjustListener mOnClickSpeedAdjustListener = new MediaController.OnClickSpeedAdjustListener() {
        @Override
        public void onClickNormal() {
            // 0x0001/0x0001 = 2
            //mVideoView.setPlaySpeed(0X00010001);
        }

        @Override
        public void onClickFaster() {
            // 0x0002/0x0001 = 2
            //mVideoView.setPlaySpeed(0X00020001);
        }

        @Override
        public void onClickSlower() {
            // 0x0001/0x0002 = 0.5
            //mVideoView.setPlaySpeed(0X00010002);
        }
    };

    private String bytesToHex(byte[] bytes) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private void updateStatInfo() {
        long bitrate = mVideoView.getVideoBitrate() / 1024;
        int fps = mVideoView.getVideoFps();
        final String stat = bitrate + "kbps, " + fps + "fps";
        if (bitrate > qoEVideoInfo.VIDEO_BITRATE) {
            qoEVideoInfo.VIDEO_BITRATE = (int) bitrate;
        }
        if (fps > qoEVideoInfo.FPS) {
            qoEVideoInfo.FPS = fps;
        }
        if (videoHandler != null) {
            Message msg = new Message();
            msg.what = 0;
            Bundle b = new Bundle();
            b.putString("stat", stat);
            msg.setData(b);
            videoHandler.sendMessage(msg);
        }
    }

    //</editor-fold>
}
