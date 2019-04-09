package com.getinfo.app.uniqoe;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class FrmHTTP extends Fragment {
    private View myView;
    private String defaultUrl="https://m.baidu.com/?from=1012852s";
    private String HTMLUrl=defaultUrl;
    private OnFragmentInteractionListener mListener;
    private WebView webView;
    private String webviewlogTag="WebViewFrmHTTP";
    private ProgressDialog mProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView=inflater.inflate(R.layout.fragment_frm_htt, container, false);
        iniWebView();
        return myView;
    }

    private  WebViewClient webViewClient=new WebViewClient(){
        //禁止跳转到app外的浏览器
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            if (url.contains("blog.csdn.net")){
//                view.loadUrl("http://www.baidu.com");
//            }else {
//                view.loadUrl(url);
//            }
            view.loadUrl(url);
            return true;
        }
        //  页面开始加载的回调
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon){
            super.onPageStarted(view, url, favicon);
          //  mProgressDialog.show();
        }
        //页面加载完毕回调
        @Override
        public void onPageFinished(WebView view, String url){
            super.onPageFinished(view, url);
           // mProgressDialog.hide();
        }
        //加载错误的时候会回调，在其中可做错误处理，比如再请求加载一次，或者提示404的错误页面
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl){
           super.onReceivedError(view,errorCode,description,failingUrl);
        }
         // 当接收到https错误时，会回调此函数，在其中可以做错误处理

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
           //一定要注释掉，不让系统处理ssl错误
            //  super.onReceivedSslError(view, handler, error);
            //继续加载网页
            handler.proceed();
        }
        /**
         * 在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次
         */
        @Override
        public void onLoadResource(WebView view, String url){
            super.onLoadResource(view,url);
        }
        // 在每一次请求资源时，都会通过这个函数来回调，可当作拦截器

        public WebResourceResponse shouldInterceptRequest(WebView view,  String url) {
            //在每一次请求资源时，都会通过这个函数来回调，比如超链接、JS文件、CSS文件、图片等，
            // 也就是说浏览器中每一次请求资源时，都会回调回来，无论任何资源！
            // 但是必须注意的是shouldInterceptRequest函数是在非UI线程中执行的
            //该函数会在请求资源前调用
            return null; //返回null，则表示不拦截，Android会显示实际url地址
        }
    };
    private void iniWebView() {
        mProgressDialog = new ProgressDialog(getActivity());
        webView = myView.findViewById(R.id.webView);
        webView.setWebViewClient(this.webViewClient);
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
                Log.i(webviewlogTag, "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }

        });
      //HTMLUrl = HTMLUrl + "?" + System.currentTimeMillis();
        HTMLUrl="https://m.sohu.com/?_trans_=000012_qq_mz";
        webView.addJavascriptInterface(new JsInteraction(), "android");
        webView.loadUrl(HTMLUrl);
    }
    public void RunJs(final String js) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" + js);
            }
        });
    }
    public class JsInteraction {
        public JsInteraction() {

        }
        @JavascriptInterface
        public void isReadyToPlayVideo(String msg) {

        }
        @JavascriptInterface
        public void startQoEVideoTest(String msg) {

        }
    }

   //region FrmSelf

    public FrmHTTP() {

    }


    public static FrmHTTP newInstance(String param1, String param2) {
        FrmHTTP fragment = new FrmHTTP();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }




    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    //endregion
}
