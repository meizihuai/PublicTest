package com.getinfo.app.uniqoe;

import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.ScreenRecorder;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MEDIA_PROJECTION_SERVICE;

//视频测试页面，开始测试 按钮
public class FrmQoEVideoTest extends Fragment {

    private Intent videoPlayerIntent;

    private OnFragmentInteractionListener mListener;
    private View myView;
    private PhoneInfo pi;
    private boolean flagGetFristPI = false;
    private RelativeLayout rl_btn_start;
    private TextView tv_start;
    private Handler qoeVideoPlayerHandler;
    public FrmQoEVideoTest() {

    }

    public static FrmQoEVideoTest newInstance(String param1, String param2) {
        FrmQoEVideoTest fragment = new FrmQoEVideoTest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_qo_evideo_test, container, false);
        rl_btn_start = myView.findViewById(R.id.rl_btn_start);
        tv_start = myView.findViewById(R.id.tv_start);
        tv_start.setText("正在加载...");
        rl_btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StartQoEVideoTest();
            }
        });
        tv_start.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                StartQoEVideoTest();
            }
        });
        return myView;
    }
    //开始视频测试按钮
    private void StartQoEVideoTest() {
        if(!flagGetFristPI)return;
        if(pi==null)return;
        Gson gson=new Gson();
        String json=gson.toJson(pi);
        QoEVideoPlayerActivity qoEVideoPlayerActivity=new QoEVideoPlayerActivity();
        qoeVideoPlayerHandler=qoEVideoPlayerActivity.getHandler();
        Intent intent = new Intent(getActivity(),qoEVideoPlayerActivity.getClass());
        intent.putExtra("piJson", json);
        videoPlayerIntent=intent;
        startActivity(videoPlayerIntent);

    }

    private String getNowTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
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
    //主进程发新位置
    public void setLocations(double lon, double lat, double accuracy, double altitude, double speed, double satelliteCount) {
        if (!flagGetFristPI) return;
        if (pi == null) return;
        DecimalFormat decimalFormat = new DecimalFormat("0.000000");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String lonstr = decimalFormat.format(lon);//format 返回的是字符串
        String latstr = decimalFormat.format(lat);//format 返回的是字符串
        pi.lon = lon;
        pi.lat = lat;
        pi.accuracy = accuracy;
        pi.altitude = altitude;
        pi.gpsSpeed = speed;
        pi.satelliteCount = (int) satelliteCount;
        if(qoeVideoPlayerHandler!=null){
            try {
                Message msg = new Message();
                msg.what = 0;
                Bundle b = new Bundle();
                b.putDouble("lon", lon);
                b.putDouble("lat", lat);
                b.putDouble("accuracy", accuracy);
                b.putDouble("altitude",altitude);
                b.putDouble("speed", speed);
                b.putInt("satelliteCount",(int)satelliteCount);
                msg.setData(b);
                qoeVideoPlayerHandler.sendMessage(msg);
            } catch (Exception e) {

            }
        }
    }
    //主进程发PhoneInfo
    public void OnRecivePhoneInfo(PhoneInfo pi) {
        if (pi == null) return;
        Log.i("FrmQoEVideoTest","OnRecivePhoneInfo");
        if (!flagGetFristPI) {
            tv_start.setText("开始测试");
            flagGetFristPI = true;
        }
        if(qoeVideoPlayerHandler!=null){
            try {
                if(this.pi!=null){
                    pi.lon = this.pi.lon;
                    pi.lat =  this.pi.lat;
                    pi.accuracy = this.pi.accuracy;
                    pi.altitude = this.pi.altitude ;
                    pi.gpsSpeed = this.pi.gpsSpeed;
                    pi.satelliteCount = (int)this.pi.satelliteCount ;
                }
                Message msg = new Message();
                msg.what = 1;
                Bundle b = new Bundle();
                Gson gs = new Gson();
                String json = gs.toJson(pi);
                b.putString("PhoneInfo", json);
                msg.setData(b);
                qoeVideoPlayerHandler.sendMessage(msg);
            } catch (Exception e) {

            }
        }
        this.pi = pi;
    }
}
