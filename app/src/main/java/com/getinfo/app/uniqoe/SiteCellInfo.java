package com.getinfo.app.uniqoe;

import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellInfo;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.List;
//基站小区的基本信息
public class SiteCellInfo {
    public int ECI;
    public int TAC;
    public int RSRP;
    public int SINR;
    public int RSRQ;
    public int PCI;
    public int MNC;
    public int FREQ;
    public String cellIdentity;  //小区网络类型 LTE WCDMA CDMA GSM之类
    public int eNodeBId;
    public int cellId;
    public String ADJ_SIGNAL;
    public int Adj_ECI1;
    public int Adj_RSRP1;
    public int Adj_SINR1;

    public SiteCellInfo() {

    }

    public SiteCellInfo(String getType) {
        try {

        } catch (Exception e) {

        }
    }

    public SiteCellInfo(List<CellInfo> allCellinfo, String getType) {
        if (allCellinfo == null) return;
        Gson gsonTmp = new Gson();
        String tmp = gsonTmp.toJson(allCellinfo);
        SetAdjs(allCellinfo, getType);
        this.ADJ_SIGNAL = tmp;
        for (int i = 0; i < allCellinfo.size(); i++) {
            CellInfo cellInfo = allCellinfo.get(i);
            if (getType == "GSM" && cellInfo instanceof CellInfoGsm) {
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoGsm);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.ECI = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mCid");
                    this.TAC = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mLac");
                    // this.PCI = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mBsic");
                    this.FREQ = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mArfcn");

//                    this.RSRP = jsonObject.getJSONObject("mCellSignalStrengthGsm").getInt("mRsrp");
//                    this.RSRQ = jsonObject.getJSONObject("mCellSignalStrengthGsm").getInt("mRsrq");
                    this.SINR = jsonObject.getJSONObject("mCellSignalStrengthGsm").getInt("mSignalStrength");

                    this.cellIdentity = "GSM";
                    ECI2eNodebId(this.ECI);
                    return;
                } catch (Exception e) {

                }

            } else if (getType == "WCDMA" && cellInfo instanceof CellInfoWcdma) {
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoWcdma);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.ECI = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mCid");
                    this.TAC = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mLac");
                    this.PCI = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mPci");
                    this.FREQ = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mUarfcn");
//                    this.RSRP = jsonObject.getJSONObject("mCellSignalStrengthWcdma").getInt("mRsrp");
//                    this.RSRQ = jsonObject.getJSONObject("mCellSignalStrengthWcdma").getInt("mRsrq");
                    this.SINR = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");

                    this.cellIdentity = "WCDMA";
                    ECI2eNodebId(this.ECI);
                    return;
                } catch (Exception e) {

                }

            } else if (getType == "LTE" && cellInfo instanceof CellInfoLte) {
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoLte);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.ECI = jsonObject.getJSONObject("mCellIdentityLte").getInt("mCi");
                    this.TAC = jsonObject.getJSONObject("mCellIdentityLte").getInt("mTac");
                    this.PCI = jsonObject.getJSONObject("mCellIdentityLte").getInt("mPci");
                    this.MNC = jsonObject.getJSONObject("mCellIdentityLte").getInt("mMnc");
                    this.FREQ = jsonObject.getJSONObject("mCellIdentityLte").getInt("mEarfcn");

                    this.RSRP = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mRsrp");
                    this.RSRQ = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mRsrq");
                    this.SINR = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");

                    this.cellIdentity = "LTE";
                    ECI2eNodebId(this.ECI);
                    return;
                } catch (Exception e) {

                }
            } else if (getType == "CDMA" && cellInfo instanceof CellInfoCdma) {
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoCdma);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.ECI = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mBasestationId");
                    this.TAC = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mNetworkId");
//                    this.PCI = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mPci");
//                    this.MNC = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mMnc");
                    //this.FREQ = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mEarfcn");

                    this.RSRP = jsonObject.getJSONObject("mCellSignalStrengthCdma").getInt("mCdmaDbm");
//                    this.RSRQ = jsonObject.getJSONObject("mCellSignalStrengthCdma").getInt("mRsrq");
//                    this.SINR = jsonObject.getJSONObject("mCellSignalStrengthCdma").getInt("mSignalStrength");

                    this.cellIdentity = "CDMA";
                    ECI2eNodebId(this.ECI);
                    return;
                } catch (Exception e) {

                }
            }
        }

    }

    private void SetAdjs(List<CellInfo> allCellinfo, String getType) {
        if (allCellinfo == null) return;
        int Count = 0;
        for (int i = 0; i < allCellinfo.size(); i++) {
            CellInfo cellInfo = allCellinfo.get(i);
            if (getType == "GSM" && cellInfo instanceof CellInfoGsm) {
                Count++;
                if(Count==1)break;
                CellInfoGsm cellInfoGsm = (CellInfoGsm) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoGsm);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.Adj_ECI1 = jsonObject.getJSONObject("mCellIdentityGsm").getInt("mCid");
                    this.Adj_SINR1 = jsonObject.getJSONObject("mCellSignalStrengthGsm").getInt("mSignalStrength");
                    return;
                } catch (Exception e) {

                }

            } else if (getType == "WCDMA" && cellInfo instanceof CellInfoWcdma) {
                Count++;
                if(Count==1)break;
                CellInfoWcdma cellInfoWcdma = (CellInfoWcdma) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoWcdma);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.Adj_ECI1 = jsonObject.getJSONObject("mCellIdentityWcdma").getInt("mCid");
                    this.Adj_SINR1 = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");
                    return;
                } catch (Exception e) {

                }
            } else if (getType == "LTE" && cellInfo instanceof CellInfoLte) {
                Count++;
                if(Count==1)break;
                CellInfoLte cellInfoLte = (CellInfoLte) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoLte);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.Adj_ECI1 = jsonObject.getJSONObject("mCellIdentityLte").getInt("mCi");
                    this.Adj_RSRP1 = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mRsrp");
                    this.Adj_SINR1 = jsonObject.getJSONObject("mCellSignalStrengthLte").getInt("mSignalStrength");
                    return;
                } catch (Exception e) {

                }
            } else if (getType == "CDMA" && cellInfo instanceof CellInfoCdma) {
                Count++;
                if(Count==1)break;
                CellInfoCdma cellInfoCdma = (CellInfoCdma) cellInfo;
                Gson gson = new Gson();
                String json = gson.toJson(cellInfoCdma);
                try {
                    JSONObject jsonObject = new JSONObject(json);
                    this.Adj_ECI1 = jsonObject.getJSONObject("mCellIdentityCdma").getInt("mBasestationId");
                    this.Adj_RSRP1 = jsonObject.getJSONObject("mCellSignalStrengthCdma").getInt("mCdmaDbm");
                    return;
                } catch (Exception e) {

                }
            }
        }

    }

    private void ECI2eNodebId(int eci) {
        try {
            if (eci > 0) {
                String h = Integer.toHexString(eci);
                if (h.length() > 2) {
                    String tail = h.substring(h.length() - 2, h.length());
                    int cellid = Integer.valueOf(tail, 16);
                    this.cellId = cellid;
                    String head = h.substring(0, h.length() - 2);
                    int eNB = Integer.valueOf(head, 16);
                    this.eNodeBId = eNB;
                }
            }
        } catch (Exception e) {

        }
    }
}
