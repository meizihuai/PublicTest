package com.getinfo.sdk.qoemaster;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.content.Context.BATTERY_SERVICE;

//一些设备信息的获取
public class DeviceHelper {
    //获取手机屏幕亮度
    public static int getSystemBrightness(Context context) {
        int systemBrightness=0;
        try {
            ContentResolver contentResolver=context.getContentResolver();
            systemBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness;
    }
    //获取手机电量
    public static int getBattery(Context context){
        BatteryManager batteryManager = (BatteryManager)context.getSystemService(BATTERY_SERVICE);
        int battery = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        return  battery;
    }
    public static boolean isPhoneVertical(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if(height>width){
            return true;
        }else{
            return  false;
        }
    }
    public  static int getPhoneWidth(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return  width;
    }
    public  static int getPhoneHeight(Activity activity){
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        return  height;
    }
    public  static String  GetNewUUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }
    public static boolean isGPSOpen(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps;
//        if (gps || network) {
//            return true;
//        }
//        return false;
    }
    public static double getGis(double lon1, double lat1, double lon2, double lat2) {
        if (lon1 == 0 || lat1 == 0 || lon2 == 0 || lat2 == 0) {
            return 0;
        }
        double R = 6378137.0;
        //将角度转化为弧度
        double radLat1 = (lat1 * Math.PI / 180.0);
        double radLat2 = (lat2 * Math.PI / 180.0);
        double radLog1 = (lon1 * Math.PI / 180.0);
        double radLog2 = (lon2 * Math.PI / 180.0);
        //纬度的差值
        double a = radLat1 - radLat2;
        //经度差值
        double b = radLog1 - radLog2;
        //弧度长度
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        //获取长度
        s = s * R;
        //返回最接近参数的 long。结果将舍入为整数：加上 1/2
        s = Math.round(s * 10000) / 10000;
        return s;
    }
    public static String getNowTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
    public static boolean isWifiConnect(Activity activity) {
        ConnectivityManager connManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifiInfo.isConnected();
    }
    public static String GetNetSpeed(Long d){
        return  GetNetSpeedbps(d);
//        if(d<0){
//            return "0 B/s";
//        }
//        if(d<1024){
//            return d+" B/s";
//        }
//        if(d<1024*1024){
//            double k=1024;
//            double net=d/k;
//            return  new DecimalFormat("0.0").format(net)+" KB/s";
//        }
//        double k1=1024*1024;
//        double mb=d/k1;
//        return   new DecimalFormat("0.0").format(mb)+" MB/s";
    }
    public static String GetNetSpeedbps(Long d){
        d=d*8;
        if(d<0){
            return "0 b/s";
        }
        if(d<1024){
            return d+" b/s";
        }
        if(d<1024*1024){
            double k=1024;
            double net=d/k;
            return new DecimalFormat("0.0").format(net)+" Kb/s";
        }
        double k1=1024*1024;
        double mb=d/k1;
        return   new DecimalFormat("0.0").format(mb)+" Mb/s";
    }
    /**
     * 判断是否包含SIM卡
     *
     * @return 状态
     */
    public static boolean hasSimCard(Context context) {
        TelephonyManager telMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        boolean result = true;
        switch (simState) {
            case TelephonyManager.SIM_STATE_ABSENT:
                result = false; // 没有SIM卡
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                result = false;
                break;
        }
        return result;
    }


}
