package com.getinfo.sdk.qoemaster.Interfaces;


import android.provider.ContactsContract;

import com.getinfo.sdk.qoemaster.LocationInfo;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.getinfo.sdk.qoemaster.QoEVideoSource;

public class InterfaceCls {
    public  interface IQoEVideoSource{
        void onNewQoEVideoSourceInfo(QoEVideoSource.QoEVideoSourceInfo qoEVideoSourceInfo);
    }
    public  interface  ILocationInfo{
        void onNewLocationInfo(LocationInfo locationInfo);
        void onError(String str);
    }
    public  interface  IGetPhoneInfo{
        void  onNewPhoneInfo(PhoneInfo phoneInfo);

        void onError(String str);
    }

    public interface IQoEWorkerInfo{
        void onError(String str,String module);
        void onGetAID(String aid);
        void onNewLocationInfo(LocationInfo locationInfo);
        void onNewPhoneInfo(PhoneInfo phoneInfo);
    }
}
