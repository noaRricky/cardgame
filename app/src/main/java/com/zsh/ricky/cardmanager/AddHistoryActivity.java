package com.zsh.ricky.cardmanager;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.PublicFuntion;
import com.zsh.ricky.cardmanager.util.UrlResources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddHistoryActivity extends AppCompatActivity {

    private EditText etPlayerA, etPlayerB;
    private EditText etWinner;
    private TextView etDate, etTime;
    private Button btAdd;
    private ImageButton ibBack;
    private JSONObject history;

    // 定义存储时间数据
    private Calendar calendar;  //通过calendar获取系统时间
    private int mYear, mMonth, mDay;
    private int mHour, mMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_history);

        setItem();

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddHistoryActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
        etDate.setOnClickListener(new DateClick());
        etTime.setOnClickListener(new TimeClick());
        btAdd.setOnClickListener(new AddClickListener());
    }

    private void setItem() {
        etPlayerA = (EditText) findViewById(R.id.hd_playerA);
        etPlayerB = (EditText) findViewById(R.id.hd_playerB);
        etWinner = (EditText) findViewById(R.id.hd_etWinner);
        etDate = (TextView) findViewById(R.id.hd_etDate);
        etTime = (TextView) findViewById(R.id.hd_etTime);
        btAdd = (Button) findViewById(R.id.hd_btAdd);
        ibBack = (ImageButton) findViewById(R.id.hd_backImageButton);
        calendar = Calendar.getInstance();
    }

    /**
     * 检查信息是否为空，并且胜者是对站者中的一个
     * @return 满足条件返回true,否则false
     */
    private boolean validate() {
        String playerA, playerB, winner;
        playerA = etPlayerA.getText().toString();
        playerB = etPlayerB.getText().toString();
        winner = etWinner.getText().toString();

        if (playerA.equals("")) {
            Toast.makeText(getApplicationContext(), "玩家A不能为空!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (playerB.equals("")) {
            Toast.makeText(getApplicationContext(), "玩家B不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (winner.equals("")) {
            Toast.makeText(getApplicationContext(), "胜者不能为空!",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!(playerA.equals(winner) || playerB.equals(winner))) {
            Toast.makeText(AddHistoryActivity.this, "胜者应该在对战者之中",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 清空所有数据
     */
    private void refresh() {
        etPlayerA.setText("");
        etPlayerB.setText("");
        etWinner.setText("");
    }

    private class AddClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (validate()) {
                String playerA = etPlayerA.getText().toString();
                String playerB = etPlayerB.getText().toString();
                String winner = etWinner.getText().toString();
                String date = etDate.getText().toString();
                String time = etTime.getText().toString();

                Map<String, String> map = new HashMap<>();
                map.put(PublicFuntion.PlayerA, playerA);
                map.put(PublicFuntion.PlayerB, playerB);
                map.put(PublicFuntion.Winner, winner);
                map.put(PublicFuntion.Date, date);
                map.put(PublicFuntion.Time, time);

                OkHttpHelper helper = new OkHttpHelper();
                Call call = helper.postRequest(UrlResources.ADD_HISTORY, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "服务器连接失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {

                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());
                            boolean result = jsonObj.getBoolean("result");

                            if (result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "添加数据成功",
                                                Toast.LENGTH_SHORT).show();
                                        refresh();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "添加数据失败",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    /**
     * 设定点击日期文本框触发事件
     */
    private class DateClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new DatePickerDialog(AddHistoryActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            mYear = year;
                            mMonth = month;
                            mDay = dayOfMonth;
                            //更新EditText控件
                            etDate.setText(new StringBuilder()
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
            new TimePickerDialog(AddHistoryActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;

                            etTime.setText(new StringBuilder()
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
