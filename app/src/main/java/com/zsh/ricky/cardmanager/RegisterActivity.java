package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText etID, etPassword, etName;
    private EditText etQuestion, etAnswer;
    private Button yesButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initItem();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        yesButton.setOnClickListener(new YesOnClickListener());
    }

    /**
     * 初始化所有控件
     */
    private void initItem() {
        etID = (EditText) this.findViewById(R.id.rg_etUserID);
        etPassword = (EditText) this.findViewById(R.id.rg_etPassword);
        etName = (EditText) this.findViewById(R.id.rg_etUsername);
        etQuestion = (EditText) this.findViewById(R.id.rg_etQuestion);
        etAnswer = (EditText) this.findViewById(R.id.rg_etAnswer);
        yesButton = (Button) this.findViewById(R.id.rg_YesButton);
        cancelButton = (Button) this.findViewById(R.id.rg_ButtonCancel);
    }

    /**
     * 确认注册按钮触发事件
     */
    private class YesOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if (validate()) {
                String userID = etID.getText().toString();
                String password = etPassword.getText().toString();
                String username = etName.getText().toString().trim();
                String question = etQuestion.getText().toString().trim();
                String answer = etAnswer.getText().toString().trim();

                Map<String, String> map = new HashMap<>();
                map.put("userID", userID);
                map.put("password", password);
                map.put("username", username);
                map.put("question", question);
                map.put("answer", answer);

                OkHttpHelper httpHelper = new OkHttpHelper();
                Call call = httpHelper.postRequest(UrlResources.REGISTER, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),
                                        "服务器响应失败！", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "注册成功",
                                                Toast.LENGTH_SHORT).show();

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                Intent intent = new Intent(RegisterActivity.this,
                                                        LoginActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }, 1000);
                                    }
                                });


                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),
                                                "注册失败!", Toast.LENGTH_SHORT).show();
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
     * @return 都不为空返回true,否则返回false
     */
    private boolean validate() {
        String userID = etID.getText().toString().trim();
        if (userID.equals("")) {
            Toast.makeText(getApplicationContext(), "用户ID不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String password = etPassword.getText().toString().trim();
        if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "用户密码不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String username = etName.getText().toString().trim();
        if (username.equals("")) {
            Toast.makeText(getApplicationContext(), "用户密码不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String question = etQuestion.getText().toString().trim();
        if (question.equals("")) {
            Toast.makeText(getApplicationContext(), "密保问题不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String answer = etAnswer.getText().toString().trim();
        if (answer.equals("")) {
            Toast.makeText(getApplicationContext(), "密保答案不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
