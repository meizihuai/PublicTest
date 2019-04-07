package com.getinfo.app.uniqoe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.getinfo.app.uniqoe.utils.Config;
import com.getinfo.app.uniqoe.widget.MediaController;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
//QOE视频测试页面  ！！！本页面已经抛弃使用！！！
public class FrmQoETest extends Fragment {
    private View myView;
    private Date startTime;
    private boolean flagAutoPlayOpen = false;
    private TextView txtFlagAutoPlay;
    private View linearLayoutVedio;
    private PhoneInfo pi;
    private String serverURL = "http://111.53.74.132:7062/default.ashx";
    private String videoPath360 = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private String videoPath1080 = "http://221.228.226.23/11/t/j/v/b/tjvbwspwhqdmgouolposcsfafpedmb/sh.yinyuetai.com/691201536EE4912BF7E4F1E2C67B8119.mp4";
    private PLVideoView mVideoView;
    private int mDisplayAspectRatio = PLVideoView.ASPECT_RATIO_FIT_PARENT;
    private TextView mStatInfoTextView;
    private MediaController mMediaController;
    private boolean mIsLiveStreaming;
    private boolean flag_IsBufferingOver = false;
    private int Video_Stall_Num = 0;
    private long Video_Stall_TOTAL_TIME = 0;
    private long playMs = 0;
    private long bufferMs = 0;
    private OnFragmentInteractionListener mListener;
    private VLCTestInfo vlcTestInfo;
    private static final String TAG = "FrmQoETest";
    private Handler handler;
    private Thread loopWorkThread;

    public FrmQoETest() {

    }

