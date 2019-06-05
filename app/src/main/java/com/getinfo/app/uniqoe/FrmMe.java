package com.getinfo.app.uniqoe;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import com.getinfo.app.uniqoe.EnitityCls.QoEMissionInfo;
import com.getinfo.app.uniqoe.utils.MultipleClick;
import com.getinfo.sdk.qoemaster.APKVersionCodeUtils;
import com.getinfo.sdk.qoemaster.GlobalInfo;
import com.getinfo.sdk.qoemaster.HTTPHelper;
import com.getinfo.sdk.qoemaster.NormalResponse;
import com.getinfo.sdk.qoemaster.PhoneInfo;
import com.getinfo.sdk.qoemaster.QoEVideoSource;
import com.getinfo.sdk.qoemaster.Setting;
import com.getinfo.sdk.qoemaster.UploadDataHelper;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//"我的"页面 包含手机基本信息、个人设置、app更新等
public class FrmMe extends Fragment {
    private String aid; //终端账户,绑定imsi
    private boolean isFirstSelectedServerUrl = true;
    private TextView txtPhoneModel, txtphoneOS, txtIMSI, txtIMEI, txtBonusPoints, txtAID,txtQoEMission;
    private LinearLayout divPhoneOS;
    private int divPhoneOSClickTime = 0;
    private long divPhoneOSClickms = System.currentTimeMillis();
    private View myView;
    private String apkUrl;
    private boolean isNeedInit=false;
    private boolean isInited=false;


    private String token = "928453310";
    private String dirName = "PublicTest";
    private String appName = "PublicTest.apk";
    private String updateUrl = "http://221.238.40.153:7062/default.ashx"; //app更新功能服务器

    private String imei = "";
    private Button btnMission, btnAbout;

    // private Button btnCheckUpdate, btnGetUpdate;
    private TextView txtLocalVersion;
    private Switch switchQoEScore, switchQoEScreenRecord;
    private OnFragmentInteractionListener mListener;

    public FrmMe() {

    }

    public static FrmMe newInstance(String param1, String param2) {
        FrmMe fragment = new FrmMe();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public View getMyView() {
        return myView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_me, container, false);
        iniSpinner();
        txtLocalVersion = myView.findViewById(R.id.txtLocalVersion);
        txtPhoneModel = myView.findViewById(R.id.txtPhoneModel);
        txtphoneOS = myView.findViewById(R.id.txtphoneOS);
        txtIMSI = myView.findViewById(R.id.txtIMSI);
        txtIMEI = myView.findViewById(R.id.txtIMEI);
        txtQoEMission=myView.findViewById(R.id.txtQoEMission);
        txtBonusPoints = myView.findViewById(R.id.txtBonusPoints);
        txtBonusPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetMyBonusPoints();
            }
        });
        switchQoEScore = myView.findViewById(R.id.switchQoESocre);
        switchQoEScreenRecord = myView.findViewById(R.id.switchQoEScreenRecord);
//        Setting tmpSetting = GlobalInfo.getSetting(getContext());
//        if (tmpSetting != null) {
//            Log.i("setSetting", "tmpSetting.switchQoEScore=" + tmpSetting.switchQoEScore);
//            switchQoEScore.setChecked(tmpSetting.switchQoEScore);
//            switchQoEScreenRecord.setChecked(tmpSetting.switchQoEScreenRecord);
//        } else {
//            Log.i("setSetting", "tmpSetting is null");
//        }

        switchQoEScore.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Setting setting = GlobalInfo.getSetting(getContext());
                setting.switchQoEScore = isChecked;
                Log.i("setSetting", "switchQoEScore.isChecked=" + isChecked);
                GlobalInfo.setSetting(getContext(), setting);
            }
        });
        switchQoEScreenRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Setting setting = GlobalInfo.getSetting(getContext());
                setting.switchQoEScreenRecord = isChecked;
                Log.i("setSetting", "switchQoEScreenRecord.isChecked=" + isChecked);
                GlobalInfo.setSetting(getContext(), setting);
            }
        });

        txtPhoneModel.setText("读取中...");
        txtphoneOS.setText("读取中...");
        txtIMSI.setText("读取中...");
        txtIMEI.setText("读取中...");
        txtQoEMission.setText("正在获取...");
        String versionName = APKVersionCodeUtils.getVerName(getContext());

        txtLocalVersion.setText("本地版本:" + versionName);
