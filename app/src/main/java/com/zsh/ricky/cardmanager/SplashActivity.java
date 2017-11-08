package com.zsh.ricky.cardmanager;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.AdminActivity;
import com.zsh.ricky.cardmanager.R;
import com.zsh.ricky.cardmanager.model.GameHistory;
import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.UrlResources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class SplashActivity extends AppCompatActivity {

    private LinearLayout splash_layout = null;
    private TextView version_view = null;
    private DBAdapter dbAdapter;
    private ContentValues cValues=new ContentValues();
    private String pic_list=new String();
    private List<String> pic_download_list=new ArrayList<>();

    private static final String self_permissions[] = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int i = ContextCompat.checkSelfPermission(this, self_permissions[0]);
            if (i != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(SplashActivity.this,
                        self_permissions, 1);
            }
        }

        dbAdapter = new DBAdapter(this, DBAdapter.DB_NAME, null, 1);
        splash_layout = (LinearLayout) this.findViewById(R.id.SplashLayout);
        version_view = (TextView) this.findViewById(R.id.version);
        version_view.setText(getAppVersion());

        //检查网络状态是否可用
        if (isNetworkConnected())
        {
            //更新卡牌
            Thread update;
            update = new Thread(new Runnable() {
                @Override
                public void run() {
                    updateCards();
                }
            });
            update.start();
            try {
                update.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            //splash做一个动画后进入登录界面
            AlphaAnimation aa = new AlphaAnimation(0.5f, 1.0f);
            aa.setDuration(2000);
            splash_layout.setAnimation(aa);
            splash_layout.startAnimation(aa);
            //通过handler延迟2秒
            new Handler().postDelayed(new LoadLoginTask(), 2000);
        }
        else
        {
            showNetworkDialog();
        }

        Intent intent = getIntent();
    }

    public void updateCards(){//get卡牌id列表，对比本地信息生成需下载的卡牌列表，开辟新线程逐个下载到本地SD卡中
        final OkHttpHelper helper=new OkHttpHelper();
        Call get_cards_count=helper.getRequest(UrlResources.CARDS_LIST);
        get_cards_count.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "与服务器连接失败",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    //获取服务器卡牌信息
                    JSONArray cards = new JSONArray(response.body().string());
                    //写入本地卡牌信息
                    SQLiteDatabase db= dbAdapter.getWritableDatabase();
                    db.execSQL("DELETE FROM "+DBAdapter.TABLE_NAME);//删除本地卡牌信息
                    for (int i=0;i<cards.length();i++){
                        JSONObject row=(JSONObject) cards.get(i);
                        cValues.put(DBAdapter.COL_ID,row.getString(DBAdapter.COL_ID));
                        cValues.put(DBAdapter.COL_NAME,row.getString(DBAdapter.COL_NAME));
                        cValues.put(DBAdapter.COL_PIC_NAME,row.getString(DBAdapter.COL_PIC_NAME));
                        if (!isSave(helper.BITMAP_SAVE_PATH,cValues.get(DBAdapter.COL_PIC_NAME).toString())){
                            //图片未保存，加入下载列表
                            pic_download_list.add(cValues.get(DBAdapter.COL_PIC_NAME).toString());
                        }
                        cValues.put(DBAdapter.COL_HP,row.getString(DBAdapter.COL_HP));
                        cValues.put(DBAdapter.COL_ATTACK,row.getString(DBAdapter.COL_ATTACK));
                        cValues.put(DBAdapter.COL_TYPE,row.getString(DBAdapter.COL_TYPE));
                        db.insert(DBAdapter.TABLE_NAME,null,cValues);
                        cValues.clear();
                    }

                    //下载图片
                    try {
                        Thread download;
                        download= new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    for (int it=0;it<pic_download_list.size();it++) {
                                        String name=pic_download_list.get(it).toString();
                                        Bitmap bitmap = helper.getPic(pic_download_list.get(it).toString());//下载
                                        helper.onSaveBitmap(bitmap, SplashActivity.this, pic_download_list.get(it).toString());//保存到本地
                                    }
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.toString(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        download.start();
                        download.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public boolean isSave(String path,String fileName){
        File appDir = new File(path);
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File f=new File(path,fileName);
        return f.exists();
    }

    private class LoadLoginTask implements Runnable
    {
        @Override
        public void run() {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

            startActivity(intent);
            finish();
        }
    }

    /**
     * 获取当前app的版本号
     * @return 获取成功返回版本号，否则返回空
     */
    private String getAppVersion()
    {
        String version;

        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = "Version " + info.versionName;

        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
            version = "Version";
        }

        return version;
    }

    /**
     * 判断当前网络是否可用
     * @return 可用则返回true,否则返回false
     */
    private boolean isNetworkConnected()
    {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();

        //WifiManager  wifimanager =  (WifiManager) getSystemService(WIFI_SERVICE);
        //wifimanager.isWifiEnabled();
        //wifimanager.getWifiState();

        return (info != null && info.isConnected());
    }

    /**
     * 当网络检测出问题时出现该对话框
     */
    private void showNetworkDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置网络");
        builder.setMessage("网络错误请检查网络状态");

        //设置手机中网络连接
        builder.setPositiveButton("设置网络", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                //类名一定包含包名
                intent.setClassName("com.android.settings", "com.android.settings.WirelessSettings");

                startActivity(intent);
                finish();
            }
        });
        //结束游戏
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();
    }
}
