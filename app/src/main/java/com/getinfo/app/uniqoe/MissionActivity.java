package com.getinfo.app.uniqoe;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.google.gson.Gson;

import java.util.Date;

//任务管理界面，本界面是H5混合开发模式，任务管理界面实质是webView运行的H5页面
public class MissionActivity extends AppCompatActivity {
    private WebView webView;
 //   private String HTMLUrl = "http://10.253.12.105:8849/PublicTestH5Page/Amap.html";
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/index.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_mission);
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
                return true;
            }
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                url = url.toLowerCase();
                if(url.contains("http://221.238.40.153")){
                    return super.shouldInterceptRequest(view, url);
                }else{
                    Log.i("MissionActivity","拦截到一条广告,url="+url);
                    return new WebResourceResponse(null,null,null);
                }
            }
        });
        webView.addJavascriptInterface(new JsInteraction(), "android");
        HTMLUrl=HTMLUrl+"?"+new Date();
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
    public class JsInteraction {
        public JsInteraction() {
        }

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

