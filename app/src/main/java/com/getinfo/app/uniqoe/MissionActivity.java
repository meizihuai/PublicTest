package com.getinfo.app.uniqoe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;


import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;

import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import java.util.Date;

//任务管理界面，本界面是H5混合开发模式，任务管理界面实质是webView运行的H5页面
public class MissionActivity extends WebViewActivity {
    private  String TAG="MissionActivity";
    public ProgressBar progressBar;
    public  MissionActivity mActivity;
    private com.tencent.smtt.sdk.WebView webView;
 //   private String HTMLUrl = "http://10.253.12.105:8849/PublicTestH5Page/Amap.html";
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/index.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mission);
        StatusBarUtil.setColor(this,0x05ACED,0);

        mActivity=this;
        progressBar=findViewById(R.id.progressBar);
        HTMLUrl=HTMLUrl+"?t="+System.currentTimeMillis();
        JsInteraction jsInteraction=new JsInteraction();
        iniWebView(this,jsInteraction, HTMLUrl,TAG+"Console","android",progressBar);

    }

    public class JsInteraction {

        @JavascriptInterface
        public void doAndroidCode(String message) {
            Log.i("JsInteraction", message);
        }

        @JavascriptInterface
        public void getPhoneInfo(String message) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String js="onRecivePhoneInfo(\"\")";
                    PhoneInfo pi= GlobalInfo.getPi();
                    if(pi!=null){
                        Gson gson=new Gson();
                        String json=gson.toJson(pi);
                        js="onRecivePhoneInfo("+json+")";
                    }
                    webView.loadUrl("javascript:"+js);
                }
            });
        }
    }
}

