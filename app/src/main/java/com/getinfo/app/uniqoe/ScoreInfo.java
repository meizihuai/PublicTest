package com.getinfo.app.uniqoe;
//一键测试用到的分值信息
public class ScoreInfo{
    public  int score;
    public int progress;
    public String scoreName;
    public ScoreInfo(int score) {

        if(score>=5)scoreName="很好";
        if(score==4)scoreName="好";
        if(score==3)scoreName="一般";
        if(score==2)scoreName="差";
        if(score<=1)scoreName="很差";
        if(score<=0)scoreName="待测";
        this.score=score;
        this.progress=20*score;
    }
}