//        btnCheckUpdate = myView.findViewById(R.id.btnCheckUpdate);
//        btnCheckUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CheckCanUpdate();
//            }
//        });
//        btnGetUpdate = myView.findViewById(R.id.btnGetUpdate);
//        btnGetUpdate.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String uri = apkUrl + "?t=" + System.currentTimeMillis();
//                Toast.makeText(getActivity(), "正在下载", Toast.LENGTH_LONG).show();
//                Context context = getActivity();
//                DownloadUtils du = new DownloadUtils(context);
//                du.download(uri, appName, dirName);
//            }
//        });
        btnAbout = myView.findViewById(R.id.btnAbout);
        //  btnAbout.setText("关于 UniQoE");totast
        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            }
        });

        btnMission = myView.findViewById(R.id.btnMission);
        btnMission.setVisibility(View.INVISIBLE);
        btnMission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MissionActivity.class);
                startActivity(intent);
            }
        });
        divPhoneOS = myView.findViewById(R.id.divPhoneOS);
        MultipleClick multipleClick = new MultipleClick();
        multipleClick.multipleClick(3, 1000, divPhoneOS, new MultipleClick.MultipleClickBack() {
            @Override
            public void doBack() {
                String str = "IMEI:" + GlobalInfo.myDeviceImei + " IMSI:" + GlobalInfo.myDeviceImsi;
                Toast.makeText(getContext(), str, Toast.LENGTH_LONG).show();
            }
        });
