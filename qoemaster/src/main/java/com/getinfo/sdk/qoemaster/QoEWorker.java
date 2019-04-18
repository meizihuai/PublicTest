package com.getinfo.sdk.qoemaster;

import android.content.Context;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.widget.Toast;

import com.getinfo.sdk.qoemaster.Interfaces.InterfaceCls;
import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.LineNumberReader;

import javax.microedition.khronos.opengles.GL;

public class QoEWorker {
    private Context context;
    private String TAG="QoEWorker";
   private String businessType = "QOER";
    private String QOER_HTTP_URL = "http://111.53.74.132:7062/default.ashx/?func=Test"; //http://111.53.74.132:7062/default.ashx/?func=Test
    private long QOER_HTTP_Response_Time = 0;
    private long QOER_HTTP_BufferSize = 0;
    private GPSHelper gpsHelper;
    private GetPhoneInfoHelper getPhoneInfoHelper;
    private  boolean islogined=false;
    private  boolean isSetedImeiAndImsi=false;
    private InterfaceCls.IQoEWorkerInfo iQoEWorkerInfo;

    /**
     * 初始化
     *
     * @param context
     * @param iQoEWorkerInfo
     */
    public void init(Context context, InterfaceCls.IQoEWorkerInfo iQoEWorkerInfo) {
        this.context = context;
        this.iQoEWorkerInfo=iQoEWorkerInfo;
        String apkVersion=APKVersionCodeUtils.getVerName(context);
        String apkName = "QoEMaster";
        setApkName(apkName);
        setApkVersion(apkVersion);
        boolean isHasSimCard = DeviceHelper.hasSimCard(context);
        if (!isHasSimCard) {
            Toast.makeText(context, "没有监测到SIM卡，请检查SIM卡状态", Toast.LENGTH_LONG).show();
            iQoEWorkerInfo.onError("没有监测到SIM卡，请检查SIM卡状态", "init");
            return;
        }
        GlobalInfo.iniSetting(context);
    }
    public  void startWork(){
        Setting setting = GlobalInfo.getSetting(context);
        UploadDataHelper.getInstance().setServerURL(setting.serverUrl);
        QoEVideoSource.serverURL = setting.serverUrl;
        GlobalInfo.serverUrl = setting.serverUrl;
        LightSensorManager.getInstance().start(context);
        startQOEHttpTestLoop();
        startPing();
        startGPSHelper();
        startGetPhoneInfoHelper();
        startUploadQoERDataToServer();
    }
    public void setApkName(String apkName){
        GlobalInfo.apkName=apkName;
    }
    public void  setApkVersion(String apkVersion){
        GlobalInfo.apkVersion=apkVersion;
    }


