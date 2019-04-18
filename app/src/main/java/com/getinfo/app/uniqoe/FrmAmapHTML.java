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

import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.LocationInfo;
import com.google.gson.Gson;

import java.util.Date;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FrmAmapHTML.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FrmAmapHTML#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FrmAmapHTML extends Fragment {

    private View myView;
    private Context mContext;
    private WebView webView;
    private String HTMLUrl = "http://221.238.40.153:7062/html/PublicTestH5Page/Amap.html";
    //  private String HTMLUrl = "http://10.253.12.105:8849/PublicTestH5Page/Amap.html";
    //private String HTMLUrl = "https://www.nbwanlian.com/amap.html";
    private OnFragmentInteractionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myView = inflater.inflate(R.layout.fragment_frm_amap_html, container, false);
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
                Log.i("WebViewConsole", "[" + consoleMessage.messageLevel() + "] " + consoleMessage.message() + "(" + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber() + ")");
                return super.onConsoleMessage(consoleMessage);
            }


        });
        HTMLUrl = HTMLUrl + "?" + new Date();
        webView.addJavascriptInterface(new JsInteraction(), "android");
        webView.loadUrl(HTMLUrl);
    }

    private void iniWebView2() {
        Log.i("FrmAmapHTML", "HTMLUrl=" + HTMLUrl);
        webView = myView.findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        // 设置与Js交互的权限
        webSettings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // webView.addJavascriptInterface(qoEWorker.new JsInteraction(), "android");
        // 防止webView刷新页面的时候跳转到系统浏览器
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        HTMLUrl = HTMLUrl + "?" + new Date();
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
        public void getlocation() {
            // Log.i("FrmAmapHTML","前端请求getlocation");
            LocationInfo locationInfo= GlobalInfo.getLocationInfo();
            if(locationInfo!=null){
            Gson gson=new Gson();
                String json=gson.toJson(locationInfo);
                String str = "onLocation("+json+")";
                RunJs(str);
            }
        }
    }

    //region Frm自带
    public FrmAmapHTML() {

    }


    public static FrmAmapHTML newInstance(String param1, String param2) {
        FrmAmapHTML fragment = new FrmAmapHTML();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    // TODO: Rename method, update argument and hook method into UI event
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
