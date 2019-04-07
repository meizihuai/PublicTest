package com.getinfo.app.uniqoe;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.style.FontStyle;
import com.getinfo.app.uniqoe.utils.EarFcnHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//首页，主要的手机状态、当前网络状态、邻区表、最近信号记录表等
public class FrmMainTest extends Fragment {
    private OnFragmentInteractionListener mListener;
    private TextView txtnetType, txtSignalType;
    private TextView txtLon, txtLat, txtAccuracy, txtSatelliteCount;
    private TextView txtTAC, txtPCI, txtEARFCN, txtCI, txteNB, txtCellID, txtRSRP, txtRSRQ, txtSINR;//注意：txtRSRQ已经改为显示网络制式
    private TextView txtAvgRSRP;
    private List<CellSigNalInfo> cellSigNalInfoList;
    private com.bin.david.form.core.SmartTable cellSigNalInfoListTable, neighbourListTable;
    private View myView;
    private boolean flagUIReady = false;

    public FrmMainTest() {

    }

    public static FrmMainTest newInstance(String param1, String param2) {
        FrmMainTest fragment = new FrmMainTest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        myView = inflater.inflate(R.layout.fragment_frm_main_test, container, false);
        neighbourListTable = (SmartTable<Neighbour>) myView.findViewById(R.id.neighbourListTable);
        cellSigNalInfoListTable = (SmartTable<CellSigNalInfo>) myView.findViewById(R.id.cellSigNalInfoListTable);
        FontStyle fontStyle = new FontStyle(40, Color.WHITE);
        fontStyle = neighbourListTable.getConfig().getContentStyle();
        fontStyle.setTextColor(Color.WHITE);
        neighbourListTable.getConfig().setContentStyle(fontStyle);
        neighbourListTable.getConfig().setYSequenceStyle(fontStyle);
        neighbourListTable.getConfig().setXSequenceStyle(fontStyle);
        neighbourListTable.getConfig().setColumnTitleStyle(fontStyle);
        neighbourListTable.getConfig().setTableTitleStyle(fontStyle);
        //   neighbourListTable.getConfig().setShowColumnTitle(false);
        neighbourListTable.getConfig().setShowTableTitle(false);
        neighbourListTable.getConfig().setShowXSequence(false);
        neighbourListTable.getConfig().setShowYSequence(false);

        //   fontStyle = new FontStyle(40, Color.WHITE);
        // fontStyle = new FontStyle(Color.WHITE);
        cellSigNalInfoListTable.getConfig().setContentStyle(fontStyle);
        cellSigNalInfoListTable.getConfig().setYSequenceStyle(fontStyle);
        cellSigNalInfoListTable.getConfig().setXSequenceStyle(fontStyle);
        cellSigNalInfoListTable.getConfig().setColumnTitleStyle(fontStyle);
        cellSigNalInfoListTable.getConfig().setTableTitleStyle(fontStyle);
        //  cellSigNalInfoListTable.getConfig().setShowColumnTitle(false);
        cellSigNalInfoListTable.getConfig().setShowTableTitle(false);
        cellSigNalInfoListTable.getConfig().setShowXSequence(false);
        cellSigNalInfoListTable.getConfig().setShowYSequence(false);
        cellSigNalInfoList = new ArrayList();
        iniTVS();
//        linearLayoutGPS = myView.findViewById(R.id.linearLayoutGPS);
//        linearLayoutGPS.setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                ClipboardManager myClipboard;
//                myClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
//                ClipData myClip;
//                String lon = txtLon.getText() + "";
//                String lat = txtLat.getText() + "";
//                String text = "经度:" + lon + ",纬度:" + lat;
//                text = lon + "," + lat;
//                myClip = ClipData.newPlainText("text", text);
//                myClipboard.setPrimaryClip(myClip);
//                Toast.makeText(getActivity(), "GPS信息已复制进粘贴板", Toast.LENGTH_LONG);
//                return false;
//            }
//        });
        return myView;
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

    private void iniTVS() {

        txtnetType = myView.findViewById(R.id.txtnetType);
        txtSignalType = myView.findViewById(R.id.txtSignalType);


        txtLon = myView.findViewById(R.id.txtLon);
        txtLat = myView.findViewById(R.id.txtLat);
        txtAccuracy = myView.findViewById(R.id.txtAccuracy);
        txtSatelliteCount = myView.findViewById(R.id.txtSatelliteCount);
        txtTAC = myView.findViewById(R.id.txtTAC);
        txtPCI = myView.findViewById(R.id.txtPCI);
        txtEARFCN = myView.findViewById(R.id.txtEARFCN);
        txtCI = myView.findViewById(R.id.txtCI);
        txteNB = myView.findViewById(R.id.txteNB);
        txtCellID = myView.findViewById(R.id.txtCellID);
        txtRSRP = myView.findViewById(R.id.txtRSRP);
        txtRSRQ = myView.findViewById(R.id.txtRSRQ);
        txtSINR = myView.findViewById(R.id.txtSINR);
        txtAvgRSRP = myView.findViewById(R.id.txtAvgRSRP);


        txtnetType.setText("数据类型:");
        txtSignalType.setText("信号类型:");

        txtLon.setText("经度:");
        txtLat.setText("纬度:");
        txtAccuracy.setText("精度:");
        txtSatelliteCount.setText("卫星:");

        txtTAC.setText("TAC:");
        txtPCI.setText("");
        txtEARFCN.setText("");
        txtCI.setText("CI:");
        txteNB.setText("eNBId:");
        txtCellID.setText("CellId:");
        txtRSRP.setText("RSRP:");
        txtRSRQ.setText("FreqLable:");
        txtSINR.setText("SINR:");
        flagUIReady = true;
    }

    //用于接收主进程发过来的位置信息
    public void setLocations(double lon, double lat, double myPhoneAccuracy, double myPhoneAltitude, double myPhoneSpeed, int myPhoneSatelliteCount) {
        DecimalFormat decimalFormat = new DecimalFormat("0.000000");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String lonstr = decimalFormat.format(lon);//format 返回的是字符串
        String latstr = decimalFormat.format(lat);//format 返回的是字符串
        DecimalFormat accuracyFormat = new DecimalFormat("0.00");
        String accuracy = accuracyFormat.format(myPhoneAccuracy);
        if (!flagUIReady) return;
        txtSatelliteCount.setText("卫星:" + myPhoneSatelliteCount);
        txtLon.setText("经度:" + lonstr);
        txtLat.setText("纬度:" + latstr);
        txtAccuracy.setText("精度:" + accuracy);
    }

    //用于接收主进程发过来的PhoneInfo
    public void OnRecivePhoneInfo(PhoneInfo pi) {
        try {
            if (pi == null) return;
            if (!flagUIReady) return;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// HH:mm:ss
            Date date = new Date(System.currentTimeMillis());
            String time = simpleDateFormat.format(date);
            txtAvgRSRP.setText("邻区采集时间  " + time);
            txtnetType.setText("数据类型:" + pi.netType);
            txtSignalType.setText("信号类型:" + pi.sigNalType);
            txtTAC.setText("TAC:" + pi.TAC);
            txtPCI.setText("PCI:" + pi.PCI);
            txtEARFCN.setText("EARFCN:" + pi.EARFCN);
            txtCI.setText("eCI:" + pi.CI);
            txteNB.setText("eNBId:" + pi.eNodeBId);
            txtCellID.setText("CellId:" + pi.cellId);
            txtRSRP.setText("RSRP:" + (int) pi.RSRP + " dBm");
            //  txtRSRQ.setText("RSRQ:" + (int) pi.RSRQ + " dB");
            txtRSRQ.setText(EarFcnHelper.GetFreqLable(pi.EARFCN));
            txtSINR.setText("SINR:" + (int) pi.SINR + " dB");
            if (cellSigNalInfoList.size() == 10) {
                cellSigNalInfoList.remove(0);
            }
            cellSigNalInfoList.add(new CellSigNalInfo(pi.TAC, pi.eNodeBId, pi.cellId, pi.RSRP, pi.SINR));
            try {
                cellSigNalInfoListTable.setData(cellSigNalInfoList);
            } catch (Exception e) {

            }


            List<Neighbour> nb = pi.neighbourList;
            if (nb != null) {
                try {
                    neighbourListTable.setData(nb);
                } catch (Exception e) {

                }
            }

        } catch (Exception e) {
            Log.i("neighbourHasaki", e.toString());
        }

    }


}
