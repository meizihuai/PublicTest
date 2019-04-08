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
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;

import java.io.Console;
import java.util.Date;

public class OneKeyTestHisActivity extends AppCompatActivity {

    private WebView webView;
  //  private String HTMLUrl = "http://10.253.12.104:8848/PublicTestH5Page/OneKeyTestHis.html";
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/OneKeyTestHis.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mission);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this,0x05ACED,0);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // webView.addJavascriptInterface(qoEWorker.new JsInteraction(), "android");
        // 防止webView刷新页面的时候跳转到系统浏览器
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        final Activity activity=this;
        JsInterface jsInterface=new JsInterface() {
            @Override
            public void onBack() {
                Log.i("OnekeyTestHis","onBack");
                activity.finish();
            }
        };

        webView.addJavascriptInterface(new JsInteraction(jsInterface), "android");
        HTMLUrl=HTMLUrl +"?"+new Date();
        webView.loadUrl(HTMLUrl);
    }
    public   void RunJs(final String js){
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:"+js);
            }
        });
    }
    public interface JsInterface{
        void onBack();
    }

    public class JsInteraction {
        private  JsInterface jsInterface;
        public JsInteraction( JsInterface jsInterface) {
            this.jsInterface=jsInterface;
        }

        @JavascriptInterface
        public void goBack(String msg) {
            Log.i("OnekeyTestHis","goBack");
            jsInterface.onBack();
        }

        @JavascriptInterface
        public void getPhoneInfo(String message) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    String js="onRecivePhoneInfo(\"\")";
                    PhoneInfo pi=GlobalInfo.getPi();
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
