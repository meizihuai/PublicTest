package com.getinfo.app.uniqoe;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.getinfo.sdk.qoemaster.QoEHTTPInfo;
import com.getinfo.sdk.qoemaster.QoEVideoInfo;
import com.getinfo.sdk.qoemaster.UploadDataHelper;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//QOE视频打分界面
public class QoEVideoScoreActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "QoEVideoScoreActivity";
    private QoEVideoInfo qoEVideoInfo;
    private QoEHTTPInfo qoEHTTPInfo;
    private LinearLayout iv_rating_div_1, iv_rating_div_2, iv_rating_div_3, iv_rating_div_4, iv_rating_div_5;
    private LinearLayout iv_qx_div_1, iv_qx_div_2, iv_qx_div_3, iv_qx_div_4, iv_qx_div_5;
    private LinearLayout iv_qd_div_1, iv_qd_div_2, iv_qd_div_3, iv_qd_div_4, iv_qd_div_5;//启动速度
    private LinearLayout iv_lc_div_1, iv_lc_div_2, iv_lc_div_3, iv_lc_div_4, iv_lc_div_5;//流畅速度

    private ImageView iv_qx_1, iv_qx_2, iv_qx_3, iv_qx_4, iv_qx_5;//清晰程度
    private ImageView iv_qd_1, iv_qd_2, iv_qd_3, iv_qd_4, iv_qd_5;//启动速度
    private ImageView iv_lc_1, iv_lc_2, iv_lc_3, iv_lc_4, iv_lc_5;//流畅速度
    private TextView tv_commit;
    //  private TextView tv_time;
    private TextView score_des;
    private ImageView iv_rating_1, iv_rating_2, iv_rating_3, iv_rating_4, iv_rating_5;
    private List<RatingBean> listqx, listqd, listlc, listRating;
    private int EVMOS = 0;//顶部打分
    private int ECLATIRY = 0;//清晰度打分
    private int ELOAD = 0;//启动速度打分
    private int ESTALL = 0;//流畅速度打分
    private boolean flagSubmited = false;
    private String scoreKind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_qo_evideo_score);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this, 0x05ACED, 0);
        Intent getIntent = getIntent();
        scoreKind= getIntent.getStringExtra("ScoreKind");
        if(scoreKind.equals("QoEVideo")){
            String qoEVideoInfoJson = getIntent.getStringExtra("QoEVideoInfo");
            Gson gson = new Gson();
            qoEVideoInfo = gson.fromJson(qoEVideoInfoJson, QoEVideoInfo.class);
        }
        if(scoreKind.equals("QoEHTTP")){
            String qoEHTTPInfoJson = getIntent.getStringExtra("QoEHTTPInfo");
            Gson gson = new Gson();
            qoEHTTPInfo = gson.fromJson(qoEHTTPInfoJson, QoEHTTPInfo.class);

        }

        init();
        WaitForSubmit();
    }

    private void WaitForSubmit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int waitMinute = 3;
                    Thread.sleep(waitMinute * 60 * 1000);
                } catch (Exception e) {

                }
                if (!flagSubmited) {
                    Submit();
                    finish();
                }
            }
        }).start();
    }

    private String GetSystemTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String startTime = simpleDateFormat.format(date);
        return startTime;
    }

    private void init() {
        score_des = (TextView) findViewById(R.id.score_des);
//        tv_time=findViewById(R.id.tv_time);
//        tv_time.setText(GetSystemTime());

        tv_commit = (TextView) findViewById(R.id.tv_commit);
        tv_commit.setOnClickListener(this);
        tv_commit.setVisibility(View.INVISIBLE);

        if(scoreKind.equals("QoEHTTP")) {
            TextView tv_qidong=findViewById(R.id.tv_qidong);
            TextView tv_liuchangdu=findViewById(R.id.tv_liuchangdu);
            TextView tv_qingxidu=findViewById(R.id.tv_qingxidu);
            tv_qidong.setText("白屏时间评分");
            tv_liuchangdu.setText("页面响应评分");
            tv_qingxidu.setText("整体加载评分");
        }



        listRating = new ArrayList<>();
        listqx = new ArrayList<>();
        listqd = new ArrayList<>();
        listlc = new ArrayList<>();
        iv_rating_1 = (ImageView) findViewById(R.id.iv_rating_1);
        iv_rating_1.setOnClickListener(this);
        iv_rating_div_1 = findViewById(R.id.iv_rating_div_1);
        iv_rating_div_1.setOnClickListener(this);
        RatingBean ratingBeanOne = new RatingBean(iv_rating_1);
        ratingBeanOne.setSelected(false);
        listRating.add(ratingBeanOne);

        iv_rating_2 = (ImageView) findViewById(R.id.iv_rating_2);
        iv_rating_2.setOnClickListener(this);
        iv_rating_div_2 = findViewById(R.id.iv_rating_div_2);
        iv_rating_div_2.setOnClickListener(this);
        RatingBean ratingBeanTwo = new RatingBean(iv_rating_2);
        ratingBeanTwo.setSelected(false);
        listRating.add(ratingBeanTwo);

        iv_rating_3 = (ImageView) findViewById(R.id.iv_rating_3);
        iv_rating_3.setOnClickListener(this);
        iv_rating_div_3 = findViewById(R.id.iv_rating_div_3);
        iv_rating_div_3.setOnClickListener(this);
        RatingBean ratingBeanThree = new RatingBean(iv_rating_3);
        ratingBeanThree.setSelected(false);
        listRating.add(ratingBeanThree);

        iv_rating_4 = (ImageView) findViewById(R.id.iv_rating_4);
        iv_rating_4.setOnClickListener(this);
        iv_rating_div_4 = findViewById(R.id.iv_rating_div_4);
        iv_rating_div_4.setOnClickListener(this);
        RatingBean ratingBeanFour = new RatingBean(iv_rating_4);
        ratingBeanFour.setSelected(false);
        listRating.add(ratingBeanFour);

        iv_rating_5 = (ImageView) findViewById(R.id.iv_rating_5);
        iv_rating_5.setOnClickListener(this);
        iv_rating_div_5 = findViewById(R.id.iv_rating_div_5);
        iv_rating_div_5.setOnClickListener(this);
        RatingBean ratingBeanFive = new RatingBean(iv_rating_5);
        ratingBeanFive.setSelected(false);
        listRating.add(ratingBeanFive);

        iv_qx_1 = (ImageView) findViewById(R.id.iv_qx_1);
        iv_qx_1.setOnClickListener(this);
        iv_qx_div_1 = findViewById(R.id.iv_qx_div_1);
        iv_qx_div_1.setOnClickListener(this);
        RatingBean RatingBeanOne = new RatingBean(iv_qx_1);
        RatingBeanOne.setSelected(false);
        listqx.add(RatingBeanOne);

        iv_qx_2 = (ImageView) findViewById(R.id.iv_qx_2);
        iv_qx_2.setOnClickListener(this);
        iv_qx_div_2 = findViewById(R.id.iv_qx_div_2);
        iv_qx_div_2.setOnClickListener(this);
        RatingBean RatingBeanTwo = new RatingBean(iv_qx_2);
        RatingBeanTwo.setSelected(false);
        listqx.add(RatingBeanTwo);

        iv_qx_3 = (ImageView) findViewById(R.id.iv_qx_3);
        iv_qx_3.setOnClickListener(this);
        iv_qx_div_3 = findViewById(R.id.iv_qx_div_3);
        iv_qx_div_3.setOnClickListener(this);
        RatingBean RatingBeanThree = new RatingBean(iv_qx_3);
        RatingBeanThree.setSelected(false);
        listqx.add(RatingBeanThree);

        iv_qx_4 = (ImageView) findViewById(R.id.iv_qx_4);
        iv_qx_4.setOnClickListener(this);
        iv_qx_div_4 = findViewById(R.id.iv_qx_div_4);
        iv_qx_div_4.setOnClickListener(this);
        RatingBean RatingBeanFour = new RatingBean(iv_qx_4);
        RatingBeanFour.setSelected(false);
        listqx.add(RatingBeanFour);

        iv_qx_5 = (ImageView) findViewById(R.id.iv_qx_5);
        iv_qx_5.setOnClickListener(this);
        iv_qx_div_5 = findViewById(R.id.iv_qx_div_5);
        iv_qx_div_5.setOnClickListener(this);
        RatingBean RatingBeanFive = new RatingBean(iv_qx_5);
        RatingBeanFive.setSelected(false);
        listqx.add(RatingBeanFive);

        iv_qd_1 = (ImageView) findViewById(R.id.iv_qd_1);
        iv_qd_1.setOnClickListener(this);
        iv_qd_div_1 = findViewById(R.id.iv_qd_div_1);
        iv_qd_div_1.setOnClickListener(this);
        RatingBean rb_qd_1 = new RatingBean(iv_qd_1);
        rb_qd_1.setSelected(false);
        listqd.add(rb_qd_1);

        iv_qd_2 = (ImageView) findViewById(R.id.iv_qd_2);
        iv_qd_2.setOnClickListener(this);
        iv_qd_div_2 = findViewById(R.id.iv_qd_div_2);
        iv_qd_div_2.setOnClickListener(this);
        RatingBean rb_qd_2 = new RatingBean(iv_qd_2);
        rb_qd_2.setSelected(false);
        listqd.add(rb_qd_2);

        iv_qd_3 = (ImageView) findViewById(R.id.iv_qd_3);
        iv_qd_3.setOnClickListener(this);
        iv_qd_div_3 = findViewById(R.id.iv_qd_div_3);
        iv_qd_div_3.setOnClickListener(this);
        RatingBean rb_qd_3 = new RatingBean(iv_qd_3);
        rb_qd_3.setSelected(false);
        listqd.add(rb_qd_3);

        iv_qd_4 = (ImageView) findViewById(R.id.iv_qd_4);
        iv_qd_4.setOnClickListener(this);
        iv_qd_div_4 = findViewById(R.id.iv_qd_div_4);
        iv_qd_div_4.setOnClickListener(this);
        RatingBean rb_qd_4 = new RatingBean(iv_qd_4);
        rb_qd_4.setSelected(false);
        listqd.add(rb_qd_4);

        iv_qd_5 = (ImageView) findViewById(R.id.iv_qd_5);
        iv_qd_5.setOnClickListener(this);
        iv_qd_div_5 = findViewById(R.id.iv_qd_div_5);
        iv_qd_div_5.setOnClickListener(this);
        RatingBean rb_qd_5 = new RatingBean(iv_qd_5);
        rb_qd_5.setSelected(false);
        listqd.add(rb_qd_5);

        iv_lc_1 = (ImageView) findViewById(R.id.iv_lc_1);
        iv_lc_1.setOnClickListener(this);
        iv_lc_div_1 = findViewById(R.id.iv_lc_div_1);
        iv_lc_div_1.setOnClickListener(this);
        RatingBean rb_lc_1 = new RatingBean(iv_lc_1);
        rb_lc_1.setSelected(false);
        listlc.add(rb_lc_1);

        iv_lc_2 = (ImageView) findViewById(R.id.iv_lc_2);
        iv_lc_2.setOnClickListener(this);
        iv_lc_div_2 = findViewById(R.id.iv_lc_div_2);
        iv_lc_div_2.setOnClickListener(this);
        RatingBean rb_lc_2 = new RatingBean(iv_lc_2);
        rb_lc_2.setSelected(false);
        listlc.add(rb_lc_2);

        iv_lc_3 = (ImageView) findViewById(R.id.iv_lc_3);
        iv_lc_3.setOnClickListener(this);
        iv_lc_div_3 = findViewById(R.id.iv_lc_div_3);
        iv_lc_div_3.setOnClickListener(this);
        RatingBean rb_lc_3 = new RatingBean(iv_lc_3);
        rb_lc_3.setSelected(false);
        listlc.add(rb_lc_3);

        iv_lc_4 = (ImageView) findViewById(R.id.iv_lc_4);
        iv_lc_4.setOnClickListener(this);
        iv_lc_div_4 = findViewById(R.id.iv_lc_div_4);
        iv_lc_div_4.setOnClickListener(this);
        RatingBean rb_lc_4 = new RatingBean(iv_lc_4);
        rb_lc_4.setSelected(false);
        listlc.add(rb_lc_4);

        iv_lc_5 = (ImageView) findViewById(R.id.iv_lc_5);
        iv_lc_5.setOnClickListener(this);
        iv_lc_div_5 = findViewById(R.id.iv_lc_div_5);
        iv_lc_div_5.setOnClickListener(this);
        RatingBean rb_lc_5 = new RatingBean(iv_lc_5);
        rb_lc_5.setSelected(false);
        listlc.add(rb_lc_5);


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //如果没有打分，则直接上报
            Submit();
            finish();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_commit://提交
                btnCommitOnClick();
                break;
            case R.id.iv_rating_1:
                setRating(0);
                break;
            case R.id.iv_rating_div_1:
                setRating(0);
                break;
            case R.id.iv_rating_2:
                setRating(1);
                break;
            case R.id.iv_rating_div_2:
                setRating(1);
                break;
            case R.id.iv_rating_3:
                setRating(2);
                break;
            case R.id.iv_rating_div_3:
                setRating(2);
                break;
            case R.id.iv_rating_4:
                setRating(3);
                break;
            case R.id.iv_rating_div_4:
                setRating(3);
                break;
            case R.id.iv_rating_5:
                setRating(4);
                break;
            case R.id.iv_rating_div_5:
                setRating(4);
                break;
            case R.id.iv_qx_1:
                setqx(0);
                break;
            case R.id.iv_qx_div_1:
                setqx(0);
                break;
            case R.id.iv_qx_2:
                setqx(1);
                break;
            case R.id.iv_qx_div_2:
                setqx(1);
                break;
            case R.id.iv_qx_3:
                setqx(2);
                break;
            case R.id.iv_qx_div_3:
                setqx(2);
                break;
            case R.id.iv_qx_4:
                setqx(3);
                break;
            case R.id.iv_qx_div_4:
                setqx(3);
                break;
            case R.id.iv_qx_5:
                setqx(4);
                break;
            case R.id.iv_qx_div_5:
                setqx(4);
                break;
            case R.id.iv_qd_1:
                setqd(0);
                break;
            case R.id.iv_qd_div_1:
                setqd(0);
                break;
            case R.id.iv_qd_2:
                setqd(1);
                break;
            case R.id.iv_qd_div_2:
                setqd(1);
                break;
            case R.id.iv_qd_3:
                setqd(2);
                break;
            case R.id.iv_qd_div_3:
                setqd(2);
                break;
            case R.id.iv_qd_4:
                setqd(3);
                break;
            case R.id.iv_qd_div_4:
                setqd(3);
                break;
            case R.id.iv_qd_5:
                setqd(4);
                break;
            case R.id.iv_qd_div_5:
                setqd(4);
                break;
            case R.id.iv_lc_1:
                setlc(0);
                break;
            case R.id.iv_lc_div_1:
                setlc(0);
                break;
            case R.id.iv_lc_2:
                setlc(1);
                break;
            case R.id.iv_lc_div_2:
                setlc(1);
                break;
            case R.id.iv_lc_3:
                setlc(2);
                break;
            case R.id.iv_lc_div_3:
                setlc(2);
                break;
            case R.id.iv_lc_4:
                setlc(3);
                break;
            case R.id.iv_lc_div_4:
                setlc(4);
                break;
            case R.id.iv_lc_5:
                setlc(4);
                break;
            case R.id.iv_lc_div_5:
                setlc(4);
                break;
        }
    }

    private void setRating(int index) {
        for (int i = 0; i < listRating.size(); i++) {
            if (i <= index) {
                listRating.get(i).setSelected(true);
            } else {
                listRating.get(i).setSelected(false);
            }
        }
        if (index == 0) score_des.setText("渣到爆了，无力吐槽");
        if (index == 1) score_des.setText("不满意，要加油呀");
        if (index == 2) score_des.setText("一般般，希望会更好");
        if (index == 3) score_des.setText("满意，继续努力");
        if (index == 4) score_des.setText("给你满分，让你骄傲");
        //sQualityValue=index+1;
        EVMOS = index + 1;
        Log.i(TAG, "EVMOS=" + EVMOS);
        CheckSubmit();

    }

    private void setqx(int index) {
        for (int i = 0; i < listqx.size(); i++) {
            if (i <= index) {
                listqx.get(i).setSelected(true);
            } else {
                listqx.get(i).setSelected(false);
            }
        }
        ECLATIRY = index + 1;
        Log.i(TAG, "ECLATIRY=" + ECLATIRY);
        CheckSubmit();
    }

    private void setqd(int index) {
        for (int i = 0; i < listqd.size(); i++) {
            if (i <= index) {
                listqd.get(i).setSelected(true);
            } else {
                listqd.get(i).setSelected(false);
            }
        }
        ELOAD = index + 1;
        Log.i(TAG, "ELOAD=" + ELOAD);
        CheckSubmit();
    }

    private void setlc(int index) {
        for (int i = 0; i < listlc.size(); i++) {
            if (i <= index) {
                listlc.get(i).setSelected(true);
            } else {
                listlc.get(i).setSelected(false);
            }
        }
        ESTALL = index + 1;
        Log.i(TAG, "ESTALL=" + ESTALL);
        CheckSubmit();
    }

    private void btnCommitOnClick() {
        if (EVMOS == 0) { //顶部打分
            String str="请给出顶部总评分，谢谢！";
            if(scoreKind.equals("QoEHTTP")) {
                str="请给出顶部总评分，谢谢！";
            }
            MsgBox(str);
            return;
        }
        if (ECLATIRY == 0) { //清晰度打分
            String str="请给清晰程度评分，谢谢！";
            if(scoreKind.equals("QoEHTTP")) {
                str="请给出白屏时间评分，谢谢！";
            }
            MsgBox(str);
            return;
        }
        if (ELOAD == 0) { //启动速度打分
            String str="请给加载速度评分，谢谢！";
            if(scoreKind.equals("QoEHTTP")) {
                str="请给出页面响应评分，谢谢！";
            }
            MsgBox(str);
            return;
        }
        if (ESTALL == 0) {  //流畅速度打分
            String str="请给流畅速度评分，谢谢！";
            if(scoreKind.equals("QoEHTTP")) {
                str="请给出整体加载评分，谢谢！";
            }
            MsgBox(str);
            return;
        }
        if(scoreKind.equals("QoEVideo")) {
            qoEVideoInfo.ECLATIRY = ECLATIRY;
            qoEVideoInfo.ELOAD = ELOAD;
            qoEVideoInfo.ESTALL = ESTALL;
            qoEVideoInfo.EVMOS = EVMOS;
            qoEVideoInfo.ELIGHT = 0;
            qoEVideoInfo.ESTATE = 0;
        }
        if(scoreKind.equals("QoEHTTP")) {
            qoEHTTPInfo.EVMOS=EVMOS;
            qoEHTTPInfo.EWHITESCREENTIMESCORE=ELOAD;
            qoEHTTPInfo.ERESPONSETIMESCORE=ESTALL;
            qoEHTTPInfo.ETOTALBUFFERTIMESCORE=ECLATIRY;
        }
//        public int USER_SCORE;    //用户分值
//        public int VMOS;    //VMOS
//        public long PACKET_LOSS;    //丢包数
//        public int ELOAD;    ///*用户对视频播放等待时间的评分(5：无法察觉到缓冲，4：缓冲时间很短，3：缓冲时间长度一般，2：缓冲时间较长，1：缓冲时间过长无法容忍)*/
//        public int ESTALL;    ///*用户对流畅度的评分(5:毫无卡顿，4：略有卡顿但不影响观看，3：有卡顿对观看造成一定影响，2：有卡顿对观看造成较大影响，1：卡顿过多无法容忍)*/
//        public int EVMOS;    ///*用户对整体视频服务的综合评分(5:非常好，4：良好，3：一般，2：较差，1：无法容忍)*/
//        public int ELIGHT;    ///*环境光照对视频观看的影响程度(5：无影响，4：较小影响，3：有一定影响，2：较大影响，1：极大影响）*/
//        public int ESTATE;    ///*用户对运动状态的反馈(:4：静止不动，3：偶尔走动，2：持续走动，1：交通工具上)*/
        //clarity

        Submit();
        MsgBox("您的评分已提交！谢谢您！");
        finish();
    }

    private void CheckSubmit() {
        if (EVMOS == 0) { //顶部打分

            return;
        }
        if (ECLATIRY == 0) { //清晰度打分

            return;
        }
        if (ELOAD == 0) { //启动速度打分

            return;
        }
        if (ESTALL == 0) {  //流畅速度打分

            return;
        }
        btnCommitOnClick();
    }

    public void Submit() {
        if (flagSubmited) return;
        if(scoreKind.equals("QoEVideo")){
            UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
            uploadDataHelper.UploadDataToServer(qoEVideoInfo);
        }
        if(scoreKind.equals("QoEHTTP")){
            UploadDataHelper uploadDataHelper = UploadDataHelper.getInstance();
            uploadDataHelper.UploadObjectToServer(qoEHTTPInfo, "QoEHTTPInfo");
        }
        flagSubmited = true;
    }


    private void MsgBox(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
