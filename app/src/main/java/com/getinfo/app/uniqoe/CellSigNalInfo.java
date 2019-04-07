package com.getinfo.app.uniqoe;
import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

import java.text.SimpleDateFormat;
import java.util.Date;
//最近信号记录  用于表格
@SmartTable(name="最近信号记录")
public class CellSigNalInfo {
    @SmartColumn(id =1,name = "Time")
    public  String Time;
    @SmartColumn(id =2,name = "Tac")
    public String Tac;
    @SmartColumn(id =3,name = "eNBId")
    public String eNB;
    @SmartColumn(id =4,name = "CellId")
    public String CellId;
    @SmartColumn(id =5,name = "RSRP")
    public String RSRP;
    @SmartColumn(id =6,name = "SINR")
    public String SINR;
    public CellSigNalInfo(int Tac,int eNB,int CellId,int RSRP,int SINR){
        this.Tac=Tac+"";
        this.eNB=eNB+"";
        this.CellId=CellId+"";
        this.RSRP=RSRP+"";
        this.SINR=SINR+"";
       // SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");// HH:mm:ss
        Date date = new Date(System.currentTimeMillis());
        this.Time=simpleDateFormat.format(date);
    }

}
