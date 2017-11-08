package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.util.OkHttpHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChangeActivity extends AppCompatActivity {

    private EditText etNewPassword, etRepeat;
    private Button yesButton, cancelButton;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change);

        initItem();
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChangeActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        yesButton.setOnClickListener(new YesOnClickListener());
    }

    /**
     * 初始化控件
     */
    private void initItem() {
        etNewPassword = (EditText) this.findViewById(R.id.etNewPassword);
        etRepeat = (EditText) this.findViewById(R.id.etRepeatPassword);
        yesButton = (Button) this.findViewById(R.id.cp_YesButton);
        cancelButton = (Button) this.findViewById(R.id.cp_CancelButton);
        Intent intent = getIntent();
        userID = intent.getStringExtra("userID");
    }

    private class YesOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (validate()) {
                String password = etRepeat.getText().toString();
                Map<String, String> map = new HashMap<>();

                map.put("userID", userID);
                map.put("new_password", password);
                String url = "change";

                OkHttpHelper httpHelper = new OkHttpHelper();
                Call call = httpHelper.postRequest(url, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "服务器连接失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
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
                                        Toast.makeText(getApplicationContext(), "修改密码成功",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });

                                //让线程沉睡2秒后跳转
                                new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }

                                        Intent intent = new Intent(ChangeActivity.this,
                                                LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }.start();
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "修改密码失败",
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
     * 验证填写内容是否为空
     * @return 空返回false, 否则返回true
     */
    private boolean validate() {
        String new_password = etNewPassword.getText().toString().trim();
        String repeat_password = etRepeat.getText().toString().trim();

        if (new_password.equals(""))
        {
            Toast.makeText(getApplicationContext(), "新密码不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (repeat_password.equals(""))
        {
            Toast.makeText(getApplicationContext(), "重复密码不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!repeat_password.equals(new_password)) {
            Toast.makeText(getApplicationContext(), "密码必须相同！",
                    Toast.LENGTH_SHORT).show();
        }

        return true;
    }
}
