package com.getinfo.app.uniqoe;

import android.graphics.Color;
import android.widget.TextView;
//textView形式的分值表现，已弃用
public class ScoreSelectedBean {
    private TextView textView;
    public ScoreSelectedBean(TextView textView){
        this.textView=textView;
    }

    public void setSelected(boolean selected) {
       if(selected){
           textView.setBackgroundResource(R.drawable.ping_bg_selected);
           textView.setTextColor(Color.parseColor("#ffffff"));
       }else{
           textView.setBackgroundResource(R.drawable.ping_bg_nomal);
           textView.setTextColor(Color.parseColor("#00a0e9"));
       }
    }
}

