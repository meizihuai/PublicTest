package com.getinfo.app.uniqoe;

import java.util.ArrayList;
import java.util.List;

public class QoEHTTPInfo {
    public String DATETIME;   //时间
    public int VMOS;   //综合评分
    public int RESPONSETIMESCORE;   //HTTP响应时间评分
    public int TOTALBUFFERTIMESCORE;   //页面总缓存时间的评分
    public int DNSTIMESCORE;   //DNS解析时间的评分
    public int DOWNLOADSPEEDSCORE;   //下载速度的评分
    public int WHITESCREENTIMESCORE;   //白屏等待时间的评分

    public long RESPONSETIME;   //HTTP响应时间
    public long TOTALBUFFERTIME;   //总缓冲时间
    public long DNSTIME;   //DNS解析时间
    public long DOWNLOADSPEED;   //下载速度
    public long WHITESCREENTIME;   //白屏等待时间
    public List<HTTPTestInfo> HTTPTESTRESULTLIST;   //HTTP测试列表，往往多个URL一次测试
    public PhoneInfo pi;
    public QoEHTTPInfo(){
        HTTPTESTRESULTLIST=new ArrayList<HTTPTestInfo>();
    }
    public QoEHTTPInfo(PhoneInfo pi) {
        this.pi = pi;
        HTTPTESTRESULTLIST=new ArrayList<HTTPTestInfo>();
    }
    public void setPhoneInfo(PhoneInfo pi) {
        this.pi = pi;
    }
    public class HTTPTestInfo{
        public String URL;  //URL地址
        public long RESPONSETIME;  //HTTP响应时间
        public long TOTALBUFFERTIME;//总缓冲时间
        public long DNSTIME;   //DNS解析时间
        public long DOWNLOADSPEED;   //下载速度
        public long HTMLBUFFERSIZE; //总缓冲字节数
        public long WHITESCREENTIME;  //白屏等待时间
    }
}
