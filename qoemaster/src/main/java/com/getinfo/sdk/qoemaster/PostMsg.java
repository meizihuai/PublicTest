package com.getinfo.sdk.qoemaster;

public class PostMsg {
    public  String func;
    public Object data;
    public  String token;
    public PostMsg(String func,Object data){
        this.func=func;
        this.data=data;
    }
}
