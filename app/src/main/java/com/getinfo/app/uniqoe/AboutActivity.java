package com.getinfo.app.uniqoe;

import android.annotation.SuppressLint;
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

import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.getinfo.app.uniqoe.utils.AboutInfo;
import com.getinfo.sdk.qoemaster.DownloadUtils;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.google.gson.Gson;
import com.googlecode.mp4parser.boxes.AC3SpecificBox;
import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.Console;
import java.util.Date;

import okhttp3.HttpUrl;

public class AboutActivity extends WebViewActivity {
    public String TAG="AboutActivity";
    public  AboutActivity mActivity;
    public ProgressBar progressBar;
    private com.tencent.smtt.sdk.WebView webView;
   // private String HTMLUrl = "http://10.253.12.105:8848/PublicTestH5Page/About.html";
     private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/About.html";
    //private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/APPDataTrans.html";
    //private  String HTMLUrl="http://debugtbs.qq.com";
   // private String HTMLUrl="http://10.253.12.105:8848/PublicTestH5Page/About.html";
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
            Log.i(TAG,"goBack");
            mActivity.finish();
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
            url = url + "?t=" + System.currentTimeMillis();
            DownloadUtils du = new DownloadUtils(mActivity);
            du.download(url, appName, dirName);
        }

        @JavascriptInterface
        public  void callAPPMethod(String msg){
            RunJs("onReceiveMsg('"+msg+"')");
        }
    }
}
