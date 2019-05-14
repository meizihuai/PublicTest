package com.getinfo.app.uniqoe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.webkit.JavascriptInterface;

import android.widget.ProgressBar;

import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import java.text.DecimalFormat;


public class FrmQoEVideoHTML extends Fragment {

    private Intent videoPlayerIntent;

    private OnFragmentInteractionListener mListener;
    private View myView;
    private WebViewCls webViewCls;
    private String webviewlogTag="WebViewFrmQoEVideoHTML";
    private ProgressBar progressBar;
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/QoEVideoStart.html";
    // private String HTMLUrl = "http://10.253.12.105:8848/PublicTestH5Page/QoEVideoStart.html";

    private PhoneInfo pi;
    private boolean flagGetFristPI = false;
    private Handler qoeVideoPlayerHandler;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_qoe_video_html, container, false);
        iniWebView();
        return myView;
    }
    private void iniWebView() {
        progressBar=myView.findViewById(R.id.progressBar);
        com.tencent.smtt.sdk.WebView webView=myView.findViewById(R.id.webView);
        HTMLUrl=HTMLUrl+"?"+System.currentTimeMillis();
        webViewCls=new WebViewCls();
        webViewCls.iniWebView(webView,new JsInteraction(),HTMLUrl,webviewlogTag,"android",progressBar);
    }

    public void RunJs(final String js) {
       webViewCls.RunJs(js);
    }
    public class JsInteraction {
        @JavascriptInterface
        public void isReadyToPlayVideo(String msg) {
            RunJs("onIsReadyToPlayVideo("+flagGetFristPI+")");
        }
        @JavascriptInterface
        public void startQoEVideoTest(String msg) {
            StartQoEVideoTest();
        }
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
            //这里改变状态，可以开始测试
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




    public FrmQoEVideoHTML() {

    }


    public static FrmQoEVideoHTML newInstance(String param1, String param2) {
        FrmQoEVideoHTML fragment = new FrmQoEVideoHTML();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
