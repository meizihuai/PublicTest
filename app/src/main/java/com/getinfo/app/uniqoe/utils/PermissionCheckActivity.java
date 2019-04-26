package com.getinfo.app.uniqoe.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionCheckActivity extends AppCompatActivity {
    public  interface IPermissionResult{
        void onSuccess();
        void onFail(List<String> failPermissions);
    }
    private final int REQUEST_CODE_PERMISSION = 0; //权限获取结果
    private List<String> needPermission;  //需要申请权限列表
    private  IPermissionResult iPermissionResult;

    public  void askMultiplePermission(String[] permissionArray,IPermissionResult iPermissionResult){
        needPermission = new ArrayList<>();
        this.iPermissionResult=iPermissionResult ;
        for (String permissionName :
                permissionArray) {
            if (!checkIsAskPermission(this, permissionName)) {
                needPermission.add(permissionName);
            }
        }

        if (needPermission.size() > 0) {
            //开始弹窗申请权限
            ActivityCompat.requestPermissions(this, needPermission.toArray(new String[needPermission.size()]), REQUEST_CODE_PERMISSION);
        } else {
            //已有权限，直接返回success
            iPermissionResult.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION:
                Map<String, Integer> permissionMap = new HashMap<>();
                for (String name :
                        needPermission) {
                    permissionMap.put(name, PackageManager.PERMISSION_GRANTED);
                }
                for (int i = 0; i < permissions.length; i++) {
                    permissionMap.put(permissions[i], grantResults[i]);
                }
                List<String>failList=new ArrayList<>();
                for (int i = 0; i < permissions.length; i++) {
                    if (permissionMap.get(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                        //部分被拒绝
                        failList.add(permissions[i]);
                    }
                }
                if(failList.size()==0){
                    iPermissionResult.onSuccess();
                }else{
                    iPermissionResult.onFail(failList);
                }

//                if (checkIsAskPermissionState(permissionMap, permissions)) {
//                    //获取数据
//                    iPermissionResult.onSuccess();
//                } else {
//                    iPermissionResult.onSuccess();
//                    //提示权限获取不完成，可能有的功能不能使用
//                    //Toast.makeText(this, "部分权限未同意，程序功能将会受到影响", Toast.LENGTH_LONG).show();
//                }
                break;
        }
    }

    public boolean checkIsAskPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }

    }

    public boolean checkIsAskPermissionState(Map<String, Integer> maps, String[] list) {
        for (int i = 0; i < list.length; i++) {
            if (maps.get(list[i]) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;

    }
}

