package com.getinfo.app.uniqoe;

import android.util.Log;

import java.text.DecimalFormat;
//一键测试自动打分
public class ScoreHelper {

    public ScoreInfo GetRSRPScore(double rsrp){
        if(rsrp>=-80)return new ScoreInfo(5);
        if(rsrp>=-100)return new ScoreInfo(4);
        if(rsrp>=-105)return new ScoreInfo(3);
        if(rsrp>=-110)return new ScoreInfo(2);
        if(rsrp>=-140)return new ScoreInfo(1);
        return new ScoreInfo(1);
    }
    public ScoreInfo GetSINRScore(double sinr){
        if(sinr>=15)return new ScoreInfo(5);
        if(sinr>=5)return new ScoreInfo(4);
        if(sinr>=0)return new ScoreInfo(3);
        if(sinr>=-3)return new ScoreInfo(2);
        if(sinr>=-50)return new ScoreInfo(1);
        return new ScoreInfo(1);
    }
    public ScoreInfo GetWifiStrengthScore(int score){
        return new ScoreInfo(score);
    }
    public ScoreInfo GetNetSpeedProgress(long speed){
        double s=speed/1024;
        if(s>=1280)return new ScoreInfo(5);
        if(s>=640)return new ScoreInfo(4);
        if(s>=128)return new ScoreInfo(3);
        if(s>=10)return new ScoreInfo(2);
        return new ScoreInfo(1);
    }
    public ScoreInfo GetVideoScore(long speed,long stallTime,long totalTime) {
        int a=0;
        int b=0;
        if(totalTime==0)return new ScoreInfo(1);
        double stallRate=stallTime/totalTime;
        double s=speed/1024;
        if(s<=10)a=1;
        if(s>=10)a=2;
        if(s>=128)a=3;
        if(s>=640)a=4;
        if(s>=1280)a=5;

        if(stallRate>=0.5)b=1;
        if(stallRate>=0.1)b=2;
        if(stallRate>=0.05)b=3;
        if(stallRate>=0.01)b=4;
        if(stallRate<=0.01)b=5;
        int c=(int)(a*0.5+b*0.5);
//        Log.i("FrmOneKeyTest","视频 speed="+s);
//        Log.i("FrmOneKeyTest","视频 stallRate="+stallRate);
//        Log.i("FrmOneKeyTest","视频 a="+a);
//        Log.i("FrmOneKeyTest","视频 b="+b);
//        Log.i("FrmOneKeyTest","视频 c="+c);
        return new ScoreInfo(c);
    }
    public ScoreInfo GetHTMLPageScore(long responseTime) {
        if(responseTime>=5000)return new ScoreInfo(1);
        if(responseTime>3000)return new ScoreInfo(2);
        if(responseTime>2000)return new ScoreInfo(3);
        if(responseTime>500)return new ScoreInfo(4);
        return new ScoreInfo(5);
    }
    public String GetTogetherScoreString(OneKeyTestInfo oneKeyTestInfo){
        if(oneKeyTestInfo==null)return "抱歉，您的本次测试无效！";
        boolean flagIsWiFi=oneKeyTestInfo.isFlagIsWiFi();
        int rsrpA=oneKeyTestInfo.net4gQualityScore.score;
        int rsrpB=oneKeyTestInfo.net4gStrengthScore.score;
        int wifiA=oneKeyTestInfo.wifiQualityScore.score;
        int wifiB=oneKeyTestInfo.wifiStrengthScore.score;
        int nerSpeed=oneKeyTestInfo.netSpeedScore.score;
        int video=oneKeyTestInfo.videoScore.score;
        int htmlPage=oneKeyTestInfo.htmlPageScore.score;
        if(!flagIsWiFi)wifiA=5;wifiB=5;
        int sum=rsrpA+rsrpB+wifiA+wifiB+video+htmlPage+nerSpeed;
        Log.i("FrmOneKeyTest","总分 sum="+sum);
        int totalScore=5*7;
       // oneKeyTestInfo.togetherScore.score=(int)(sum/7);
        double score=100*sum/totalScore;
        Log.i("FrmOneKeyTest","总分 score="+score);
        double rand = (double) ((Math.random() *9 + 1));
        rand=score+rand;
        if(rand>=100)rand=99.95;
        if(rand<=0)rand=0.95;
        Log.i("FrmOneKeyTest","总分 rand="+rand);
        DecimalFormat decimalFormat =new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String distanceString = decimalFormat.format(rand)+" %";//format 返回的是字符串
        String scoreString= decimalFormat.format(score);
        String head="";
        if(rand<=20)head="抱歉";
        if(rand<=40)head="遗憾";
        if(rand<=60)head="很好";
        if(rand<=80)head="漂亮";
        if(rand<=90)head="恭喜";
        if(rand>90)head="完美";
        String result=head+",您的总评分为 "+scoreString+" 分,击败了全国 "+distanceString+ " 的用户！";
        return result;
    }
}
