package com.getinfo.sdk.qoemaster;



import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.getinfo.sdk.qoemaster.DeviceHelper;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.Interfaces.InterfaceCls;
import com.getinfo.sdk.qoemaster.Neighbour;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.getinfo.sdk.qoemaster.XYZaSpeedInfo;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.WIFI_SERVICE;
//获取手机网络信息封装类 获取PhoneInfo
public class GetPhoneInfoHelper {
    private Context context;
    private PhoneInfo PI;
    private InterfaceCls.IGetPhoneInfo iGetPhoneInfo;
    PhoneStateListener mylistener;
    TelephonyManager tm;
    private boolean getedCpu = false;

    //返回PhoneInfo给使用者，返回之前先本地获取一个最新的PhoneInfo
    public PhoneInfo GetPhoneInfo() {
        GetNewPhoneInfo();
        return this.PI;
    }

    public GetPhoneInfoHelper(Context context,  InterfaceCls.IGetPhoneInfo iGetPhoneInfo) {
        this.context = context;
        this.iGetPhoneInfo = iGetPhoneInfo;
        GetXYZaSpeed getXYZaSpeed=new GetXYZaSpeed(context); //实例化一个三轴加速度传感器信息采集器
        getXYZaSpeed.setChanger(changer); //设置监听器
        getXYZaSpeed.start(); //开始工作
    }
    //三轴加速度传感器信息采集器的监听器
    private GetXYZaSpeed.Changer changer=new GetXYZaSpeed.Changer() {
        @Override
        public void onChange(float x, float y, float z) {
            if(PI==null)return;
            PI.xyZaSpeed=new XYZaSpeedInfo(x,y,z);
        }
    };
    //开始收集手机网络信息以及开启各种监听器
    public void StartWork() {
        PI = new PhoneInfo();
        tm = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        iniListener(); //初始化监听器
        tm.listen(mylistener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS); //设置RSRP监听器
//      tm.listen(mylistener, PhoneStateListener.LISTEN_SERVICE_STATE);
//      tm.listen(mylistener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        try {
            GetNewPhoneInfo();
            PI.netType = getCurrentNetType(); //获取当前数据类型
            //UploadPhoneInfo();
        } catch (Exception e) {
            Log.i("hasaki", e.toString());
        }

    }
    private class ENBAndCellId {
        public int eNodebId;
        public int cellId;

        public ENBAndCellId(int eci) {
            if (eci > 0) {
                String h = Integer.toHexString(eci);
                if (h.length() > 2) {
                    String tail = h.substring(h.length() - 2, h.length());
                    this.cellId = Integer.valueOf(tail, 16);
                    String head = h.substring(0, h.length() - 2);
                    int eNB = Integer.valueOf(head, 16);
                    this.eNodebId = eNB;
                }
            } else {
                this.cellId = 0;
                this.eNodebId = 0;
            }
        }
    }
    //获取邻区信息
    private  List<Neighbour> GetNeiberCell(PhoneInfo pi){
        List<Neighbour> neighbourList = new ArrayList<>();
        List<CellInfo> allCellinfo= GlobalInfo.getAllCellInfos();
        if (allCellinfo == null) return null;
        Log.i("neighbourHasaki","1,allCellinfo.size()="+allCellinfo.size());
        for (int i = 0; i < allCellinfo.size(); i++) {
            CellInfo cellInfo = allCellinfo.get(i);
            if (cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoGsm);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int ECI = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mCid");
                    if(ECI==pi.CI)continue;
                    //  if (ECI == 2147483647) continue;
                    int TAC = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mLac");
                    int FREQ = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mArfcn");
                    int SINR = jsonObject.getJSONObject("mCellSignalStrengthGsm").getInt("mSignalStrength");
                    int RSRP = 0;
                    int PCI=0;
                    int EARFCN=FREQ;
                    int RSRQ=0;
                    ENBAndCellId enbAndCellId = new ENBAndCellId(ECI);
                    Neighbour neighbour = new Neighbour("GSM",EARFCN,PCI, RSRP, RSRQ);
                    neighbourList.add(neighbour);
                } catch (Exception e) {

                }
            } else if (cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoWcdma);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int ECI = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mCid");
                    // if (ECI == 2147483647) continue;
                    if(ECI==pi.CI)continue;
                    int TAC = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mLac");
                    int PCI = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mPci");
                    int EARFCN = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mUarfcn");
                    int SINR = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");
                    int RSRP = 0;
                    int RSRQ=0;
                    ENBAndCellId enbAndCellId = new ENBAndCellId(ECI);
                    Neighbour neighbour = new Neighbour("WCDMA",EARFCN, PCI,RSRP, RSRQ);
                    neighbourList.add(neighbour);
                } catch (Exception e) {

                }
            } else if (cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoLte);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    int ECI = jsonObject.getJSONObject("mCellIdentityLte").getInt("mCi");
                    //  if (ECI == 2147483647) continue;
                    if(ECI==pi.CI)continue;
                    int TAC = jsonObject.getJSONObject("mCellIdentityLte").getInt("mTac");
                    int PCI = jsonObject.getJSONObject("mCellIdentityLte").getInt("mPci");
                    int MNC = jsonObject.getJSONObject("mCellIdentityLte").getInt("mMnc");
                    int EARFCN = jsonObject.getJSONObject("mCellIdentityLte").getInt("mEarfcn");

                    int RSRP = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mRsrp");
                    int RSRQ = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mRsrq");
                    int SINR = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");
                    ENBAndCellId enbAndCellId = new ENBAndCellId(ECI);
                    Neighbour neighbour = new Neighbour("LTE",EARFCN,PCI, RSRP, RSRQ);
                    neighbourList.add(neighbour);
                } catch (Exception e) {

                }
            } else if (cellInfo instanceof CellInfoCdma) {
                continue;
//                    CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
//                    Gson gson = new Gson();
//                    String json = gson.toJson(cellInfoCdma);
//                    try {
//                        JSONObject jsonObject = new JSONObject(json);
//                        int ECI = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mBasestationId");
//                        //if (ECI == 2147483647) continue;
//                        if(ECI==pi.CI)continue;
//                        int TAC = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mNetworkId");
//                        int RSRP = jsonObject.getJSONObject("mCellSignalStrengthCdma").getInt("mCdmaDbm");
//                        int SINR = 0;
//                        int PCI=0;
//                        int EARFCN=0;
//                        ENBAndCellId enbAndCellId = new ENBAndCellId(ECI);
//                        Neighbour neighbour = new Neighbour("CDMA", EARFCN, PCI,RSRP, SINR);
//                        neighbourList.add(neighbour);
//                    } catch (Exception e) {
//
//                    }
            }
        }
        Log.i("neighbourHasaki","2");
        String mainidenty = pi.mainCellIdentity.toLowerCase();
        if ("".equals(mainidenty)) return null;
        if (neighbourList == null) return null;
        if (neighbourList.size() == 0) return null;
        Log.i("neighbourHasaki","3");
        List<Neighbour> nb = new ArrayList<>();
        String intMax=Integer.MAX_VALUE+"";
        Log.i("neighbourHasaki","intMax="+intMax);
        String zero="0";
        for (int i = 0; i < neighbourList.size(); i++) {
            Neighbour neighbour = neighbourList.get(i);
            if (neighbour.Type.toLowerCase().equals(mainidenty)) {
                if (nb.size() < 6) {
                    String EARFCN=neighbour.EARFCN.equals(intMax)?"0":neighbour.EARFCN;
                    String PCI=neighbour.PCI.equals(intMax)?"0":neighbour.PCI;
                    String RSRP=neighbour.RSRP.equals(intMax)?"0":neighbour.RSRP;
                    String RSRQ=neighbour.RSRQ.equals(intMax)?"0":neighbour.RSRQ;
                    if(zero.equals(EARFCN)&&zero.equals(PCI)&&zero.equals(RSRP)&&zero.equals(RSRQ)){

                    }else{
                        neighbour.EARFCN=EARFCN;
                        neighbour.PCI=PCI;
                        neighbour.RSRP=RSRP;
                        neighbour.RSRQ=RSRQ;
                        nb.add(neighbour);
                    }
                }
            }
        }
        for (int i = 0; i < neighbourList.size(); i++) {
            Neighbour neighbour = neighbourList.get(i);
            if (!neighbour.Type.toLowerCase().equals(mainidenty)) {
                if (nb.size() < 6) {
                    String EARFCN=neighbour.EARFCN.equals(intMax)?"0":neighbour.EARFCN;
                    String PCI=neighbour.PCI.equals(intMax)?"0":neighbour.PCI;
                    String RSRP=neighbour.RSRP.equals(intMax)?"0":neighbour.RSRP;
                    String RSRQ=neighbour.RSRQ.equals(intMax)?"0":neighbour.RSRQ;
                    if(zero.equals(EARFCN)&&zero.equals(PCI)&&zero.equals(RSRP)&&zero.equals(RSRQ)){

                    }else{
                        neighbour.EARFCN=EARFCN;
                        neighbour.PCI=PCI;
                        neighbour.RSRP=RSRP;
                        neighbour.RSRQ=RSRQ;
                        nb.add(neighbour);
                    }
                }
            }
        }
        Log.i("neighbourHasaki","4 ,nb.size="+nb.size());
        return nb;
    }
    //上传PhoneInfo到 handler , 向使用者推送新 PhoneInfo
    public void UploadPhoneInfo() {
        try {
            if (PI == null) return;
            PI.neighbourList=GetNeiberCell(PI);
            PI.PHONE_SCREEN_BRIGHTNESS= DeviceHelper.getSystemBrightness(context);
            PI.PHONE_ELECTRIC = DeviceHelper.getBattery(context);
            iGetPhoneInfo.onNewPhoneInfo(PI);
        } catch (Exception e) {

        }

    }
    //获取CPU型号
    public static String getCpuName() {

        String str1 = "/proc/cpuinfo";
        String str2 = "";

        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr);
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (str2.contains("Hardware")) {
                    return str2.split(":")[1];
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return null;

    }

    //获取新PhoneInfo

    private void GetNewPhoneInfo() {
        Log.i("signalStrength", "获取新PhoneInfo");
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (PI == null) PI = new PhoneInfo();
        String IMSI = tm.getSubscriberId();
        String IMEI = tm.getDeviceId();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            IMEI=tm.getImei();
        }
        if (IMSI == null) IMSI = "";
        if (IMEI == null) IMEI = "";
        PI.IMSI = IMSI;
        PI.IMEI = IMEI;
        if (getedCpu == false) {
            PI.cpu = getCpuName();
            getedCpu = true;
            Log.i("cpuinfo", PI.cpu);
        }
        if (PI.IMSI == null) {
            if (TelephonyManager.SIM_STATE_READY == tm.getSimState()) {
                String operator = tm.getSimOperator();
                if (operator != null) {
                    if (operator.equals("46000") || operator.equals("46002") || operator.equals("46004") || operator.equals("46007")) {
                        PI.carrier = "中国移动";
                    } else if (operator.equals("46001") || operator.equals("46006") || operator.equals("46009")) {
                        PI.carrier = "中国联通";
                    } else if (operator.equals("46003") || operator.equals("46005") || operator.equals("46011")) {
                        PI.carrier = "中国电信";
                    }
                }
            }
        } else {
            if (PI.IMSI.startsWith("46000") || PI.IMSI.startsWith("46002") || PI.IMSI.startsWith("46004") || PI.IMSI.startsWith("46007")) {
                PI.carrier = "中国移动";
            } else if (PI.IMSI.startsWith("46001") || PI.IMSI.startsWith("46006") || PI.IMSI.startsWith("46009")) {
                PI.carrier = "中国联通";
            } else if (PI.IMSI.startsWith("46003") || PI.IMSI.startsWith("46005") || PI.IMSI.startsWith("46011")) {
                PI.carrier = "中国电信";
            }
        }
        if (PI.carrier != "中国电信") {
            Log.i("newrsrpErr","step=1");
            if(tm!=null){
                GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                if(gsmCellLocation!=null){
                    PI.TAC = gsmCellLocation.getLac();
                    PI.CI = gsmCellLocation.getCid();
                }else{
                    Log.i("newrsrpErr","gsmCellLocation is null");
                }
            }else{
                Log.i("newrsrpErr","tm is null");
            }
        }
        PI.phoneName = Build.DEVICE;
        PI.phoneModel = Build.BRAND + " " + Build.MODEL;
        PI.phoneOS = Build.VERSION.RELEASE;
        PI.phonePRODUCT = Build.PRODUCT;
        List<CellInfo> cellInfos = getServerCellInfo();
        GlobalInfo.setAllCellInfos(cellInfos);
        SiteCellInfo siteCellInfo = null;
        if (cellInfos != null) {
            String mainCellIdentity=PI.mainCellIdentity==null?"LTE":PI.mainCellIdentity;
            siteCellInfo = new SiteCellInfo(cellInfos,mainCellIdentity,PI.CI);
            if(siteCellInfo != null){
                PI.CI = siteCellInfo.ECI;
                PI.TAC = siteCellInfo.TAC;
                PI.PCI = siteCellInfo.PCI;
                PI.FREQ = siteCellInfo.FREQ;
                PI.MNC = siteCellInfo.MNC;
                PI.SINR = siteCellInfo.SINR;
                PI.EARFCN=siteCellInfo.FREQ;
                PI.PCI=siteCellInfo.PCI;

                PI.Adj_ECI1 = siteCellInfo.Adj_ECI1;
                PI.Adj_RSRP1 = siteCellInfo.Adj_RSRP1;
                PI.Adj_SINR1 = siteCellInfo.Adj_SINR1;
                PI.ADJ_SIGNAL = siteCellInfo.ADJ_SIGNAL;

            }
            if (PI.carrier != "中国电信" && PI.CI==0) {
                Log.i("newrsrpErr","step=1");
                if(tm!=null){
                    GsmCellLocation gsmCellLocation = (GsmCellLocation) tm.getCellLocation();
                    if(gsmCellLocation!=null){
                        PI.TAC = gsmCellLocation.getLac();
                        PI.CI = gsmCellLocation.getCid();
                        PI.EARFCN=siteCellInfo.FREQ;
                        PI.PCI=siteCellInfo.PCI;
                    }else{
                        Log.i("newrsrpErr","gsmCellLocation is null");
                    }
                }else{
                    Log.i("newrsrpErr","tm is null");
                }
            }
        }
        int eci = PI.CI;
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isInteractive();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
        PI.isScreenOn = isScreenOn ? 1 : 0;
        Log.i("PI.isScreenOn", "PI.isScreenOn=" + PI.isScreenOn);
        try {
            if (eci > 0) {
                String h = Integer.toHexString(eci);
                if (h.length() > 2) {
                    String tail = h.substring(h.length() - 2, h.length());
                    int cellid = Integer.valueOf(tail, 16);
                    PI.cellId = cellid;
                    String head = h.substring(0, h.length() - 2);
                    int eNB = Integer.valueOf(head, 16);
                    PI.eNodeBId = eNB;
                }
            } else {
                PI.cellId = 0;
                PI.eNodeBId = 0;
            }
        } catch (Exception e) {
            Log.i("toHexString", e.getMessage());
        }
    }


    //获取所有能监测到的消息，包括主小区和邻区
    public List<CellInfo> getServerCellInfo() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            List<CellInfo> allCellinfo = tm.getAllCellInfo();
            return allCellinfo;
        } catch (Exception e) {
            GsmCellLocation location = (GsmCellLocation) tm.getCellLocation();
            int cid = location.getCid();
            int tac = location.getLac();
            return null;
        }
    }
    //  初始化信号状态变化等监听器
    private void iniListener() {
        mylistener = new PhoneStateListener() {
            @Override
            public void onCellInfoChanged(List<CellInfo> cellInfo) {
                super.onCellInfoChanged(cellInfo);
                Log.i("cellinfo", "基站信息发生改变");
                GetNewPhoneInfo();
            }

            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                super.onSignalStrengthsChanged(signalStrength);
                try {
                    Log.i("cellinfo", "基站信号发生改变");
                    if (signalStrength == null) return;
                    String signalInfo = signalStrength.toString();
                    if ("".equals(signalInfo)) return;
                    String[] params = signalInfo.split(" ");
                    if (params.length < 11) return;
                    int Itedbm = Integer.parseInt(params[9]);
                    int SINR = Integer.parseInt(params[8]);
                    int RSRQ = Integer.parseInt(params[10]);
                    int asu = signalStrength.getGsmSignalStrength();
                    int dbm = -113 + 2 * asu;
                    PI.sigNalType = "unknow " + PI.carrier;
                    if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE) {
                        PI.sigNalType = "4G " + PI.carrier;
                        PI.mainCellIdentity = "LTE";
                        PI.RSRP = (int) signalStrength.getClass().getMethod("getLteRsrp").invoke(signalStrength);
                        //getLteSignalStrength
                        PI.SINR = (int) signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength);
                        PI.SINR = PI.SINR / 10;
                        PI.RSRQ = (int) signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength);
                        PI.sigNalInfo = "LTE:" + Itedbm + "dBm,Detail:" + signalInfo;
                    } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GSM
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EHRPD
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {

                        PI.mainCellIdentity = (PI.carrier == "中国电信") ? "CDMA" : "GSM";
                        PI.sigNalType = "2G " + PI.carrier;
                        PI.RSRP = (int) signalStrength.getClass().getMethod("getGsmDbm").invoke(signalStrength);
//                        PI.SINR = (double)signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength);
//                        PI.RSRQ = (double)signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength);
                        PI.SINR = SINR;
                        PI.RSRQ = RSRQ;
                        PI.sigNalInfo = "GSM:" + dbm + "dBm,Detail:" + signalInfo;
                    } else if (tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSDPA
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPA
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSPAP
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_HSUPA
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_A
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_0
                            || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                        if (PI.carrier == "中国联通") PI.mainCellIdentity = "WCDMA";
                        if (PI.carrier == "中国移动") PI.mainCellIdentity = "GSM";
                        if (PI.carrier == "中国电信") PI.mainCellIdentity = "CDMA";
                        PI.sigNalType = "3G " + PI.carrier;
                        PI.RSRP = (int) signalStrength.getClass().getMethod("getTdScdmaDbm").invoke(signalStrength);
//                      PI.SINR = (double)signalStrength.getClass().getMethod("getLteRssnr").invoke(signalStrength);
//                      PI.RSRQ = (double)signalStrength.getClass().getMethod("getLteRsrq").invoke(signalStrength);
                        PI.SINR = SINR;
                        PI.RSRQ = RSRQ;
                        PI.sigNalInfo = "WCDMA:" + dbm + "dBm,Detail:" + signalInfo;
                    }
                    PI.netType = getCurrentNetType();
                    if ("WiFi".equals(PI.netType)) {
                        PI.wifi_SSID = getWiFiSSID();
                        Log.i("wifi_SSID", "wifi_SSID=" + PI.wifi_SSID);
                    } else {
                        PI.wifi_SSID = "";
                    }
                    Log.i("NewRSRP", "RSRP:" + PI.RSRP);
                    Log.i("NewRSRP", "SINR:" + PI.SINR);
                    Log.i("NewRSRP", "RSRQ:" + PI.RSRQ);
                    Log.i("NewRSRP", "Detail:" + PI.sigNalInfo);
                    GetNewPhoneInfo();
                    UploadPhoneInfo();
                } catch (Exception e) {
                    Log.i("NewRSRP", "new signalStrength error " + e.toString());
                }
            }

            @Override
            public void onServiceStateChanged(ServiceState state) {
                // mServiceState = state;
            }

            @Override
            public void onDataConnectionStateChanged(int state, int networkType) {
            }

        };
    }

    /**
     * 获取当前连接WIFI的SSID
     */
    public String getWiFiSSID() {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
                String s = winfo.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"' && s.charAt(s.length() - 1) == '"') {
                    return s.substring(1, s.length() - 1);
                }
            }
        }
        return "";
    }


    //获取网络类型
    private String getCurrentNetType() {
        try {
            String type = "unknown";
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(context.CONNECTIVITY_SERVICE);
            NetworkInfo info = cm.getActiveNetworkInfo();
            if (info == null) {
                type = "null";
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                type = "WiFi";
            } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                int subType = info.getSubtype();
                if (subType == TelephonyManager.NETWORK_TYPE_GSM
                        || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_CDMA
                        || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_GPRS
                        || tm.getNetworkType() == TelephonyManager.NETWORK_TYPE_EDGE) {
                    type = "2G";
                } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS
                        || subType == TelephonyManager.NETWORK_TYPE_HSDPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSPA
                        || subType == TelephonyManager.NETWORK_TYPE_HSPAP
                        || subType == TelephonyManager.NETWORK_TYPE_HSUPA
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_A
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                        || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                    type = "3G";
                } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {   // LTE是3g到4g的过渡，是3.9G的全球标准
                    type = "4G";
                }else if(subType==19){
                    type = "4G+";
                }else{
                    type = "4G";
                }

            }
            String kk="";
            if("unknown".equals(type)){
                kk="unknown";
            }else if("null".equals(type)){
                kk="null";
            }else{
                kk=info.getSubtype()+"";
            }
            Log.i("find4gplus",kk);
            return type;
        } catch (Exception e) {
            Log.i("signalStrength", "new signalStrength error 3 " + e.toString());
        }
        return "";
    }

}
