package com.zsh.ricky.cardmanager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.model.GameHistory;
import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.PublicFuntion;
import com.zsh.ricky.cardmanager.util.UrlResources;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Thinker on 2017/11/5.
 */

public class HistoryInfoActivity extends AppCompatActivity {
    private Button bt_update;
    private Button bt_delete;
    private ImageButton bt_back;
    private EditText tv_plaerA;
    private EditText tv_plaerB;
    private EditText tv_winner;
    private EditText tv_date;
    private EditText tv_time;
    private Calendar calendar;  //通过calendar获取系统时间
    private int mYear, mMonth, mDay;
    private int mHour, mMinute;

    private GameHistory rev_arg=new GameHistory();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardinfo);

        Bundle bundle = this.getIntent().getExtras();
        rev_arg.setHistoryNum(Integer.valueOf(bundle.getString(PublicFuntion.HistoryNum)));
        rev_arg.setPlayerA(bundle.getString(PublicFuntion.PlayerA));
        rev_arg.setPlayerB(bundle.getString(PublicFuntion.PlayerB));
        rev_arg.setWinner(bundle.getString(PublicFuntion.Winner));
        rev_arg.setDate(bundle.getString(PublicFuntion.Date));
        rev_arg.setTime(bundle.getString(PublicFuntion.Time));

        initWeight();

        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_plaerA.getText().toString()!=null && tv_plaerB.getText().toString()!=null
                        && tv_winner.getText().toString()!=null && tv_date.getText().toString()!=null
                        && tv_time.getText().toString()!=null){
                    Map<String,String> post_data=getPostData();
                    OkHttpHelper helper=new OkHttpHelper();
                    Call update=helper.postRequest(UrlResources.UPDATE_HISTORY,post_data);
                    update.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(HistoryInfoActivity.this, "与服务器连接失败！请稍后再试！",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Toast.makeText(getApplicationContext(), "更新成功！",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(HistoryInfoActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }

            }
        });
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_plaerA.getText().toString()!=null && tv_plaerB.getText().toString()!=null
                        && tv_winner.getText().toString()!=null && tv_date.getText().toString()!=null
                        && tv_time.getText().toString()!=null){
                    Map<String,String> post_data=getPostData();
                    OkHttpHelper helper=new OkHttpHelper();
                    Call delete=helper.postRequest(UrlResources.DELETE_HISTORY,post_data);
                    delete.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Toast.makeText(HistoryInfoActivity.this, "与服务器连接失败！请稍后再试！",
                                    Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Toast.makeText(HistoryInfoActivity.this, "删除成功！",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(HistoryInfoActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }
            }
        });
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryInfoActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    @SuppressLint("WrongViewCast")
    private void initWeight() {
        bt_update=(Button)findViewById(R.id.hi_btChange);
        bt_delete=(Button)findViewById(R.id.hi_btDelete);
        bt_back=(ImageButton)findViewById(R.id.hi_backImageButton);
        tv_plaerA=(EditText)findViewById(R.id.hi_playerA);
        tv_plaerB=(EditText)findViewById(R.id.hi_playerB);
        tv_winner=(EditText)findViewById(R.id.hi_etWinner);
        tv_date=(EditText)findViewById(R.id.hi_etDate);
        tv_time=(EditText)findViewById(R.id.hi_etTime);

        tv_plaerA.setText(rev_arg.getPlayerA());
        tv_plaerB.setText(rev_arg.getPlayerB());
        tv_winner.setText(rev_arg.getWinner());
        tv_date.setText(rev_arg.getDate());
        tv_time.setText(rev_arg.getTime());
    }
    private Map<String,String> getPostData(){
        Map<String,String> temp=new HashMap<String, String>();
        temp.put(PublicFuntion.HistoryNum,String.valueOf(rev_arg.getHistoryNum()));
        temp.put(PublicFuntion.PlayerA,tv_plaerA.getText().toString());
        temp.put(PublicFuntion.PlayerB,tv_plaerB.getText().toString());
        temp.put(PublicFuntion.Winner,tv_winner.getText().toString());
        temp.put(PublicFuntion.Date,tv_date.getText().toString());
        temp.put(PublicFuntion.Time,tv_time.getText().toString());
        return temp;
    }

    /**
     * 设定点击日期文本框触发事件
     */
    private class DateClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new DatePickerDialog(HistoryInfoActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            mYear = year;
                            mMonth = month;
                            mDay = dayOfMonth;
                            //更新EditText控件
                            tv_date.setText(new StringBuilder()
                                    .append(mYear)
                                    .append("-")
                                    .append((mMonth + 1) < 10 ? "0"
                                            + (mMonth + 1) : (mMonth + 1))
                                    .append("-")
                                    .append((mDay < 10) ? "0" + mDay : mDay));
                        }
                    },calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        }
    }

    /**
     * 设定点击事件文本框触发事件
     */
    private class TimeClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new TimePickerDialog(HistoryInfoActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;

                            tv_time.setText(new StringBuilder()
                                    .append(mHour < 10 ? "0" + mHour : mHour)
                                    .append(":")
                                    .append(mMinute < 10 ? "0" + mMinute : mMinute)
                                    .append(":00"));
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true).show();
        }
    }
}
