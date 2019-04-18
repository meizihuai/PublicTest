package com.getinfo.sdk.qoemaster;


//网络制式库，根据频点来识别网络制式  Band  等参数
public class EarFcnHelper {
    public static EFInfo[] efInfos={
            new EFInfo(0,599,1,"FDD",""),
            new EFInfo(600,1199,2,"FDD",""),
            new EFInfo(1200,1949,3,"FDD","1800"),
            new EFInfo(1950,2399,4,"FDD",""),
            new EFInfo(2400,2649,5,"FDD",""),
            new EFInfo(2650,2749,6,"FDD",""),
            new EFInfo(2750,3449,7,"FDD",""),
            new EFInfo(3450,3799,8,"FDD","900"),
            new EFInfo(3800,4149,9,"FDD",""),
            new EFInfo(4150,4749,10,"FDD",""),
            new EFInfo(4750,4949,11,"FDD",""),
            new EFInfo(5010,5179,12,"FDD",""),
            new EFInfo(5180,5279,13,"FDD",""),
            new EFInfo(5280,5379,14,"FDD",""),
            new EFInfo(5730,5849,17,"FDD",""),
            new EFInfo(5850,5999,18,"FDD",""),
            new EFInfo(6000,6149,19,"FDD",""),
            new EFInfo(6150,6449,20,"FDD",""),
            new EFInfo(6450,6599,21,"FDD",""),
            new EFInfo(6600,7399,22,"FDD",""),
            new EFInfo(7500,7699,23,"FDD",""),
            new EFInfo(7700,8039,24,"FDD",""),
            new EFInfo(8040,8689,25,"FDD",""),
            new EFInfo(8690,9039,26,"FDD",""),
            new EFInfo(9040,9209,27,"FDD",""),
            new EFInfo(9210,9659,28,"FDD",""),
            new EFInfo(9660,9769,29,"FDD",""),
            new EFInfo(36000,36199,33,"TDD",""),
            new EFInfo(36200,36349,34,"TDD","A"),
            new EFInfo(36350,36949,35,"TDD",""),
            new EFInfo(36950,37549,36,"TDD",""),
            new EFInfo(37550,37749,37,"TDD",""),
            new EFInfo(37750,38249,38,"TDD","D"),
            new EFInfo(38250,38649,39,"TDD","F"),
            new EFInfo(38650,39649,40,"TDD","E"),
            new EFInfo(39650,41589,41,"TDD","D"),
            new EFInfo(41590,43589,42,"TDD",""),
            new EFInfo(43590,45589,43,"TDD",""),
            new EFInfo(45590,46589,44,"TDD","")
    };
    public static class EFInfo {
        public int minNDL;
        public int maxNDL;
        public int band;
        public String mode;
        public String mark;
        public String result;
        public EFInfo(int minNDL,int maxNDL,int band,String mode,String mark)  {
            this.minNDL=minNDL;
            this.maxNDL=maxNDL;
            this.band=band;
            this.mode=mode;
            this.mark=mark;
            if("".equals(mark)){
                result=mode+"/Band"+band;
            }else{
                result=mode+"/Band"+band+"/"+mark;
            }
        }

    }
    public static String GetFreqLable(int earfcn){
        for(EFInfo efInfo :efInfos){
            if(earfcn>=efInfo.minNDL && earfcn<=efInfo.maxNDL){
                return efInfo.result;
            }
        }
        return "";
    }

}
