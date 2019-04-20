package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.AboutInfo;
import com.getinfo.sdk.qoemaster.DownloadUtils;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.google.gson.Gson;
import com.googlecode.mp4parser.boxes.AC3SpecificBox;

import java.io.Console;
import java.util.Date;

public class AboutActivity extends AppCompatActivity {
    public String TAG="AboutActivity";
    private WebView webView;
   // private String HTMLUrl = "http://10.253.12.105:8848/PublicTestH5Page/About.html";
     private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/About.html";
    //private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/APPDataTrans.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this,0x05ACED,0);
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
                Log.i("WebViewConsoleAbout", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }


        });
        HTMLUrl = HTMLUrl + "?" + new Date();
        JsInterface jsInterface = new JsInterface() {
            @Override
            public void onBack() {
                Log.i(TAG, "onBack");
                finish();
            }
            @Override
            public void onGetUpdate(String url,String appName,String dirName , Activity activity){
                url = url + "?t=" + System.currentTimeMillis();
                DownloadUtils du = new DownloadUtils(activity);
                du.download(url, appName, dirName);
            }
        };
        webView.addJavascriptInterface(new JsInteraction(jsInterface,this), "android");
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
        void onGetUpdate(String url,String appName,String dirName,Activity activity);
    }

    public class JsInteraction {
        private JsInterface jsInterface;
        private Activity activity;
        public JsInteraction( JsInterface jsInterface,Activity activity) {
            this.jsInterface=jsInterface;
            this.activity=activity;
        }

        @JavascriptInterface
        public void goBack(String msg) {
            Log.i(TAG,"goBack");
            jsInterface.onBack();
        }
        @JavascriptInterface
        public  void getAboutInfo(String msg){
            Log.i(TAG,"getAboutInfo");
            try{
                AboutInfo aboutInfo=new AboutInfo();
                aboutInfo.imei= GlobalInfo.myDeviceImei;
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
        public  void  getUpdate(String url,String appName,String dirName){
            Log.i(TAG,"getUpdate,url="+url+",appName="+appName+",dirName="+dirName);
            jsInterface.onGetUpdate(url,appName,dirName,activity);
        }

        @JavascriptInterface
        public  void callAPPMethod(String msg){
            RunJs("onReceiveMsg('"+msg+"')");
        }


    }
}
