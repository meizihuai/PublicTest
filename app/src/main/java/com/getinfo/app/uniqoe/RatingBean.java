package com.getinfo.app.uniqoe;

import android.widget.ImageView;
//星型打分 设置选中与未选中
public class RatingBean {
    private ImageView imageView;
    public RatingBean(ImageView imageView){
        this.imageView=imageView;
    }
    public void setSelected(boolean selected) {
        if(selected){
            imageView.setBackgroundResource(R.drawable.icon_rating_selected);
        }else{
            imageView.setBackgroundResource(R.drawable.icon_rating_nomal);
        }
    }
}
