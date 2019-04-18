package com.getinfo.app.uniqoe;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.getinfo.sdk.qoemaster.QoEHTTPInfo;
import com.getinfo.sdk.qoemaster.Setting;
import com.google.gson.Gson;


public class FrmHTTP extends Fragment {
    private View myView;
  private   ImageView iv_baidu,iv_taobao,iv_souhu,iv_toutiao,iv_tencent;
    private String defaultUrl = "https://m.baidu.com/?from=1012852s";
    private String HTMLUrl = defaultUrl;
    private OnFragmentInteractionListener mListener;
    private WebView webView;
    private String webviewlogTag = "WebViewFrmHTTP";
    private ProgressDialog mProgressDialog;
    private  boolean isOpenedScoreActitity=false;
    private QoEHTTPInfo qoEHTTPInfo;
    private long qoeHttpStartMs=0;
    private String[] htmlUrls = new String[]{
            "https://m.baidu.com/?from=1012852s",
            "https://h5.m.taobao.com/?sprefer=sypc00",
            "https://m.sohu.com/?_trans_=000012_qq_mz",
            "https://m.toutiao.com/?W2atIF=1",
            "https://portal.3g.qq.com/?aid=index&g_ut=3&g_f=23886"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_htt, container, false);
        iniImages();
        iniWebView();
        return myView;
    }
    private  void iniImages(){
        //ImageView iv_baidu,iv_taobao,iv_souhu,iv_toutiao,iv_tencent
        iv_baidu=myView.findViewById(R.id.iv_baidu);
        iv_taobao=myView.findViewById(R.id.iv_taobao);
        iv_souhu=myView.findViewById(R.id.iv_souhu);
        iv_toutiao=myView.findViewById(R.id.iv_toutiao);
        iv_tencent=myView.findViewById(R.id.iv_tencent);
        iv_baidu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(htmlUrls[0]);
            }
        });
        iv_taobao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(htmlUrls[1]);
            }
        });
        iv_souhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(htmlUrls[2]);
            }
        });
        iv_toutiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(htmlUrls[3]);
            }
        });
        iv_tencent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrl(htmlUrls[4]);
            }
        });
    }
    private  void loadUrl(String url){
        isOpenedScoreActitity=false;
        PhoneInfo pi= GlobalInfo.getPi();
        if(pi==null){
            MsgBox("请等待系统加载完毕");
            return;
        }
        qoEHTTPInfo=new QoEHTTPInfo(GlobalInfo.getPi());
        qoEHTTPInfo.pi.businessType="QoEHTTP";
        qoeHttpStartMs=System.currentTimeMillis();
        qoEHTTPInfo.HTTPURL=url;
        webView.loadUrl(url);
    }
    private void MsgBox(String msg) {
        Toast.makeText(myView.getContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private WebViewClient webViewClient = new WebViewClient() {
        //禁止跳转到app外的浏览器
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            try {
                if(url.startsWith("weixin://") //微信
                        || url.startsWith("alipays://") //支付宝
                        || url.startsWith("mailto://") //邮件
                        || url.startsWith("tel://")//电话
                        || url.startsWith("dianping://")//大众点评
                        || url.startsWith("tbopen://")
                        || url.startsWith("baiduboxlite://")
                    //其他自定义的scheme
                ) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    startActivity(intent);
                     return true;
                }
            } catch (Exception e) { //防止crash (如果手机上没有安装处理某个scheme开头的url的APP, 会导致crash)
                return true;//没有安装该app时，返回true，表示拦截自定义链接，但不跳转，避免弹出上面的错误页面
            }

            //处理http和https开头的url
         //   wv.loadUrl(url);
            view.loadUrl(url);
            return true;
        }

        //  页面开始加载的回调
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            qoEHTTPInfo.RESPONSETIME=System.currentTimeMillis()-qoeHttpStartMs;
            qoEHTTPInfo.WHITESCREENTIME=qoEHTTPInfo.RESPONSETIME;
            mProgressDialog.show();
        }

        //页面加载完毕回调
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if(isOpenedScoreActitity)return;
            mProgressDialog.hide();
            isOpenedScoreActitity=true;
            qoEHTTPInfo.TOTALBUFFERTIME=System.currentTimeMillis()-qoeHttpStartMs;
            Setting setting = GlobalInfo.getSetting(myView.getContext());
            boolean isScore = true;
            if (setting != null) {
                isScore = setting.switchQoEScore;
            }
            if(isScore){
                QoEVideoScoreActivity qoEVideoScoreActivity = new QoEVideoScoreActivity();
                Intent intent = new Intent(myView.getContext(), qoEVideoScoreActivity.getClass());
                intent.putExtra("ScoreKind", "QoEHTTP");
                intent.putExtra("QoEHTTPInfo",new Gson().toJson(qoEHTTPInfo));
                startActivity(intent);
            }


        }

        //加载错误的时候会回调，在其中可做错误处理，比如再请求加载一次，或者提示404的错误页面
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
        // 当接收到https错误时，会回调此函数，在其中可以做错误处理

        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //一定要注释掉，不让系统处理ssl错误
            //  super.onReceivedSslError(view, handler, error);
            //继续加载网页
            handler.proceed();
        }

        /**
         * 在加载页面资源时会调用，每一个资源（比如图片）的加载都会调用一次
         */
        @Override
        public void onLoadResource(WebView view, String url) {
            super.onLoadResource(view, url);
        }
        // 在每一次请求资源时，都会通过这个函数来回调，可当作拦截器

        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
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
        HTMLUrl = "https://m.sohu.com/?_trans_=000012_qq_mz";
        webView.addJavascriptInterface(new JsInteraction(), "android");
       // webView.loadUrl(HTMLUrl);
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
