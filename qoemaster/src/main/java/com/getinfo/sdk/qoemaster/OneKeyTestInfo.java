package com.getinfo.sdk.qoemaster;



//一键测试所需要的信息
public class OneKeyTestInfo {
    public ScoreInfo net4gStrengthScore;  //RSRP分值
    public ScoreInfo net4gQualityScore;  //SINR分值
    public ScoreInfo wifiStrengthScore;  //wifi信号强度分值
    public ScoreInfo wifiQualityScore;   //wifi信号质量分值
    public ScoreInfo netSpeedScore;     //网速测试评分
    public ScoreInfo videoScore;       //视频测试评分
    public ScoreInfo htmlPageScore;     //网页测试分值
    public ScoreInfo togetherScore;    //总体评分
    public boolean flagIsWiFi;         //判断是否是在WIFI状态
    public int netSpeedTestSpeed;
    public int videoTestSpeed;
    public  long httpResonseTime;
    public PhoneInfo pi;
    public OneKeyTestInfo(){
        net4gStrengthScore=new ScoreInfo(0);
        net4gQualityScore=new ScoreInfo(0);
        wifiStrengthScore=new ScoreInfo(0);
        wifiQualityScore=new ScoreInfo(0);
        netSpeedScore=new ScoreInfo(0);
        videoScore=new ScoreInfo(0);
        htmlPageScore=new ScoreInfo(0);
        togetherScore=new ScoreInfo(0);
    }
    public void SetIsWiFi(boolean flag){
        flagIsWiFi=flag;
    }

    public boolean isFlagIsWiFi() {
        return flagIsWiFi;
    }
}
