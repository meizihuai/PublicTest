package com.getinfo.sdk.qoemaster;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//信号差的地方，数据无法及时上报，便会先存入本地数据库SQLite里，等网络状态转好，则再次上传
public class LocalTestInfoHelper {
    private Context context;
    private static String rootPath = "/sdcard/ApublicTest/";
    private static String dbPath = rootPath + "/LocalTestInfoHelper.db"; //数据文件存放位置
    private static LocalTestInfoHelper localTestInfoHelper = null;
    private Object lock = new Object();

    //单例模式，获取单例
    public synchronized static LocalTestInfoHelper getInstance() {
        if (localTestInfoHelper == null) {
            makeRootDirectory(rootPath);  //判断文件夹是否存在，如不存在则新建
            localTestInfoHelper = new LocalTestInfoHelper();
        }
        return localTestInfoHelper;

    }
    //判断文件夹是否存在，如不存在则新建
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }
    //离线数据格式
    public class LocalTestInfo {
        public int id;
        public String time;
        public String json;
        public String type;
        public int ISUPLOADDATATIMELY;

        public LocalTestInfo() {
            ISUPLOADDATATIMELY = 0;
        }

        public LocalTestInfo(int id, String time, String json, String type) {
            this.id = id;
            this.time = time;
            this.json = json;
            this.type = type;
            ISUPLOADDATATIMELY = 0;
            if(context!=null){
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = pm.isInteractive();//如果为true，则表示屏幕“亮”了，否则屏幕“暗”了。
                if (!isScreenOn) {
                    ISUPLOADDATATIMELY = 2;
                }
            }
        }
    }
    //检查表是否存在
    public void checkTable() {
        boolean needReCheck = false;
        synchronized (lock) {
            try {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                String sql = "create table if not exists LocalTestTable (id integer primary key autoincrement,time varchar(50),json text,type varchar(50),ISUPLOADDATATIMELY integer)";
                db.execSQL(sql);
                db.close();
                db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                boolean result = checkColumnExists2(db, "LocalTestTable", "ISUPLOADDATATIMELY");
                if (!result) {
                    sql = "drop table LocalTestTable";
                    db.execSQL(sql);
                    db.close();
                    needReCheck = true;
                }
            } catch (Exception e) {

            }
        }
        Log.i("LocalTestInfo", "needReCheck=" + needReCheck);
        if (needReCheck) {
            checkTable();
        }
    }
    //新增离线数据，由使用者调用
    public void addLocalTestInfo(String json, String type) {
        checkTable();
        synchronized (lock) {
            try {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                ContentValues cValue = new ContentValues();
                cValue.put("time", GetSystemTime());
                cValue.put("json", json);
                cValue.put("type", type);
                db.insert("LocalTestTable", null, cValue);
                db.close();
                Log.i("LocalTestInfo", "new insert");
            } catch (Exception e) {

            }
        }
    }
    //通过主键id来删除某条离线数据，通常上传成功之后将本地离线数据删除
    public void deleteById(int[] ids) {
        checkTable();
        synchronized (lock) {
            try {
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                for (int id : ids) {
                    String sql = "delete from LocalTestTable where id=" + id;
                    db.execSQL(sql);
                }
                db.close();
            } catch (Exception e) {

            }
        }
    }
    //检查表是否存在某字段
    private boolean checkColumnExists2(SQLiteDatabase db, String tableName
            , String columnName) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            Log.i("getLocalTestInfo", "checkColumnExists2..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return result;
    }
    //查询本地数据库有哪些离线数据
    public LocalTestInfo[] getLocalTestInfo() {
        checkTable();
        synchronized (lock) {
            try {
                Log.i("getLocalTestInfo", "查询中...");
                List<LocalTestInfo> localTestInfos = new ArrayList<LocalTestInfo>();
                SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
                Cursor cursor = db.query("LocalTestTable", null, null, null, null, null, null);
                int maxLength=100;
                int readIndex=0;
                while (cursor.moveToNext() && readIndex<maxLength) {
                    readIndex++;
                    int id = cursor.getInt(0);
                    String time = cursor.getString(1);
                    String json = cursor.getString(2);
                    String type = cursor.getString(3);
                    localTestInfos.add(new LocalTestInfo(id, time, json, type));
                }
                cursor.close();
                db.close();
                int size = localTestInfos.size();
                Log.i("getLocalTestInfo", "size=" + size);
                return localTestInfos.toArray(new LocalTestInfo[size]);
            } catch (Exception e) {
                Log.i("getLocalTestInfo", e.getMessage());
            }
        }
        return null;
    }

    //获取系统时间
    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }
}
