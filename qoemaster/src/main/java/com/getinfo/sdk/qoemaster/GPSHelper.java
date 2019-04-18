package com.getinfo.sdk.qoemaster;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.getinfo.sdk.qoemaster.Interfaces.InterfaceCls;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//手机GPS获取
public class GPSHelper {
    private String TAG="GPSHelper";
    private Context context;
    private InterfaceCls.ILocationInfo iLocationInfo;
    private int satelliteCount;  //GPS可用卫星数量
    double mylon; double mylat; float myaccuracy; double myspeed;double myaltitude;
    //经度   纬度   精度   运动速度  海拔
    private LocationManager locationManager;
    public GPSHelper(Context context,InterfaceCls.ILocationInfo iLocationInfo) {
        this.iLocationInfo = iLocationInfo;
        this.context = context;

    }

    //初始化位置服务以及监听器
    private void initLocation() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //判断GPS是否正常启动
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            iLocationInfo.onError("请打开系统GPS开关");
            return;
        }else{

        }
        //添加卫星状态改变监听
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG,"GPS卫星权限不足");
            iLocationInfo.onError("APP权限不足，无法调用GPS功能");
            return;
        }
        locationManager.addGpsStatusListener(statusListener);
        //1000位最小的时间间隔，1为最小位移变化；也就是说每隔1000ms会回调一次位置信息
        // manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }
    //开启接收GPS变化的工作
    public void Start() {
        try {
            initLocation();
            location();
        } catch (Exception e) {

        }
    }

    //上传GPS数据到接受者，也就是MainActity
    public void UploadGPSData(){
        LocationInfo la=new LocationInfo();
        la.Lon=mylon;
        la.Lat=mylat;
        la.Accuracy=myaccuracy;
        la.Altitude=myaltitude;
        la.Speed=myspeed;
        la.SatelliteCount=satelliteCount;
        iLocationInfo.onNewLocationInfo(la);
    }

    public void UploadGPSData(double lon, double lat, float accuracy, double speed,double altitude) {
        BigDecimal blon = new BigDecimal(lon);
        lon= blon.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

        BigDecimal blat = new BigDecimal(lat);
        lat= blat.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();

        BigDecimal baccuracy = new BigDecimal(accuracy);
        accuracy= baccuracy.setScale(3, BigDecimal.ROUND_HALF_UP).floatValue();
        mylon=lon;
        mylat=lat;
        myaccuracy=accuracy;
        myspeed=speed;
        myaltitude=altitude;
        UploadGPSData();

    }
    //判断GPS开关是否打开
    private boolean isOPen() {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }
    //跳转到GP设置界面，引导打开GPS开关
    private void openGPS() {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>(); // 卫星信号

    private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) { // GPS状态变化时的回调，如卫星数
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("gpsHelper","GPS卫星权限不足");
                return;
            }
            GpsStatus status = locationManager.getGpsStatus(null); //取当前状态
            String satelliteInfo = updateGpsStatus(event, status);
            UploadGPSData();
            Log.i("gpsHelper","GPS卫星:"+satelliteInfo);
            // Log.i("gpsHelperStatus","");
        }
    };
    //更新GPS状态
    private String updateGpsStatus(int event, GpsStatus status) {
        StringBuilder sb2 = new StringBuilder("");
        if (status == null) {
            sb2.append("搜索到卫星个数：" +0);
        } else if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
            int maxSatellites = status.getMaxSatellites();
            Iterator<GpsSatellite> it = status.getSatellites().iterator();
            numSatelliteList.clear();
            int count = 0;
            int snrCount=0;
            while (it.hasNext() && count <= maxSatellites) {
                GpsSatellite s = it.next();
                numSatelliteList.add(s);
                count++;
                if(s.getSnr()!=0){

                }
                if(s.usedInFix()){
                    snrCount++;
                }
            }
            satelliteCount=snrCount;
            sb2.append("搜索到卫星个数：" + count);
            sb2.append("\n");
            sb2.append("搜索到SNR>0卫星个数：" + snrCount);
        }

        return sb2.toString();
    }
    //定位主要方法
    private void location() {
        if (!isOPen()) {
            openGPS();
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Double latitude, longitude;
        boolean bool = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) != null;
        //   Log.i("hasaki","locationManager is null ="+bool);
        if (bool) {

            latitude = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER).getLatitude();
            longitude = locationManager.getLastKnownLocation(
                    LocationManager.GPS_PROVIDER).getLongitude();
            if (longitude != 0 && latitude != 0) {
                UploadGPSData(longitude, latitude, 0, 0,0);
                // Log.i("hasaki","LastKnownLocation1.."+longitude+","+latitude);
            }
        }
        if (true) {

            LocationListener locationListener = new LocationListener() {
                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
                    //  UpdateGpsStatus();
//                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        return;
//                    }
//                    Log.i("gpsHelper","onStatusChanged");
//                    GpsStatus gpsStatus = locationManager.getGpsStatus(null); // 取当前状态
//                    int maxSatellites = gpsStatus.getMaxSatellites();
//                    Iterator<GpsSatellite> it = gpsStatus.getSatellites().iterator();
//                    int count = 0;
//                    while (it.hasNext() && count <= maxSatellites) {
//                        GpsSatellite s = it.next();
//                        if (s.getSnr() != 0)//只有信躁比不为0的时候才算搜到了星
//                        {
//                            count++;
//                        }
//                    }
//                    Log.i("gpsHelper","星数:"+count);

                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {
                    //  Log.i("hasaki","GPS被打开");
                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {
                    //  Log.i("hasaki","GPS关闭");
                }
                // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {

                    if (location != null) {
                        double longitude,latitude;
                        longitude=location.getLongitude();
                        latitude=location.getLatitude();
                        if(longitude!=0 && latitude!=0){
                            float accuracy=0;
                            double speed=0;
                            double altitude=0;
                            try{
                                if (location.hasAccuracy()) {
                                    accuracy=location.getAccuracy();
                                }
                                if (location.hasSpeed()) {
                                    if (location.getSpeed() * 3.6 < 5) {
                                        speed=0;
                                    } else {
                                        speed=location.getSpeed() * 3.6 ;//km/h
                                    }
                                }
                                if(location.hasAltitude()){
                                    altitude=location.getAltitude();
                                }

                            }catch (Exception e){
                                Log.i("gpsHelper","Exception "+e.getMessage());
                            }

                            Log.i("gpsHelper","精度:"+accuracy);
                            Log.i("gpsHelper","速度:"+speed);
                            Log.i("gpsHelper","海拔:"+altitude);
                            UploadGPSData(longitude,latitude,accuracy,speed,altitude);
                            //  Log.i("hasaki","location change..."+longitude+","+latitude);
                        }
                    }
                }
            };
            //   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0, 0, locationListener);
            Location location2 = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location2 != null) {
                latitude = location2.getLatitude(); // 经度
                longitude = location2.getLongitude(); // 纬度
                if(longitude!=0 && latitude!=0){
                    UploadGPSData(longitude,latitude,0,0,0);
                    //  Log.i("hasaki","LastKnownLocation2..."+longitude+","+latitude);
                }
            }
        }
    }
}
