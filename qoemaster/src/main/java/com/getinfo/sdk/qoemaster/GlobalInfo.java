package com.getinfo.sdk.qoemaster;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
public class GlobalInfo {
    public static String jarVersion="1.0.0.1";
    public static String apkName="QoEMaster";
    private static String defaultServerUrl="http://111.53.74.132:7062/default.ashx";
    private static String settingFileName="qoeSetting.txt";
    public  static String AID="";
    public  static String serverUrl=defaultServerUrl;
    public  static float PING_AVG_RTT;
    private static PhoneInfo pi;
    private  static LocationInfo locationInfo;
    private static Object laiLock=new Object();
    private static Object lock=new Object();
    private static List<CellInfo> allCellInfos;
    private static Setting mSetting;
    public static String apkVersion;

    public static String myDeviceImei,myDeviceImsi,myVersion;
    private   static  int LIGHT_INTENSITY;
    public static void setDeviceImeiAndImsi(String imei,String Imsi){
        myDeviceImei=imei;
        myDeviceImsi=Imsi;
    }

    public static LocationInfo getLocationInfo() {
        synchronized (laiLock){
            return locationInfo;
        }
    }

    public static void setLocationInfo(LocationInfo locationInfo) {
        synchronized (laiLock){
            GlobalInfo.locationInfo = locationInfo;
        }

    }
    public static void setPi(PhoneInfo pi){
        synchronized (lock){
            GlobalInfo.pi=pi;
        }
    }
    public  static  PhoneInfo getPi(){
        synchronized (lock){
            return GlobalInfo.pi;
        }
    }

    public static List<CellInfo> getAllCellInfos() {
        return allCellInfos;
    }

    public static void setAllCellInfos(List<CellInfo> allCellInfos) {
        GlobalInfo.allCellInfos = allCellInfos;
    }

    public static Setting getSetting(Context context) {
        if(mSetting==null){
            iniSetting(context);
        }
        return mSetting;
    }

    public static void setSetting(Context context,Setting setting) {
        GlobalInfo.mSetting = setting;
        try{
            Gson gson=new Gson();
            String json=gson.toJson(setting);
            FileHelper.fileWriteAllText(context,settingFileName,json);
            Log.i("setSetting",json);
        }catch (Exception e){

        }
    }
    public static void iniSetting(Context context){
        try{
            String txt=FileHelper.fileReadAllText(context,settingFileName);
            if("".equals(txt)){
                defaultSetting(context);
                return;
            }
            Gson gson=new Gson();
            Setting setting=gson.fromJson(txt,Setting.class);
            if(setting==null){
                defaultSetting(context);
                return;
            }
            if(setting.serverUrl==null){
                defaultSetting(context);
                return;
            }
            if("".equals(setting.serverUrl)){
                defaultSetting(context);
                return;
            }
            mSetting = setting;
        }catch (Exception e){
            Log.i("iniSettingErr",e.toString());
            defaultSetting(context);
        }
    }
    public   static void defaultSetting(Context context){
        try{
            Setting setting=new Setting();
            setting.switchQoEScore=true;
            setting.switchQoEScreenRecord=true;
            setting.videoWantType="全部";
            setting.serverUrl=defaultServerUrl;
            Gson gson=new Gson();
            String json=gson.toJson(setting);
            FileHelper.fileWriteAllText(context,settingFileName,json);
            mSetting=setting;
        }catch (Exception e){
            Log.i("iniSettingErr","defaultSetting->"+e.toString());
        }
    }
    public static String getSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    public static int getLightIntensity() {
        return LIGHT_INTENSITY;
    }

    public static void setLightIntensity(int lightIntensity) {
        LIGHT_INTENSITY = lightIntensity;
    }
}
