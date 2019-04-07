package com.getinfo.app.uniqoe.utils;

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

}
