package com.getinfo.sdk.qoemaster;

public class NormalResponse {
    public  boolean result;
    public  String msg;
    public String errmsg;
    public Object data;
    public NormalResponse(){

    }
    public  NormalResponse(boolean result ,String msg){
        this.result=result;
        this.msg=msg;
    }
    public  NormalResponse(boolean result,String msg,String errmsg,Object data){
        this.result=result;
        this.msg=msg;
        this.errmsg=errmsg;
        this.data=data;
    }

}

