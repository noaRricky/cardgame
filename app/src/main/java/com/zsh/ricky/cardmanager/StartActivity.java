package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.graphics.ColorSpace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.model.User;
import com.zsh.ricky.cardmanager.util.ModelUri;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.UrlResources;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class StartActivity extends AppCompatActivity {

    private Button btExit, btGame, btUserInfo;

    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initWidget();
        initEvent();
    }

    private void initWidget() {

        Intent intent = getIntent();
        userID = intent.getStringExtra(ModelUri.USER_ID);

        btExit = (Button) this.findViewById(R.id.st_btExit);
        btGame = (Button) this.findViewById(R.id.st_btGame);
        btUserInfo = (Button) this.findViewById(R.id.st_btUserInfo);
    }

    private void initEvent() {
        btExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, String> map = new HashMap<>();
                map.put(ModelUri.ACTION, ModelUri.EXIT);
                map.put(ModelUri.USER_ID, userID);

                OkHttpHelper helper = new OkHttpHelper();
                Call call = helper.postRequest(UrlResources.LOGIN, map);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplication(), "无法连接服务器",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplication(), "退出",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                Intent intent = new Intent(StartActivity.this, LoginActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                startActivity(intent);
                finish();
            }
        });

        btGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, DeckActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                startActivity(intent);
                finish();
            }
        });

        btUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, UserInfoActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                startActivity(intent);
                finish();
            }
        });
    }
}
