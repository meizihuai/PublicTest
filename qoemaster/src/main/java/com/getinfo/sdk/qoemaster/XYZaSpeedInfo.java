package com.getinfo.sdk.qoemaster;

import java.math.BigDecimal;

public class XYZaSpeedInfo {
    public float x;
    public float y;
    public float z;
    public XYZaSpeedInfo(){

    }
    public XYZaSpeedInfo(float x,float y,float z){
        BigDecimal xg = new BigDecimal(x);
        this.x= xg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        BigDecimal yg = new BigDecimal(y);
        this.y= yg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
        BigDecimal zg = new BigDecimal(z);
        this.z= zg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
    }
}
