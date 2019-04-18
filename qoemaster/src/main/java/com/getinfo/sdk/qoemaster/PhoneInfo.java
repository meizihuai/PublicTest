package com.getinfo.sdk.qoemaster;
import java.util.List;

import java.util.List;
public class PhoneInfo {
    public  String AID;
    public String DATETIME; //采集时间
    public String businessType; //业务类型
    public String phoneModel; //手机型号
    public String phoneName;   //手机名称
    public String phoneOS;   //Android版本
    public String phonePRODUCT;   //手机厂商
    public String carrier;   //运营商
    public String IMSI;   //IMSI
    public String IMEI;   //IMEI
    public int RSRP;   //RSRP
    public int SINR;   //SINR
    public int RSRQ;   //RSRQ
    public int TAC;   //TAC
    public int PCI;   //PCI
    public int EARFCN;   //频点
    public int CI;   //ECI

    public int PHONE_SCREEN_BRIGHTNESS;   //屏幕亮度

    public int MNC;   //MNC
    public String wifi_SSID;   //当前连接的wifi名称
    public String wifi_MAC;   //当前连接的wifi mac地址
    public float PING_AVG_RTT;   //平均ping时延
    public double FREQ;   //频点
    public String cpu;   //CPU型号
    public String ADJ_SIGNAL;   //邻区综合信息
    public List<Neighbour> neighbourList;   //邻区

    public int Adj_ECI1;   //邻区1 ECI
    public int Adj_RSRP1;   //邻区1 RSRP
    public int Adj_SINR1;   //邻区1 SINR
    public int isScreenOn;   //手机是否开屏
    public int isGPSOpen;   //GPS开关是否打开
    public  int PHONE_ELECTRIC;   //手机电量
    public XYZaSpeedInfo xyZaSpeed;   //三轴加速度

    public int eNodeBId;   //eNodeBId
    public int cellId;   //cellId
    public String netType;   //网络类型
    public String mainCellIdentity;   //主小区的网络类型 LTE WCDMA CDMA GSM 之类

    public String sigNalType;   //信号类型
    public String sigNalInfo;   //信号详细信息
    public double lon;   //经度
    public double lat;   //纬度
    public double accuracy;   //精度
    public double altitude;   //海拔
    public double gpsSpeed;   //移动速度
    public int satelliteCount;   //可用卫星数量
    public String address;   //详细地址
    public String apkVersion;   //apk版本号
    public String apkName;   //apk名称

    public  long HTTP_RESPONSE_TIME;
    public  int VMOS;
    public String HTTP_URL;
    public  long HTTP_BUFFERSIZE;
}
