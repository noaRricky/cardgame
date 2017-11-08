package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.UrlResources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etID, etPass; //定义用户名，和密码输出框
    private Button bnLogin, bnRegister,bnForget;  //定义登录，注册，忘记密码按钮
    JSONObject forget_json = null;

    private static final int PLAYER = 1;
    private static final int ADMIN = 0;
    private static final int NO_EXIST = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initItem();

        bnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
        bnForget.setOnClickListener(new ForgetOnClickListener());
        bnLogin.setOnClickListener(new LoginOnClickListener());
    }

    private void initItem()
    {
        //获取界面中两个编辑器
        etID = (EditText) this.findViewById(R.id.EditTextUserID);
        etPass = (EditText) this.findViewById(R.id.EditTextPassword);
        //获取界面中的两个按钮
        bnLogin = (Button) this.findViewById(R.id.btnLogin);
        bnRegister = (Button) this.findViewById(R.id.btnRegister);
        bnForget = (Button) this.findViewById(R.id.btnForget);
    }

    /**
     * 验证用户名和密码是否为空
     * @return 如果有某一项为空，返回false,否则返回true
     */
    private boolean validate()
    {
        String userID = etID.getText().toString().trim();
        if (userID.equals(""))
        {
            Toast.makeText(getApplicationContext(), "用户ID是必填项!", Toast.LENGTH_SHORT).show();
            return false;
        }
        String pwd = etPass.getText().toString().trim();
        if (pwd.equals(""))
        {
            Toast.makeText(getApplicationContext(), "密码是必填项!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 处理登录按钮事件
     */
    private class LoginOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            if (validate()) {
                String userID = etID.getText().toString();
                String password = etPass.getText().toString();

                String url = "login";
                Map<String, String> map = new HashMap<>();
                map.put("userID", userID);
                map.put("password", password);

                OkHttpHelper httpHelper = new OkHttpHelper();
                Call call = httpHelper.postRequest(url, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "连接服务器失败", Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());

                            //进行跳转操作
                            int type = jsonObj.getInt("userType");
                            String userID = jsonObj.getString("userID");
                            if (type == PLAYER) {
                                Intent intent = new Intent(LoginActivity.this,
                                        StartActivity.class);
                                intent.putExtra("userID", userID);
                                startActivity(intent);
                                finish();
                            }
                            else if (type == ADMIN) {
                                Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "用户ID或者密码出错", Toast.LENGTH_SHORT).show();
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
     * 忘记密码处理事件
     */
    private class ForgetOnClickListener implements View.OnClickListener
    {

        @Override
        public void onClick(View v) {
            String userID = etID.getText().toString().trim();
            if (userID.equals("")) {
                Toast.makeText(getApplicationContext(), "用户ID不能为空！",
                        Toast.LENGTH_SHORT).show();
            }
            else {
                Map<String, String> map = new HashMap<>();
                map.put("userID", userID);

                OkHttpHelper httpHelper = new OkHttpHelper();
                Call call = httpHelper.postRequest(UrlResources.FORGET, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "服务器连接失败！",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        try {
                            JSONObject jsonObj = new JSONObject(response.body().string());
                            String result = jsonObj.getString("result");

                            if (result.equals("false")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "不存在该用户！", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            else {
                                String userID = jsonObj.getString("userID");
                                String question = jsonObj.getString("question");
                                String answer = jsonObj.getString("answer");

                                Intent intent = new Intent(LoginActivity.this,
                                        ForgetActivity.class);
                                intent.putExtra("userID", userID);
                                intent.putExtra("question", question);
                                intent.putExtra("answer", answer);
                                startActivity(intent);
                                finish();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
