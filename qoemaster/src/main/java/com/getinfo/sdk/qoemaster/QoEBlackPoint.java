package com.getinfo.sdk.qoemaster;


import android.util.Log;

import com.google.gson.Gson;

import java.security.PublicKey;

public class QoEBlackPoint {
    public String AID;
    public PhoneInfo Pi;
    public String VideoUrl;
    public  String Type;
    public  String  Mark;

    //第一次上传黑点数据
    public  void UploadToServer(){
        String url=GlobalInfo.uplanServerUrl+"/api/UniQoE/UploadQoEBlackPoint";
        Log.i("UploadQoEBlackPoint","上传QoE黑点,mark="+Mark);
        HTTPHelper.PostH(url, this, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                if(np.result){
                    Log.i("UploadQoEBlackPoint","上传黑点成功");
                }else {
                    Log.i("UploadQoEBlackPoint", "上传黑点失败" + np.msg);
                    if (null!=this){
                        String json = new Gson().toJson(this);
                        LocalTestInfoHelper.getInstance().addLocalTestInfo(json, "QoEBlackPoint");
                    }else {
                        Log.i("UploadQoEBlackPoint", "UploadQoEBlackPoint data is null!");
                    }
                }
            }
        });
    }



    //每次上传数据时检查 是否有未上传的黑点数据，则使用该方法上传黑点数据
    public void UploadBPToServer(final int id){
        String url=GlobalInfo.uplanServerUrl+"/api/UniQoE/UploadQoEBlackPoint";
        Log.i("UploadQoEBlackPoint","上传QoE黑点,mark="+Mark);
        HTTPHelper.PostH(url, this, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                if(np.result){
                    Log.i("UploadQoEBlackPoint","上传黑点成功");
                    LocalTestInfoHelper.getInstance().deleteById( new int[]{id});//上传成功 则删除当前的数据
                }else {
                    Log.i("UploadQoEBlackPoint", "上传黑点失败" + np.msg);
                }
            }
        });
    }
}