    private void startQOEHttpTestLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        HTTPHelper.TestResponseTime(QOER_HTTP_URL, true, new HTTPHelper.HTTPResponseTime() {
                            @Override
                            public void OnResponseTime(long responseTime, long bufferTotalTime, long bufferTotalSize) {
                                QOER_HTTP_Response_Time = responseTime;
                                QOER_HTTP_BufferSize = bufferTotalSize;
                                Log.i("needGetBufferTotalTime", "totalBufferSize=" + bufferTotalSize);
                            }
                        });
                    } catch (Exception e) {

                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                }

            }
        }).start();
    }

    //开始循环ping百度官网
    private void startPing() {
        final String pingbaidu = "pingthread";
        Log.i(pingbaidu, "start pingthread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HTTPHelper.GetH(GlobalInfo.serverUrl + "?func=GetPingIP", new HTTPHelper.HTTPResponse() {
                        @Override
                        public void OnNormolResponse(NormalResponse np) {
                            if (np.result) {
                                try {
                                    String address = "www.baidu.com";
                                    address = np.data.toString();
                                    Log.i(pingbaidu, "address=" + address);
                                    String pingComm = "ping -s 512 -i 3 " + address + "";
                                    pingComm = "ping -i 3 -s 512 -t 255 " + address;
                                    Process process = Runtime.getRuntime().exec(pingComm);
                                    InputStreamReader r = new InputStreamReader(process.getInputStream());
                                    LineNumberReader returnData = new LineNumberReader(r);
                                    String returnMsg = "";
                                    String line = "";
                                    while ((line = returnData.readLine()) != null) {
                                        Log.i(pingbaidu, line);
                                        returnMsg += line;
                                        if ("".equals(line) == false) {
                                            String[] st = line.split(" ");
                                            for (String s : st) {
                                                if ("".equals(s) == false) {
                                                    if (s.contains("=")) {
                                                        String key = s.split("=")[0];
                                                        String value = s.split("=")[1];
                                                        if ("time".equals(key)) {
                                                            GlobalInfo.PING_AVG_RTT = Float.parseFloat(value);
                                                            Log.i(pingbaidu, GlobalInfo.PING_AVG_RTT + "");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (returnMsg.indexOf("100% loss") != -1) {
                                        System.out.println("与 " + address + " 连接不畅通.");
                                    } else {
                                        System.out.println("与 " + address + " 连接畅通.");
                                    }
                                } catch (Exception e) {

                                }
                            }
                        }
                    });
                    //ping www.huawei.com -t -l 1


                } catch (Exception e) {
                    Log.i(pingbaidu, e.toString());
                }
            }
        }).start();
    }

    private void startGPSHelper() {
        gpsHelper = new GPSHelper(context, new InterfaceCls.ILocationInfo() {
            @Override
            public void onNewLocationInfo(LocationInfo locationInfo) {
                //GPSHelper上报位置信息
                if(locationInfo==null){
                    return;
                }
                LocationInfo la=GlobalInfo.getLocationInfo();
                if(la!=null){
                    double oldLon = la.Lon;
                    double oldlat = la.Lat;
                    double dis =DeviceHelper.getGis(oldLon, oldlat, locationInfo.Lon, locationInfo.Lat);
                    Log.i("GPSDistance", "dis=" + dis);
                    if (dis >= 10) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (getPhoneInfoHelper != null) {
                                    PhoneInfo pi = getPhoneInfoHelper.GetPhoneInfo();
                                    if(pi!=null){
                                        pi=makeMoreInfoToPhoneInfo(pi);
                                        UploadDataHelper.getInstance().UploadDataToServer(pi);
                                    }
                                    Log.i("GPSDistance", "dis>10 upload QOER");
                                }
                            }
                        }).start();
                    }
                }

                GlobalInfo.setLocationInfo(locationInfo);
                iQoEWorkerInfo.onNewLocationInfo(locationInfo);
            }

            @Override
            public void onError(String str) {
                //GPSHelper报错
                iQoEWorkerInfo.onError(str,"gpsHelper");
            }
        });
        gpsHelper.Start();
    }
    private  void startGetPhoneInfoHelper(){
        getPhoneInfoHelper=new GetPhoneInfoHelper(context, new InterfaceCls.IGetPhoneInfo() {
            @Override
            public void onNewPhoneInfo(PhoneInfo pi) {
                if (pi == null) return;
                pi=makeMoreInfoToPhoneInfo(pi);
                GlobalInfo.setPi(pi);

                if (!isSetedImeiAndImsi) {
                    isSetedImeiAndImsi = true;
                    GlobalInfo.setDeviceImeiAndImsi(pi.IMEI, pi.IMSI);
                    login(pi.IMSI, pi.IMEI);
                }
                iQoEWorkerInfo.onNewPhoneInfo(pi);
            }

            @Override
            public void onError(String str) {
                iQoEWorkerInfo.onError(str,"getPhoneInfoHelper");
            }
        });
        getPhoneInfoHelper.StartWork();
    }
    private  PhoneInfo makeMoreInfoToPhoneInfo(PhoneInfo pi){
        if(pi==null)return null;
        pi.AID=GlobalInfo.AID;
        LocationInfo la=GlobalInfo.getLocationInfo();
        if(la!=null){
            pi.lon = la.Lon;
            pi.lat = la.Lat;
            pi.accuracy = la.Accuracy;
            pi.altitude = la.Altitude;
            pi.gpsSpeed = la.Speed;
            pi.satelliteCount = la.SatelliteCount;
        }
        pi.businessType =businessType;
        pi.apkName = GlobalInfo.apkName;
        pi.apkVersion = GlobalInfo.apkVersion;

        pi.PING_AVG_RTT = GlobalInfo.PING_AVG_RTT;

        pi.HTTP_RESPONSE_TIME = QOER_HTTP_Response_Time;
        pi.HTTP_URL = QOER_HTTP_URL;
        pi.HTTP_BUFFERSIZE = QOER_HTTP_BufferSize;

        pi.DATETIME =GlobalInfo.getSystemTime();
        pi.isGPSOpen =DeviceHelper.isGPSOpen(context) ? 1 : 0;
        return pi;
    }
    private  void startUploadQoERDataToServer(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (getPhoneInfoHelper != null) {
                            PhoneInfo pi = getPhoneInfoHelper.GetPhoneInfo();
                            pi=makeMoreInfoToPhoneInfo(pi);
                            GlobalInfo.setPi(pi);
                            Log.i(TAG,"pi.aid="+pi.AID);
                            UploadDataHelper.getInstance().UploadDataToServer(pi);
                        }
                    } catch (Exception e) {
                        Log.i("UploadLoadDataToServer", "error-->" + e.getMessage());
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {

                    }
                }
            }
        }).start();
    }
    private void login(String imsi, String imei) {
        if (islogined) return;
        islogined = true;
        HTTPHelper.GetH(GlobalInfo.serverUrl + "?func=GetAid&imei=" + imei + "&imsi=" + imsi, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                if (np.result) {
                    GlobalInfo.AID = np.data.toString();
                    iQoEWorkerInfo.onGetAID(GlobalInfo.AID);
                }
            }
        });
    }
}
