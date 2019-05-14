package com.getinfo.app.uniqoe;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import android.widget.ProgressBar;

import com.getinfo.app.uniqoe.utils.AboutInfo;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.LocationInfo;
import com.getinfo.sdk.qoemaster.Setting;
import com.google.gson.Gson;

import java.util.Date;

import javax.microedition.khronos.opengles.GL;

public class SettingActivity extends WebViewActivity {
    public String TAG="SettingActivity";
    public ProgressBar progressBar;
    private SettingActivity settingActivity;
   // private String HTMLUrl = "http://10.253.12.105:8848/PublicTestH5Page/Setting.html";
     private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/Setting.html";
   // private String HTMLUrl = "http://192.168.16.55:8848/PublicTestH5Page/Setting.html";
   // private  String myIp="10.253.12.105";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_about);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this,0x05ACED,0);
        settingActivity=this;
        progressBar=findViewById(R.id.progressBar);
        HTMLUrl=HTMLUrl+"?t="+System.currentTimeMillis();
        JsInteraction jsInteraction=new JsInteraction();
        iniWebView(this,jsInteraction, HTMLUrl,TAG+"Console","android",progressBar);
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
        public void getlocation() {

            LocationInfo locationInfo=GlobalInfo.getLocationInfo();
            if(locationInfo!=null){
                Gson gson=new Gson();
                String json=gson.toJson(locationInfo);
                String str = "onLocation("+json+")";
                RunJs(str);
            }
        }
        @JavascriptInterface
        public  void getSetting(){
            Setting setting=GlobalInfo.getSetting(settingActivity);
            Gson gson=new Gson();
            String json=gson.toJson(setting);
            String str = "onSetting("+json+")";
            RunJs(str);
        }
        @JavascriptInterface
        public  void  saveSetting(String json){
            Gson gson=new Gson();
            try{
                Setting setting=gson.fromJson(json,Setting.class);
                if(setting==null){
                    RunJs("onSaveSettingResult('保存失败，格式有误')");
                    return;
                }
                GlobalInfo.setSetting(settingActivity,setting);
                RunJs("onSaveSettingResult('success')");
            }catch (Exception e){
                RunJs("onSaveSettingResult('"+e.getMessage()+"')");
            }
        }
    }
}
