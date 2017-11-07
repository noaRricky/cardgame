package com.zsh.ricky.cardmanager;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.concurrent.Callable;

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
    private EditText tv_playerA;
    private EditText tv_playerB;
    private EditText tv_winner;
    private TextView tv_date;
    private TextView tv_time;
    private Calendar calendar;  //通过calendar获取系统时间
    private int mYear, mMonth, mDay;
    private int mHour, mMinute;

    private GameHistory rev_arg=new GameHistory();


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_info);

        Bundle bundle = this.getIntent().getExtras();
        rev_arg.setHistoryNum(Integer.valueOf(bundle.getString(PublicFuntion.HistoryNum)));
        rev_arg.setPlayerA(bundle.getString(PublicFuntion.PlayerA));
        rev_arg.setPlayerB(bundle.getString(PublicFuntion.PlayerB));
        rev_arg.setWinner(bundle.getString(PublicFuntion.Winner));
        rev_arg.setDate(bundle.getString(PublicFuntion.Date));
        rev_arg.setTime(bundle.getString(PublicFuntion.Time));

        initWeight();
        initEvent();
    }

    private void initWeight() {
        bt_update = (Button) this.findViewById(R.id.hi_btChange);
        bt_delete = (Button) this.findViewById(R.id.hi_btDelete);
        bt_back = (ImageButton) this.findViewById(R.id.hi_backImageButton);
        tv_playerA = (EditText) this.findViewById(R.id.hi_playerA);
        tv_playerB = (EditText) this.findViewById(R.id.hi_playerB);
        tv_winner = (EditText) this.findViewById(R.id.hi_etWinner);
        tv_date = (TextView) this.findViewById(R.id.hi_etDate);
        tv_time = (TextView) this.findViewById(R.id.hi_etTime);

        tv_playerA.setText(rev_arg.getPlayerA());
        tv_playerB.setText(rev_arg.getPlayerB());
        tv_winner.setText(rev_arg.getWinner());
        tv_date.setText(rev_arg.getDate());
        tv_time.setText(rev_arg.getTime());
    }

    /**
     * 设置所有控件按下时的动作
     */
    private void initEvent() {
        //设置更新按钮按下
        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validate()) {
                    Map<String, String> post_data = getPostData();
                    OkHttpHelper helper = new OkHttpHelper();
                    Call update = helper.postRequest(UrlResources.UPDATE_HISTORY, post_data);
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

        bt_delete.setOnClickListener(new DeleteClick());
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryInfoActivity.this,
                        AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     *  检查信息是否为空，并且胜者是对站者中的一个
     * @return 出现空的返回false, 否则返回true
     */
    private boolean validate() {
        String playerA, playerB, winner;
        playerA = tv_playerA.getText().toString().trim();
        playerB = tv_playerB.getText().toString().trim();
        winner = tv_winner.getText().toString().trim();

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
            Toast.makeText(HistoryInfoActivity.this, "胜者应该在对战者之中",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private Map<String,String> getPostData(){
        Map<String,String> temp=new HashMap<String, String>();
        temp.put(PublicFuntion.HistoryNum,String.valueOf(rev_arg.getHistoryNum()));
        temp.put(PublicFuntion.PlayerA,tv_playerA.getText().toString());
        temp.put(PublicFuntion.PlayerB,tv_playerB.getText().toString());
        temp.put(PublicFuntion.Winner,tv_winner.getText().toString());
        temp.put(PublicFuntion.Date,tv_date.getText().toString());
        temp.put(PublicFuntion.Time,tv_time.getText().toString());
        return temp;
    }

    /**
     * 删除按钮按下响应事件
     */
    private class DeleteClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Map<String, String> map = new HashMap<>();
            map.put(PublicFuntion.HistoryNum, String.valueOf(rev_arg.getHistoryNum()));

            OkHttpHelper helper = new OkHttpHelper();
            Call delete = helper.postRequest(UrlResources.DELETE_HISTORY, map);

            delete.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(getApplicationContext(), "连接服务器失败",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Toast.makeText(getApplicationContext(), "删除数据成功",
                            Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(HistoryInfoActivity.this,
                                    AdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 1000);
                }
            });
        }
    }

}
