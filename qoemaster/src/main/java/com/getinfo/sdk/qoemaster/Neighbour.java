package com.getinfo.sdk.qoemaster;

public class Neighbour {
    public String Type;
    public String EARFCN;
    public String PCI;
    public String RSRP;
    public String RSRQ;
    public Neighbour(){

    }
    public Neighbour(String type,int EARFCN,int PCI,int RSRP,int RSRQ){
        this.Type=type;
        this.EARFCN=EARFCN+"";
        this.PCI=PCI+"";
        this.RSRP=RSRP+"";
        this.RSRQ=RSRQ+"";
    }
}
