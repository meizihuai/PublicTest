package com.getinfo.app.uniqoe.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Created by ii on 2019/4/15.
 *
 */

public class MultipleClick {
    MultipleClickBack multipleClickBack;
    //多次点击方法

    /**
     *
     * @param clickCount  需要点击的次数 次数》=3，最少三次
     * @param myDuration  连续点击持续的时间
     * @param view          需要添加点击事件的控件
     * @param multipleClickBack  回调监听
     */
    public void multipleClick(int clickCount, final long myDuration, View view,final MultipleClickBack multipleClickBack){
        final long [] mHits = new long[clickCount];
        this.multipleClickBack = multipleClickBack;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.arraycopy(mHits,1,mHits,0,mHits.length - 1);
                mHits[mHits.length - 1] = System.currentTimeMillis();
                if (mHits[0] >=(System.currentTimeMillis() - myDuration)){
                    multipleClickBack.doBack();
                }
            }
        });
    }

    //双击事件
    private void doubleClick(){
        long firstClickTime = 0;
        long secondClickTime = 0;
        long intervalTime = 0;
        if (firstClickTime>0){
            secondClickTime = System.currentTimeMillis();
            intervalTime = secondClickTime - firstClickTime;
            if (intervalTime>500){
                // Toast.makeText(getApplicationContext(),"",Toast.LENGTH_LONG).show();
            }else {
                firstClickTime = 0;
            }
        }
        firstClickTime = System.currentTimeMillis();
    }

    //回调接口
    public interface MultipleClickBack{
        void doBack();
    }
}

