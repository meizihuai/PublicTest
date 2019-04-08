package com.getinfo.app.uniqoe;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView=inflater.inflate(R.layout.fragment_frm_htt, container, false);
        iniWebView();
        return myView;
    }
    private void iniWebView() {
        webView = myView.findViewById(R.id.webView);
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
