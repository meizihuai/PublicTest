package com.getinfo.app.uniqoe;

import android.util.Log;
//线程类，用于线程守护
public class ThreadDaemon {
    private Thread workThread;
    private Thread myThread;
    private WorkFlag workFlag;
    private int watchIntervalSecond;
    private Runnable runnable;
    public ThreadDaemon(int watchIntervalSecond){
        this.watchIntervalSecond = watchIntervalSecond;
    }
    public void setWorkFlag(WorkFlag workFlag){
        this.workFlag = workFlag;
    }
    public void setWorkThread(Thread workThread){
        this.workThread = workThread;
    }
    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }
//    public ThreadDaemon(Thread workThread, WorkFlag workFlag, int watchIntervalSecond) {
//        this.workThread = workThread;
//        this.workFlag = workFlag;
//        this.watchIntervalSecond = watchIntervalSecond;
//    }
    public void startWatching() {
        if (myThread != null) {
            try {
                myThread.interrupt();
            } catch (Exception e) {

            }
        }
        if (workFlag == null) return;
        if(workThread==null)return;
        if(runnable==null)return;
        final int sleepCount = 1000 * watchIntervalSecond;
        myThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    workFlag.setFlagNeedClosed(true);
                    Log.i("ThreadDaemon","工作线程 flag 设置为 true");
                    try {
                        Thread.sleep(sleepCount);
                    } catch (Exception e) {

                    }
                    Log.i("ThreadDaemon",workFlag.getFlagNeedClosed()+"");
                    try {
                        if(workFlag.getFlagNeedClosed()){
                            Log.i("ThreadDaemon","工作线程 flag 为true,即将重启工作线程！");
                            try{
                                workThread.interrupt();
                            }catch (Exception e){

                            }
                            workThread=new Thread(runnable);
                            workThread.start();
                        }
                    } catch (Exception e) {

                    }
                }
            }
        });
        myThread.start();
    }

    public void stopWatching() {

    }



    public class WorkFlag {
        private boolean flagNeedClosed = true;
        private Object lock;
        public WorkFlag() {
            lock=new Object();
            flagNeedClosed = true;
        }

        public void setFlagNeedClosed(boolean flagNeedClosed) {
            synchronized (lock){
                this.flagNeedClosed = flagNeedClosed;
            }
        }

        public boolean getFlagNeedClosed() {
            synchronized (lock){
                return flagNeedClosed;
            }
        }
    }
}
