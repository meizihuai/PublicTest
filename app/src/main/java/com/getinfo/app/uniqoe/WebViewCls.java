package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.tencent.smtt.export.external.interfaces.ConsoleMessage;
import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.util.Date;

public class WebViewCls {
    private String TAG="WebViewConsole";
    private ProgressBar progressBar;
    public com.tencent.smtt.sdk.WebView webView;
  //  private Activity mActivity;
    private Object JsInteraction;
    private String HTMLUrl;
    private String jsCallName;
    public void iniWebView(Activity activity, Object JsInteraction, String url, String TAG, String jsCallName, ProgressBar progressBar) {
        com.tencent.smtt.sdk.WebView webViewTmp=activity.findViewById(R.id.webView);
        iniWebView(webViewTmp,JsInteraction,url,TAG,jsCallName,progressBar);
    }
    public void iniWebView(com.tencent.smtt.sdk.WebView webView, Object JsInteraction, String url,final String TAG, String jsCallName, ProgressBar progressBar) {
        this.webView=webView;
        this.JsInteraction=JsInteraction;
        this.progressBar=progressBar;
        this.HTMLUrl=url;
        this.TAG=TAG;
        this.jsCallName=jsCallName;

        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            //            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                url = url.toLowerCase();
//                if(url.contains("http://221.238.40.153")){
//                    return super.shouldInterceptRequest(view, url);
//                }else{
//                    Log.i(TAG,"拦截到一条广告,url="+url);
//                    return new WebResourceResponse(null,null,null);
//                }
//            }
        });


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        webSettings.setDomStorageEnabled(true);
        final ProgressBar pgb=this.progressBar;
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissionsCallback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i(TAG, "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if(pgb==null)return;
                if (newProgress == 100) {
                    pgb.setVisibility(View.GONE);//加载完网页进度条消失
                } else {
                    pgb.setVisibility(View.VISIBLE);//开始加载网页时显示进度条
                    pgb.setProgress(newProgress);//设置进度值
                }
                super.onProgressChanged(view, newProgress);
            }

        });
        HTMLUrl = HTMLUrl + "?" + new Date();
        webView.addJavascriptInterface( JsInteraction, jsCallName);
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

}
