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
import android.view.KeyEvent;
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

public class WebViewActivity extends AppCompatActivity {

    private WebViewCls webViewCls;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //改写物理返回键的逻辑
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            if(webViewCls==null)finish();
            if(webViewCls.webView.canGoBack()) {
                webViewCls.webView.goBack();//返回上一页面
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
    public void iniWebView(Activity activity, Object jsInteraction, String url, String TAG, String jsCallName, ProgressBar progressBar) {
        webViewCls=new WebViewCls();
        webViewCls.iniWebView(activity,jsInteraction,url,TAG,jsCallName,progressBar);
    }
    public void iniWebView(com.tencent.smtt.sdk.WebView webView, Object jsInteraction, String url, String TAG, String jsCallName, ProgressBar progressBar) {
        webViewCls=new WebViewCls();
        webViewCls.iniWebView(webView,jsInteraction,url,TAG,jsCallName,progressBar);
    }
    public  void RunJs(final String js){
        webViewCls.RunJs(js);
    }
}
