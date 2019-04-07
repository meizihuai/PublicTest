package com.getinfo.app.uniqoe;
//旧版本的视频测试信息，已弃用
public class VLCTestInfo {
    public String BusinessType;
    public long Video_BUFFER_INIT_TIME;
    public long Video_BUFFER_TOTAL_TIME;
    public int Video_Stall_Num;
    public long Video_Stall_TOTAL_TIME;
    public float Video_Stall_Duration_Proportion;
    public int Video_LOAD_Score;
    public int Video_STALL_Score;
    public int USER_Score;
    public int VMOS;
    public long Packet_loss;
    public String CARRIER;
    public String PLMN;
    public String MCC;
    public String MNC;
    public int tac;
    public String enodebid;
    public int cellid;
    public String RSRP;
    public String SINR;
    public String IMSI;
    public String IMEI;
    public String phoneName;
    public String phoneModel;
    public String OS;
    public int PHONE_ELECTRIC_START;
    public int PHONE_ELECTRIC_END;
    public double LON;
    public double LAT;
    public double accuracy;
    public double altitude;
    public double speed;
    public  int satelliteCount;

    public double LON_END;
    public double LAT_END;
    public String COUNTRY;
    public String PROVINCE;
    public String CITY;
    public String ADDRESS;
    public String netType;
    public long SCREEN_RESOLUTION_LONG;
    public long SCREEN_RESOLUTION_WIDTH;
    public String VIDEO_CLARITY;
    public String VIDEO_CODING_FORMAT;
    public long VIDEO_BITRATE;
    public int FPS;
    public long VIDEO_TOTAL_TIME;
    public long VIDEO_PLAY_TOTAL_TIME;
    public long preparedTime;
    public float BVRate;
    public String STARTTIME;
    public long file_Len;
    public String File_NAME;
    public long LIGHT_INTENSITY;
    public long PHONE_SCREEN_BRIGHTNESS;
    public String SIGNAL_Info;
    public String ENVIRONMENTAL_NOISE;
    public long Called_Num;
    public long PING_AVG_RTT;
    public long ACCELEROMETER_DATA;
    public float INSTAN_DOWNLOAD_SPEED;
    public String VIDEO_SERVER_IP;
    public String UE_INTERNAL_IP;
    public long MOVE_SPEED;
    public String apkVersion;
    public VLCTestInfo(PhoneInfo npi){
        if(npi==null)return;
        this.BusinessType="流媒体";
        this.phoneModel = npi.phoneModel;
        this.phoneName = npi.phoneName;
        this.CARRIER = npi.carrier;
        this.IMSI = npi.IMSI;
        this.IMEI = npi.IMEI;
        this.cellid = npi.cellId;
        this.enodebid=npi.eNodeBId+"";
        this.tac = npi.TAC;
        this.netType = npi.netType;
        this.SIGNAL_Info = npi.sigNalInfo;
        this.LON = npi.lon;
        this.LAT = npi.lat;
        this.ADDRESS = npi.address;
        this.OS = npi.phoneOS;
        this.RSRP = npi.RSRP+"";
        this.SINR = npi.SINR+"";
        this.accuracy=npi.accuracy;
        this.altitude=npi.altitude;
        this.speed=npi.gpsSpeed;
        this.satelliteCount=npi.satelliteCount;
        this.apkVersion = npi.apkVersion;
    }

    public void setPhoneInfo(PhoneInfo npi) {
        if(npi==null)return;
        this.BusinessType="流媒体";
        this.phoneModel = npi.phoneModel;
        this.phoneName = npi.phoneName;
        this.CARRIER = npi.carrier;
        this.IMSI = npi.IMSI;
        this.IMEI = npi.IMEI;
        this.cellid = npi.cellId;
        this.enodebid=npi.eNodeBId+"";
        this.tac = npi.TAC;
        this.netType = npi.netType;
        this.SIGNAL_Info = npi.sigNalInfo;
        this.LON = npi.lon;
        this.LAT = npi.lat;
        this.ADDRESS = npi.address;
        this.OS = npi.phoneOS;
        this.RSRP = npi.RSRP+"";
        this.SINR = npi.SINR+"";
        this.apkVersion = npi.apkVersion;
        this.accuracy=npi.accuracy;
        this.altitude=npi.altitude;
        this.speed=npi.gpsSpeed;
        this.satelliteCount=npi.satelliteCount;
    }
}