//        divPhoneOS.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                long cms=System.currentTimeMillis();
//                if(cms-divPhoneOSClickms>1000){
//                    divPhoneOSClickTime=0;
//                }
//                divPhoneOSClickms=System.currentTimeMillis();
//                divPhoneOSClickTime++;
//                if(divPhoneOSClickTime==4){
//                    divPhoneOSClickTime=0;
//                    String str="IMEI:"+GlobalInfo.myDeviceImei+" IMSI:"+GlobalInfo.myDeviceImsi;
//                    Toast.makeText(getContext(),str, Toast.LENGTH_LONG).show();
//                }
//            }
//        });
        if(isNeedInit && !isInited){
            init();
        }
        return myView;
    }
    public  void init(){
        Log.i("FrmMeInit","init");
        if(isInited)return;
        isNeedInit=true;
        if(myView==null){
            isInited=false;
            return;
        }
        isInited=true;
        CheckDevicePermission();
        CheckCanUpdate();
        Switch switchQoEScore =myView.findViewById(R.id.switchQoESocre);
        Switch switchQoEScreenRecord = myView.findViewById(R.id.switchQoEScreenRecord);
        Spinner spinner_serverIp = myView.findViewById(R.id.spinner_serverUrl);
        final Spinner spinner_videoType = myView.findViewById(R.id.spinner_videoType);
        final Setting tmpSetting = GlobalInfo.getSetting(getContext());
        if (tmpSetting != null) {
            switchQoEScore.setChecked(tmpSetting.switchQoEScore);
            switchQoEScreenRecord.setChecked(tmpSetting.switchQoEScreenRecord);
            String serverUrl = tmpSetting.serverUrl;
            QoEVideoSource.wantType = tmpSetting.videoWantType;
            for (int i = 0; i < spinner_serverIp.getAdapter().getCount(); i++) {
                if (serverUrl.equals(spinner_serverIp.getAdapter().getItem(i).toString())) {
                    spinner_serverIp.setSelection(i);
                    break;
                }
            }
        }
        HTTPHelper.GetH((GlobalInfo.serverUrl + "?func=GetQoEVideoSourceTypeList"), new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                if (np.result && np.data != null) {
                    try {
                        List<String> list = new ArrayList<>();
                        list = new Gson().fromJson(np.data.toString(), list.getClass());
                        final List<String> tmp = list;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                iniSpinnerVideoType(tmp);
                                if (tmpSetting != null) {
                                    for (int i = 0; i < spinner_videoType.getAdapter().getCount(); i++) {
                                        if (tmpSetting.videoWantType.equals(spinner_videoType.getAdapter().getItem(i).toString())) {
                                            spinner_videoType.setSelection(i);
                                            break;
                                        }
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        Log.i("startActivity","GetQoEVideoSourceTypeList failed "+e.getMessage());
                    }
                }
            }
        });

    }

    private void iniSpinner() {
        //  iniSpinnerVideoType();
        final List<String> data_list;
        ArrayAdapter<String> arr_adapter;
        data_list = new ArrayList<String>();
        data_list.add("111服务器");
        data_list.add("221服务器");
        final List<String> serverList;
        serverList = new ArrayList<String>();
        serverList.add("http://111.53.74.132:7062/default.ashx");
        serverList.add("http://221.238.40.153:7062/default.ashx");
        Spinner spinner = myView.findViewById(R.id.spinner_serverUrl);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectItm = data_list.get(position);
                if (isFirstSelectedServerUrl) {
                    isFirstSelectedServerUrl = false;
                    return;
                }
                String serverUrl = serverList.get(position);
                Setting setting = GlobalInfo.getSetting(getContext());
                setting.serverUrl = serverUrl;
                UploadDataHelper.getInstance().setServerURL(serverUrl);
                GlobalInfo.setSetting(getContext(), setting);
                // Toast.makeText(getContext(),selectItm+"设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "您没有选择任何项目！", Toast.LENGTH_SHORT).show();
            }
        });


        //适配器
        arr_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);

        Setting tmpSetting = GlobalInfo.getSetting(getContext());
        if (tmpSetting != null) {
            String serverUrl = tmpSetting.serverUrl;
            int selectIndex = 0;
            for (int i = 0; i < serverList.size(); i++) {
                if (serverUrl.equals(serverList.get(i))) {
                    selectIndex = i;
                    break;
                }
            }
            spinner.setSelection(selectIndex);
        }

    }

    public void iniSpinnerVideoType(final List<String> data_list) {

        ArrayAdapter<String> arr_adapter;
//        final List<String> data_list;
//        data_list = new ArrayList<String>();
//        data_list.add("全部");
//        data_list.add("小视频");
//        data_list.add("综艺");
//        data_list.add("小品");
//        data_list.add("相声");
//        data_list.add("电影");
//        data_list.add("音乐");

        Spinner spinner = myView.findViewById(R.id.spinner_videoType);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectItm = data_list.get(position);
                Setting setting = GlobalInfo.getSetting(getContext());
                setting.videoWantType = selectItm;
                QoEVideoSource.wantType = selectItm;
                GlobalInfo.setSetting(getContext(), setting);
                //  Toast.makeText(getContext(),selectItm+"设置成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(getContext(), "您没有选择任何项目！", Toast.LENGTH_SHORT).show();
            }
        });


        //适配器
        arr_adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);

        Setting tmpSetting = GlobalInfo.getSetting(getContext());
        if (tmpSetting != null) {
            String serverUrl = tmpSetting.serverUrl;
            int selectIndex = 0;
            for (int i = 0; i < data_list.size(); i++) {
                if (serverUrl.equals(data_list.get(i))) {
                    selectIndex = i;
                    break;
                }
            }
            spinner.setSelection(selectIndex);
        }
    }

    //获取本设备的执行权限，目前权限为9的设备可以打开任务管理模块
    public void CheckDevicePermission() {
        Setting tmpSetting = GlobalInfo.getSetting(getContext());
        String urlTmp = GlobalInfo.serverUrl;
        Log.i("CheckDevicePermission","url="+urlTmp);
        if (tmpSetting != null) {
            urlTmp = GlobalInfo.getSetting(getContext()).serverUrl;
        }
        final String serverUrl = urlTmp;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int count = 0;
                while ("".equals(imei) && count < 10) {
                    count++;
                    PhoneInfo pi = GlobalInfo.getPi();
                    if (pi != null) {
                        imei = pi.IMEI;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {

                    }
                }
                if ("".equals(imei)) return;
                OkHttpClient okHttpClient = new OkHttpClient();
                MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
                Request request = new Request.Builder()
                        .url(serverUrl + "?func=GetDevicePermission&imei=" + imei)
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.i("GetDevicePermission", e.getMessage());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            final String result = response.body().string();
                            Log.i("GetDevicePermission", result);

                            try {
                                JSONObject obj = new JSONObject(result);
                                boolean flag = obj.getBoolean("result");
                                String data = obj.getString("data");
                                String permission = "0";
                                permission = data;
                                if ("9".equals(permission)) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                btnMission.setVisibility(View.VISIBLE);
                                            } catch (Exception e) {

                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {

                            }
                        }
                    }
                });
            }
        }).start();
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    ///判断是否可以更新
    public void CheckCanUpdate() {
        Log.i("hasaki", "begin runing...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String versionName = APKVersionCodeUtils.getVerName(getContext());
                    String requestBody = "";
                    JSONObject pd = new JSONObject();
                    pd.put("appName", appName);
                    pd.put("version", versionName);
                    JSONObject ps = new JSONObject();
                    ps.put("func", "GetCanUpdate");
                    ps.put("data", pd);
                    requestBody = ps.toString();
                    Log.i("GetCanUpdate", requestBody);
                    String u = updateUrl;
                    OkHttpClient okHttpClient = new OkHttpClient();
                    MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8;");
                    Request request = new Request.Builder()
                            .url(u)
                            .post(RequestBody.create(mediaType, requestBody.getBytes("UTF-8")))
                            .build();
                    Call call = okHttpClient.newCall(request);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.i("GetCanUpdate", e.getMessage());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                final String result = response.body().string();
                                Log.i("GetCanUpdate", result);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            JSONObject jb = new JSONObject(result);
                                            boolean handleResult = jb.getBoolean("result");
                                            String msg = jb.getString("msg");
                                            JSONObject rb = jb.getJSONObject("data");
                                            boolean canUpdate = rb.getBoolean("canUpdate");
                                            String serverVersion = rb.getString("serverVersion");
                                            apkUrl = rb.getString("url");
                                            TextView txtServerVersion = (TextView) myView.findViewById(R.id.txtServerVersion);
                                            if (canUpdate) {
                                                txtServerVersion.setText("有更新:" + serverVersion + "  请到'关于'升级");
                                                txtServerVersion.setTextColor(getResources().getColor(R.color.red));
//                                                btnGetUpdate.setBackgroundColor(getResources().getColor(R.color.titleColor));
//                                                btnGetUpdate.setEnabled(true);
                                            } else {
                                                txtServerVersion.setText("最新版本:" + serverVersion + "  暂无更新");
                                                txtServerVersion.setTextColor(getResources().getColor(R.color.gray));
//                                                btnGetUpdate.setBackgroundColor(getResources().getColor(R.color.lightgrey));
//                                                btnGetUpdate.setEnabled(false);
                                            }
                                        } catch (Exception e) {

                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (Exception e) {
                    Log.i("hasaki", e.getMessage());
                }
            }
        }).start();
    }

    //主进程发过来PhoneInfo
    public void OnRecivePhoneInfo(PhoneInfo pi) {
        try {
            txtPhoneModel.setText(pi.phoneModel);
            txtphoneOS.setText(pi.phoneOS);
            txtIMSI.setText(pi.IMSI);
            txtIMEI.setText(pi.IMEI);
        } catch (Exception e) {

        }
    }

    public void SetMyAID(String aid) {
        this.aid=aid;
        txtAID = myView.findViewById(R.id.txtAID);
        txtAID.setText(aid);
        getMyQoEMission();
    }
    private void getMyQoEMission(){

        HTTPHelper.GetH(GlobalInfo.uplanServerUrl + "/api/uniqoe/GetQoEMission?aid=" + aid, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                Module.setHaveQoEMission(false,null);
                Gson gson = new Gson();
                String result = gson.toJson(np);
                if(np.result){
                    try {
                        QoEMissionInfo info=gson.fromJson(gson.toJson(np.data),QoEMissionInfo.class);
                        if(info!=null){
                            txtQoEMission.setText(info.TYPE);
                            String json=gson.toJson(info);
                            Log.i("getMyQoEMission", json);
                            Module.setHaveQoEMission(true,info);
                        }else{
                            txtQoEMission.setText("未知任务");

                        }
                    }catch (Exception e){
                        Log.i("getMyQoEMission", e.toString());
                    }
                }else{
                    txtQoEMission.setText("没有任务");
                }
                try{
                    Thread.sleep(10*1000);
                    getMyQoEMission();
                }catch (Exception e){

                }
            }
        });
    }

    public void GetMyBonusPoints() {
        String url = GlobalInfo.serverUrl + "?func=GetMyBonusPoints&imsi=" + GlobalInfo.myDeviceImsi;
        HTTPHelper.GetH(url, new HTTPHelper.HTTPResponse() {
            @Override
            public void OnNormolResponse(NormalResponse np) {
                Gson gson = new Gson();
                String result = gson.toJson(np);
                Log.i("GetMyBonusPoints", result);
                if (np.result) {
                    String str = np.data.toString() + " 分 [点击刷新]";
                    txtBonusPoints.setText(str);
                }
            }
        });
    }
}
