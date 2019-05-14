package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;


import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import java.io.Console;
import java.util.Date;

public class OneKeyTestHisActivity extends WebViewActivity {

    public String TAG="OneKeyTestHisActivity";
    public  OneKeyTestHisActivity mActivity;
    public ProgressBar progressBar;
    private com.tencent.smtt.sdk.WebView webView;
  //  private String HTMLUrl = "http://10.253.12.104:8848/PublicTestH5Page/OneKeyTestHis.html";
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/OneKeyTestHis.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this,0x05ACED,0);
        mActivity=this;
        progressBar=findViewById(R.id.progressBar);
        HTMLUrl=HTMLUrl+"?t="+System.currentTimeMillis();
        JsInteraction jsInteraction=new JsInteraction();
        iniWebView(this,jsInteraction, HTMLUrl,TAG+"Console","android",progressBar);
    }


    public class JsInteraction {

        @JavascriptInterface
        public void goBack(String msg) {
            Log.i("OnekeyTestHis","goBack");
            mActivity.finish();
        }

        @JavascriptInterface
        public void getPhoneInfo(String message) {
            String js="onRecivePhoneInfo(\"\")";
            PhoneInfo pi= GlobalInfo.getPi();
            if(pi!=null){
                Gson gson=new Gson();
                String json=gson.toJson(pi);
                js="onRecivePhoneInfo("+json+")";
            }
            RunJs(js);
        }
    }

}
