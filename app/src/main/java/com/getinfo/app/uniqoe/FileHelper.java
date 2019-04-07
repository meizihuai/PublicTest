package com.getinfo.app.uniqoe;

import android.content.Context;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//系统文件读写封装
public class FileHelper {
    private static String rootPath = "/sdcard/ApublicTest/";
    //写入字符串到文件
    public static void fileWriteAllText(Context context, String filename, String content) throws IOException {

        //获取外部存储卡的可用状态
        String storageState = Environment.getExternalStorageState();

        //判断是否存在可用的的SD Card
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File file=new File(rootPath);
            if( !file.exists()){
                file.mkdir();
            }else{
                if(file.isFile()){
                    file.delete();
                    file.mkdir();
                }
            }
            //路径： /storage/emulated/0/Android/data/com.yoryky.demo/cache/yoryky.txt
          //  filename = context.getExternalCacheDir().getAbsolutePath()  + File.separator + filename;
            filename=rootPath+filename;
            boolean isExist=fileIsExists(filename,true);
           // Log.i("mkdir","文件存在="+isExist);


            FileOutputStream outputStream = new FileOutputStream(filename,true);
            outputStream.write(content.getBytes());
            outputStream.close();
        }
    }
    //读取文件所有字符串
    public static String fileReadAllText(Context context, String filename) throws IOException {
        StringBuilder sb = new StringBuilder("");
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
           // filename = context.getExternalCacheDir().getAbsolutePath() + File.separator + filename;
            filename=rootPath+filename;
            boolean isExist=fileIsExists(filename,false);
            //打开文件输入流
            FileInputStream inputStream = new FileInputStream(filename);

            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            //读取文件内容
            while(len > 0){
                sb.append(new String(buffer,0,len));
                //继续将数据放到buffer中
                len = inputStream.read(buffer);
            }
            //关闭输入流
            inputStream.close();
        }
        return sb.toString();
    }
    //
    public static byte[] fileReadAllByte(Context context, String filename) throws IOException {
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            FileInputStream inStream = new FileInputStream(filename);//读文件
            byte[] buffer = new byte[1024];//缓存
            int len = 0;
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            while( (len = inStream.read(buffer))!= -1){//直到读到文件结束
                outStream.write(buffer, 0, len);
            }
            byte[] data = outStream.toByteArray();//得到文件的二进制数据
            outStream.close();
            inStream.close();
            return data;
        }
        return null;
    }
    //判断文件是否存在
    public static boolean fileIsExists(String strFile,boolean isDelete)
    {
        try
        {
            File f=new File(strFile);
            if(!f.exists())
            {
                return false;
            }else{
                if(isDelete){
                    f.delete();
                }
            }
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }
    public static List<String> GetFiles(){
        List<String> files=new ArrayList<>();
        File file = new File(rootPath);
        File[] subFile = file.listFiles();
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                files.add(filename);
            }
        }
        return files;
    }
    public static List<String> GetFiles(String path){
        List<String> files=new ArrayList<>();
        File file = new File(path);
        File[] subFile = file.listFiles();
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                files.add(filename);
            }
        }
        return files;
    }
}