    public static FrmQoETest newInstance(String param1, String param2) {
        FrmQoETest fragment = new FrmQoETest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_qo_etest, container, false);
        linearLayoutVedio = myView.findViewById(R.id.linearLayoutVedio);
        txtFlagAutoPlay = myView.findViewById(R.id.txtFlagAutoPlay);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 0) {  //更新FPS等信息
                    Bundle b = msg.getData();
                    String stat = b.getString("stat");
                    mStatInfoTextView.setText(stat);
                }
                if (msg.what == 1) {

                    StartPlayVedio();
                }
                super.handleMessage(msg);
            }
        };
        View layoutBtnStartAutoPlay = myView.findViewById(R.id.layoutBtnStartAutoPlay);
        layoutBtnStartAutoPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("VideoWorker", "OnClick,flagAutoPlayOpen=" + flagAutoPlayOpen);
                if (flagAutoPlayOpen) {
                    StopLoopWorker();
                    txtFlagAutoPlay.setText("开启自动播放");
                    flagAutoPlayOpen = false;
                } else {
                    OpenLoopWorker();
                    txtFlagAutoPlay.setText("关闭自动播放");
                    flagAutoPlayOpen = true;
                }
            }
        });
        vlcTestInfo = new VLCTestInfo(null);
        IniPlayer();
        mIsLiveStreaming = false;
        return myView;
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        Log.i("onHiddenChanged", hidden + "");
//        if (!hidden) {
//            // load data here：fragment可见时执行加载数据或者进度条等
//
//            linearLayoutVedio.setVisibility(View.VISIBLE);
//            mVideoView.setVisibility(View.VISIBLE);
//            mVideoView.start();
//        } else {
//            // fragment is no longer visible：不可见时不执行操作
//            mVideoView.pause();
//            mVideoView.setVisibility(View.INVISIBLE);
//            linearLayoutVedio.setVisibility(View.INVISIBLE);
//
//
//        }
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

    public void OnRecivePhoneInfo(PhoneInfo pi) {
        if (pi == null) return;
        this.pi = pi;
    }
    //开启循环测试
     private void OpenLoopWorker() {
        Log.i("VideoWorker", "OpenLoopWorker");
        // mVideoView.setVisibility(View.VISIBLE);
        StopLoopWorker();
        loopWorkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        StartPlayVedio();
//                        Message msg = new Message();
//                        msg.what = 1;
//                        handler.sendMessage(msg);  //-->映射到Run方法
//                        Log.i("VideoWorker"," handler.sendMessage");
                        Thread.sleep(90 * 1000);
                    } catch (InterruptedException e) {

                        e.printStackTrace();
                    }
                }
            }
        });
        loopWorkThread.start();
    }

    private void StopLoopWorker() {
        //mVideoView.setVisibility(View.INVISIBLE);
        Log.i("VideoWorker", "StopLoopWorker");
        if (loopWorkThread != null) {
            try {
                loopWorkThread.interrupt();
            } catch (Exception e) {

            }
        }
    }

    private void StartPlayVedio() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("VideoWorker", "StartPlayVedio");
                vlcTestInfo = new VLCTestInfo(null);
                flag_IsBufferingOver = false;
                GetFileLength(videoPath360);
                IniPlayer();
                mVideoView.setVideoPath(videoPath360);
                mVideoView.start();
            }
        });

    }

    private void GetFileLength(String url) {
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
            Request request = new Request.Builder()
                    .url(url)
                    .head()
                    .build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.i("hasaki", e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String result = response.header("Content-Length");
                        Log.i(TAG, "Content-Length" + "=" + result);
                        if (result != null) {
                            if (result.equals("") == false) {
                                if (isNumeric(result)) {
                                    vlcTestInfo.file_Len = Integer.parseInt(result);
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {

        }
    }

    private static boolean isNumeric(String str) {
        for (int i = str.length(); --i >= 0; ) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    //初始化播放器
    private void IniPlayer() {  //初始化播放器
        mVideoView = myView.findViewById(R.id.VideoView);

        View loadingView = myView.findViewById(R.id.LoadingView);
        mVideoView.setBufferingIndicator(loadingView);

        View mCoverView = myView.findViewById(R.id.CoverView);
        mVideoView.setCoverView(mCoverView);

        mStatInfoTextView = myView.findViewById(R.id.StatInfoTextView);

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
        mMediaController = new MediaController(myView.getContext(), !mIsLiveStreaming, mIsLiveStreaming);
        mMediaController.setOnClickSpeedAdjustListener(mOnClickSpeedAdjustListener);
        mVideoView.setMediaController(mMediaController);
    }

    //<editor-fold desc="PLVideoView相关">
    @Override
    public void onResume() {
        super.onResume();
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
        mVideoView.stopPlayback();
    }

    public void onClickSwitchScreen(View v) {
        mDisplayAspectRatio = (mDisplayAspectRatio + 1) % 5;
        mVideoView.setDisplayAspectRatio(mDisplayAspectRatio);
        switch (mVideoView.getDisplayAspectRatio()) {
            case PLVideoView.ASPECT_RATIO_ORIGIN:
                //Utils.showToastTips(myView.getContext(), "Origin mode");
                break;
            case PLVideoView.ASPECT_RATIO_FIT_PARENT:
                //Utils.showToastTips(myView.getContext(), "Fit parent !");
                break;
            case PLVideoView.ASPECT_RATIO_PAVED_PARENT:
                //Utils.showToastTips(myView.getContext(), "Paved parent !");
                break;
            case PLVideoView.ASPECT_RATIO_16_9:
                //Utils.showToastTips(myView.getContext(), "16 : 9 !");
                break;
            case PLVideoView.ASPECT_RATIO_4_3:
                //Utils.showToastTips(myView.getContext(), "4 : 3 !");
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
                    Log.i(TAG, "Buffering start: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_BUFFERING_END:
                    Log.i(TAG, "Buffering end: " + extra);
                    break;
                case PLOnInfoListener.MEDIA_INFO_VIDEO_RENDERING_START:
                    //Utils.showToastTips(myView.getContext(), "first video render time: " + extra + "ms");
                    Log.i(TAG, "first video render time= " + extra + "ms");
                    String str = mVideoView.getResponseInfo();
                    Log.i(TAG, "Response: " + str);
                    vlcTestInfo.Video_BUFFER_INIT_TIME = extra;
                    try {
                        JSONObject obj = new JSONObject(str);
                        vlcTestInfo.File_NAME = obj.getString("url");
                        vlcTestInfo.VIDEO_SERVER_IP = obj.getString("ip");
                        if (vlcTestInfo.File_NAME == null) return;
                        if (vlcTestInfo.VIDEO_SERVER_IP == null) return;
                        //vlcTestInfo.responseTime=obj.getLong("responseTime");
                        //  vlcTestInfo.responseCode=obj.getInt("responseCode");
                        // vlcTestInfo.sendBytes=obj.getLong("sendBytes");
                        // vlcTestInfo.receiveBytes=obj.getLong("receiveBytes");
                        // vlcTestInfo.contentType=obj.getString("contentType");
                        vlcTestInfo.VIDEO_CLARITY = "360";
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
            vlcTestInfo.preparedTime = f1;
            long duration = mVideoView.getDuration();
            vlcTestInfo.VIDEO_TOTAL_TIME = duration;
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
                    Log.e(TAG, "IO Error!");
                    return false;
                case PLOnErrorListener.ERROR_CODE_OPEN_FAILED:
                    //Utils.showToastTips(myView.getContext(), "failed to open player !");
                    break;
                case PLOnErrorListener.ERROR_CODE_SEEK_FAILED:
                    //Utils.showToastTips(myView.getContext(), "failed to seek !");
                    return true;
                case PLOnErrorListener.ERROR_CODE_CACHE_FAILED:
                    //Utils.showToastTips(myView.getContext(), "failed to cache url !");
                    break;
                default:
                    //Utils.showToastTips(myView.getContext(), "unknown error !");
                    break;
            }
            return true;
        }
    };

    private void UploadVLCTestInfo() {  //上传测试数据到服务器
        try {
            if (vlcTestInfo.file_Len > 0 && vlcTestInfo.Video_BUFFER_TOTAL_TIME > 0) {
                vlcTestInfo.INSTAN_DOWNLOAD_SPEED = (vlcTestInfo.file_Len / 1024) / (vlcTestInfo.Video_BUFFER_TOTAL_TIME / 1000);
            }
            if (vlcTestInfo.VIDEO_TOTAL_TIME > 0) {
                if (vlcTestInfo.file_Len > 0)
                    vlcTestInfo.BVRate = 100 * vlcTestInfo.Video_BUFFER_TOTAL_TIME / vlcTestInfo.VIDEO_TOTAL_TIME;
            }
            vlcTestInfo.BVRate = GetTfloat(vlcTestInfo.BVRate);
            vlcTestInfo.INSTAN_DOWNLOAD_SPEED = GetTfloat(vlcTestInfo.INSTAN_DOWNLOAD_SPEED);
            float DPRate = 0;
            if (vlcTestInfo.VIDEO_TOTAL_TIME > 0 && vlcTestInfo.Video_BUFFER_TOTAL_TIME > 0) {
                DPRate = vlcTestInfo.Video_Stall_TOTAL_TIME / vlcTestInfo.Video_BUFFER_TOTAL_TIME;
            }
            vlcTestInfo.Video_Stall_Duration_Proportion = GetTfloat(DPRate);
            if (pi != null) {
                vlcTestInfo.setPhoneInfo(pi);
            }
            Log.i(TAG, "New VLCInfo Upload");
            String versionName = APKVersionCodeUtils.getVerName(getContext());
            vlcTestInfo.apkVersion=versionName;
            UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
            uploadDataHelper.UploadDataToServer(vlcTestInfo);
        } catch (Exception e) {

        }

    }


    private float GetTfloat(float f) {
        float num = (float) (Math.round(f * 100)) / 100;
        return num;
    }

    private PLOnCompletionListener mOnCompletionListener = new PLOnCompletionListener() {
        @Override
        public void onCompletion() {
            Log.i(TAG, "Play Completed !");
            //Utils.showToastTips(myView.getContext(), "Play Completed !");
            // UploadVLCTestInfo();
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
            long currentPosition = mVideoView.getCurrentPosition();
            long bufferPosition = vlcTestInfo.VIDEO_TOTAL_TIME * precent / 100;
            if (bufferPosition < currentPosition) {
                vlcTestInfo.Video_Stall_Num = vlcTestInfo.Video_Stall_Num + 1;
            }
            // Log.i(TAG, "onBufferingUpdate: "+precent);
            //  Log.i(TAG, "onBufferingUpdate: " + precent+",currentPosition="+currentPosition);
            //   Log.i(TAG, "bufferPosition: " + bufferPosition+",currentPosition="+currentPosition);
            if (precent == 100) {
                Log.i(TAG, "buffer.percent=100");
                if (!flag_IsBufferingOver) {
                    flag_IsBufferingOver = true;
                    Date endTime = new Date();
                    long number = endTime.getTime() - startTime.getTime();
                    vlcTestInfo.Video_BUFFER_TOTAL_TIME = number;
                    if (vlcTestInfo.Video_BUFFER_TOTAL_TIME > vlcTestInfo.VIDEO_TOTAL_TIME) {
                        vlcTestInfo.Video_Stall_TOTAL_TIME = vlcTestInfo.Video_BUFFER_TOTAL_TIME - vlcTestInfo.VIDEO_TOTAL_TIME;
                    }
                    UploadVLCTestInfo();
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
            mVideoView.setPlaySpeed(0X00010001);
        }

        @Override
        public void onClickFaster() {
            // 0x0002/0x0001 = 2
            mVideoView.setPlaySpeed(0X00020001);
        }

        @Override
        public void onClickSlower() {
            // 0x0001/0x0002 = 0.5
            mVideoView.setPlaySpeed(0X00010002);
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
        if (bitrate > vlcTestInfo.VIDEO_BITRATE) {
            vlcTestInfo.VIDEO_BITRATE = bitrate;
        }
        if (fps > vlcTestInfo.FPS) {
            vlcTestInfo.FPS = fps;
        }
        if (handler != null) {
            Message msg = new Message();
            msg.what = 0;
            Bundle b = new Bundle();
            b.putString("stat", stat);
            msg.setData(b);
            handler.sendMessage(msg);
        }
    }

    //</editor-fold>

}
