package com.getinfo.app.uniqoe.utils;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.getinfo.app.uniqoe.MainActivity;

import java.lang.ref.WeakReference;

public class AudioRecordHelper {
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    static AudioRecord mAudioRecord;
    static Object mLock;
    public static int getNoiseValue(){
        if(mLock==null){
            mLock=new Object();
        }
        if (mAudioRecord == null) {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        }
        if (mAudioRecord == null) {
            return  0;
        }
        mAudioRecord.startRecording();
        synchronized (mLock) {
            try {
                mLock.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        short[] buffer = new short[BUFFER_SIZE];
        int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        long v = 0;
        for (int i = 0; i < buffer.length; i++) {
            v += buffer[i] * buffer[i];
        }
        double mean = v / (double) r;
        final double volume = 10 * Math.log10(mean);
        int db=(int)volume;
        return db;
    }
}
