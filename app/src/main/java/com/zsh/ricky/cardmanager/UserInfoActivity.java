package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.model.Player;
import com.zsh.ricky.cardmanager.model.User;
import com.zsh.ricky.cardmanager.util.ModelUri;
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

public class UserInfoActivity extends AppCompatActivity {

    //控件部分
    private EditText etUserName, etPassword;
    private EditText etQuestion, etAnswer;
    private TextView tvDatetime;
    private Button btChange, btBack;

    //信息部分
    private Player player;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        initWidget();
        initEvent();
        initInfo();
    }

    /**
     * 获取控件
     */
    private void initWidget() {

        Intent intent = getIntent();
        userID = intent.getStringExtra(ModelUri.USER_ID);

        etUserName = (EditText) this.findViewById(R.id.cupInputName);
        etPassword = (EditText) this.findViewById(R.id.cupInputPassword);
        etQuestion = (EditText) this.findViewById(R.id.cupEditQuestion);
        etAnswer = (EditText) this.findViewById(R.id.cupEditPassquestionAnswer);
        tvDatetime = (TextView) this.findViewById(R.id.cupShowRegisterTime);
        btChange = (Button) this.findViewById(R.id.cupChange);
        btBack = (Button) this.findViewById(R.id.cupReturn);
    }

    private void initInfo() {
        Map<String, String> map = new HashMap<>();
        map.put("userID", userID);

        OkHttpHelper helper = new OkHttpHelper();
        Call call = helper.postRequest(UrlResources.GET_USER_PROPERTY, map);
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
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObj = new JSONObject(response.body().string());

                    player = new Player();
                    player.setUserID(userID);
                    player.setUserName(jsonObj.getString(ModelUri.USER_NAME));
                    player.setPassword(jsonObj.getString(ModelUri.PASSWORD));
                    player.setQuestion(jsonObj.getString(ModelUri.QUESTION));
                    player.setAnswer(jsonObj.getString(ModelUri.ANSWER));
                    player.setDate(jsonObj.getString(ModelUri.DATE));
                    player.setTime(jsonObj.getString(ModelUri.TIME));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            etUserName.setText(player.getUserName());
                            etPassword.setText(player.getPassword());
                            etQuestion.setText(player.getQuestion());
                            etAnswer.setText(player.getAnswer());
                            tvDatetime.setText(player.getDateTime());
                        }
                    });

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initEvent() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserInfoActivity.this, StartActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                startActivity(intent);
                finish();
            }
        });
        btChange.setOnClickListener(new ChangeClick());
    }

    private class ChangeClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (validate()) {
                String username = etUserName.getText().toString();
                String password = etPassword.getText().toString();
                String question = etQuestion.getText().toString();
                String answer = etAnswer.getText().toString();

                Map<String, String> map = new HashMap<String, String>();
                map.put(ModelUri.USER_ID, player.getUserID());
                map.put(ModelUri.USER_NAME, username);
                map.put(ModelUri.PASSWORD, password);
                map.put(ModelUri.QUESTION, question);
                map.put(ModelUri.ANSWER, answer);

                OkHttpHelper helper = new OkHttpHelper();
                Call call = helper.postRequest(UrlResources.UPDATE_USER, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "服务器响应失败!",
                                        Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "修改成功",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "修改失败",
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
     * 验证用户输入信息
     * @return 信息为空或者信息未改变返回false, 否则返回true
     */
    private boolean validate () {
        String username = etUserName.getText().toString().trim();
        if (username.equals("")) {
            Toast.makeText(getApplicationContext(), "用户名不能为空！",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        String password = etPassword.getText().toString().trim();
        if (password.equals("")) {
            Toast.makeText(getApplicationContext(), "密码不能为空！",
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
            Toast.makeText(getApplicationContext(), "密保答案不能为空",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (player.compareInfo(username, answer, question, password)) {
            Toast.makeText(getApplicationContext(), "未更改信息",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
