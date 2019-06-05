package com.getinfo.app.uniqoe;
import com.getinfo.app.uniqoe.EnitityCls.QoEMissionBody;
import com.getinfo.app.uniqoe.EnitityCls.QoEMissionInfo;
import com.getinfo.app.uniqoe.Interfaces.IQoEMissionListener;
import com.getinfo.app.uniqoe.utils.ScreenRecorder;
import com.getinfo.sdk.qoemaster.*;

public class Module {
     private static ScreenRecorder screenRecorder;
     public  static boolean flag_HaveQoEMission=false;
     public  static QoEMissionInfo qoeMissionInfo;
     public  static IQoEMissionListener qoEMissionListener;

    public static ScreenRecorder getScreenRecorder() {
        return screenRecorder;
    }

    public static void setScreenRecorder(ScreenRecorder screenRecorder) {
        Module.screenRecorder = screenRecorder;
    }
    public  static void   setHaveQoEMission(boolean flag,QoEMissionInfo info){
        flag_HaveQoEMission=false;
        qoeMissionInfo=info;
        if(qoEMissionListener!=null){
            qoEMissionListener.onQoEMissionChange(flag,info);
        }
    }
}
