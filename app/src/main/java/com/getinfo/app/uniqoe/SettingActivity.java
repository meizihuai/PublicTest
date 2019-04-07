package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.getinfo.app.uniqoe.utils.AboutInfo;
import com.getinfo.app.uniqoe.utils.LocationInfo;
import com.google.gson.Gson;

import java.util.Date;

public class SettingActivity extends AppCompatActivity {
    public String TAG="SettingActivity";
    private WebView webView;
    private SettingActivity settingActivity;
    //private String HTMLUrl = "http://10.253.12.105:8848/PublicTestH5Page/Setting.html";
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/Setting.html";
   // private  String myIp="10.253.12.105";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        StatusBarUtil.setColor(this,0x05ACED,0);
        settingActivity=this;
        iniWebView();
    }
    private void iniWebView() {
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                url = url.toLowerCase();
//                if (url.contains(myIp)) {
//                    return super.shouldInterceptRequest(view, url);
//                } else {
//                    //去掉广告
//                    return new WebResourceResponse(null, null, null);
//                }
//            }
        });


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i(TAG+"Console", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }

        });
        HTMLUrl = HTMLUrl + "?" + new Date();
        webView.addJavascriptInterface(new JsInteraction(), "android");
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
        public void goBack(String msg) {
            Log.i(TAG,"goBack");
            settingActivity.finish();
        }
        @JavascriptInterface
        public  void getAboutInfo(String msg){
            Log.i(TAG,"getAboutInfo");
            try{
                AboutInfo aboutInfo=new AboutInfo();
                aboutInfo.imei=GlobalInfo.myDeviceImei;
                aboutInfo.imsi=GlobalInfo.myDeviceImsi;
                aboutInfo.version=GlobalInfo.myVersion;
                Gson gson=new Gson();
                String str=gson.toJson(aboutInfo);
                String func="onAboutInfo("+str+")";
                RunJs(func);
            }catch (Exception e){

            }
        }
        @JavascriptInterface
        public void getlocation() {
            // Log.i("FrmAmapHTML","前端请求getlocation");
            LocationInfo locationInfo=GlobalInfo.getLocationInfo();
            if(locationInfo!=null){
                Gson gson=new Gson();
                String json=gson.toJson(locationInfo);
                String str = "onLocation("+json+")";
                RunJs(str);
            }
        }
    }
}
