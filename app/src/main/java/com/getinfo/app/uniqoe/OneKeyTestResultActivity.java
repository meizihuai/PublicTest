package com.getinfo.app.uniqoe;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

public class OneKeyTestResultActivity extends AppCompatActivity {
    private  OneKeyTestInfo oneKeyTestInfo=null;
    private TextView txtTime,txtNetType,txtNetSpeedTestSpeed,txtNetSpeedScore,txtVideoSpeed,txtVideoScore,txtHttpResonseTime,txtHttpScore,txtTotalScore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_key_test_result);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        StatusBarUtil.setColor(this,0x05ACED,0);  //设置状态栏颜色，保持沉浸
        Intent intent=getIntent();
        String str=intent.getStringExtra("oneKeyTestInfo");
        Gson gson=new Gson();
        oneKeyTestInfo=gson.fromJson(str,OneKeyTestInfo.class);
        txtTime=findViewById(R.id.txtTime);
        txtNetType=findViewById(R.id.txtNetType);
        txtNetSpeedTestSpeed=findViewById(R.id.txtNetSpeedTestSpeed);
        txtNetSpeedScore=findViewById(R.id.txtNetSpeedScore);
        txtVideoSpeed=findViewById(R.id.txtVideoSpeed);
        txtVideoScore=findViewById(R.id.txtVideoScore);
        txtHttpResonseTime=findViewById(R.id.txtHttpResonseTime);
        txtHttpScore=findViewById(R.id.txtHttpScore);
        txtTotalScore=findViewById(R.id.txtTotalScore);
        TextView txt_back=findViewById(R.id.txt_goback);
        txt_back.setClickable(true);
        txt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
        ShowInfo();
    }
    private  void init(){
        final  Activity activity=this;
        Button btnOpenHistory=findViewById(R.id.btnOpenHistory);
        btnOpenHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, OneKeyTestHisActivity.class);
                startActivity(intent);
            }
        });

    }
    private void ShowInfo(){
        txtTime.setText(GlobalInfo.GetSystemTime());
        txtNetType.setText(oneKeyTestInfo.pi.netType);
        double mbps=((double)oneKeyTestInfo.netSpeedTestSpeed*8/1000);
        txtNetSpeedTestSpeed.setText(String.format("%.2f",mbps)+" Mbps");
        txtNetSpeedScore.setText(oneKeyTestInfo.netSpeedScore.score+" 分");
        mbps=((double)oneKeyTestInfo.videoTestSpeed*8/1000);
        txtVideoSpeed.setText(String.format("%.2f",mbps)+" Mbps");
        txtVideoScore.setText(oneKeyTestInfo.videoScore.score+" 分");
        txtHttpResonseTime.setText(oneKeyTestInfo.httpResonseTime+" ms");
        txtHttpScore.setText(oneKeyTestInfo.htmlPageScore.score+" 分");
        txtTotalScore.setText(oneKeyTestInfo.togetherScore.score+" 分");
//        int totalScore=oneKeyTestInfo.netSpeedScore.score+oneKeyTestInfo.videoScore.score+oneKeyTestInfo.htmlPageScore.score;
//        totalScore=(int)(totalScore/3);
//        txtTotalScore.setText(totalScore+" 分");
    }
}
