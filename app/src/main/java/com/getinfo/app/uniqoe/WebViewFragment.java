package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.widget.ProgressBar;

public class WebViewFragment extends Fragment {
    private WebViewCls webViewCls;
    public void iniWebView(Activity mActivity, Object jsInteraction, String url, String TAG, String jsCallName, ProgressBar progressBar) {
        webViewCls=new WebViewCls();
        webViewCls.iniWebView(mActivity,jsInteraction,url,TAG,jsCallName,progressBar);
    }
    public  void RunJs(final String js){
        webViewCls.RunJs(js);
    }


}
