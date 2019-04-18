package com.getinfo.sdk.qoemaster;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

//获取手机XYZ轴的加速度
public class GetXYZaSpeed implements SensorEventListener  {
    private  SensorManager sensormanager;
    private  Context context;
    private Changer changer;

    public Changer getChanger() {
        return changer;
    }

    public void setChanger(Changer changer) {
        this.changer = changer;
    }

    public interface Changer{
        void onChange(float x, float y, float z);
    }
    public  GetXYZaSpeed(Context context){
        this.context= context;
        sensormanager=(SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
    }

    public void start() {
        sensormanager.registerListener(this, sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI);
    }
    public void stop() {
        sensormanager.unregisterListener(this);
    }
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        float[] values=event.values;
        StringBuilder sb=new StringBuilder();
        StringBuilder s2=new StringBuilder();
        StringBuilder s3=new StringBuilder();
        if(values.length>=3){
            float x=values[0];
            float y=values[1];
            float z=values[2];
            if(changer!=null){
                changer.onChange(x,y,z);
            }
        }

    }
}
