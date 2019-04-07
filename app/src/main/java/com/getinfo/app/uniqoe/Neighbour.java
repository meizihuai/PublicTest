package com.getinfo.app.uniqoe;
import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

@SmartTable(name="邻区信息")
public class Neighbour {
    @SmartColumn(id =1,name = "Type")
    public String Type;
    @SmartColumn(id =2,name = "EARFCN")
    public String EARFCN;
    @SmartColumn(id =3,name = "PCI")
    public String PCI;
    @SmartColumn(id =4,name = "RSRP")
    public String RSRP;
    @SmartColumn(id =5,name = "RSRQ")
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