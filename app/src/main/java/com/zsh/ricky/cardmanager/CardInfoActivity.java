package com.zsh.ricky.cardmanager;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.PublicFuntion;
import com.zsh.ricky.cardmanager.util.UrlResources;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Thinker on 2017/11/5.
 */

public class CardInfoActivity extends AppCompatActivity {
    private Button bt_update;
    private Button bt_delete;
    private ImageButton ib_back;
    private EditText cards_name;
    private ImageView cards_pic_name;
    private EditText    cards_hp;
    private EditText    cards_attack;
    private Spinner cards_type;
    private DBAdapter dbAdapter;
    private ContentValues cValues;
    private String      card_pic_path;
    private PublicFuntion pf=new PublicFuntion();
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardinfo);

        ActivityCompat.requestPermissions(CardInfoActivity.this, new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        initWeightItems();

        //ImageView点击事件，获取一个图片，并为 card_pic_path 赋值
        cards_pic_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*");
                startActivityForResult(intent, 0x1);
            }
        });
        //添加按钮事件 ———— 上传卡牌信息到服务器，添加信息到本地数据库
        dbAdapter = new DBAdapter(this, DBAdapter.DB_NAME, null, 1);
        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (cards_pic_name.getDrawable() == null || cards_attack.getText().toString() == null || cards_hp.getText().toString() == null || cards_name.getText().toString() == null || cards_type.getSelectedItem().toString() == null) {
                        Toast.makeText(getApplicationContext(), "卡牌信息不完整！",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //添加卡牌基本信息到cValue中
                    initcValues();
                    //卡牌基本信息上传到服务器，成功，则继续上传图片
                    Map<String, String> card_info = pf.ContentValuesToMap(cValues);
                    OkHttpHelper httpHelper = new OkHttpHelper();
                    Call call = httpHelper.postRequest(UrlResources.UPDATE_CARD_INFO, card_info);
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
                            //图片上传到服务器
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        OkHttpHelper post_pic = new OkHttpHelper();
                                        Call pic_call = post_pic.imageUpLoad(UrlResources.UPDATE_CARD_PIC, card_pic_path);
                                        pic_call.enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Toast.makeText(getApplicationContext(), "图片上传失败",
                                                        Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                //添加到本地数据库中
                                                SQLiteDatabase db = dbAdapter.getWritableDatabase();
                                                Cursor dataset = db.query(DBAdapter.TABLE_NAME, null, null, null, null, null, null);
                                                //db.update(DBAdapter.TABLE_NAME, null, cValues);
                                                cValues.clear();
                                                //保存图片到本地目录
                                                pf.copyFile(card_pic_path,OkHttpHelper.BITMAP_SAVE_PATH);
                                            }
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //返回按钮事件 ———— 返回上一级
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        //删除按钮事件 ———— 删除当前卡牌信息
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
    public void initWeightItems() {
        bt_update=(Button)this.findViewById(R.id.cardinfo_bt_update);
        cards_name=(EditText)this.findViewById(R.id.cardinfo_name);
        cards_pic_name=(ImageView)this.findViewById(R.id.cardinfo_image);
        cards_hp=(EditText)this.findViewById(R.id.cardinfo_hp);
        cards_attack=(EditText)this.findViewById(R.id.cardinfo_attack);
        cards_type=(Spinner)this.findViewById(R.id.cardinfo_type);
        bt_delete=(Button)this.findViewById(R.id.cardinfo_bt_delete);
        ib_back = (ImageButton) this.findViewById(R.id.cardInfo_backImageButton);
    }
    //显示图片
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());

            PublicFuntion pf=new PublicFuntion();
            card_pic_path=pf.getRealPathFromUri(this,uri);

            ContentResolver cr = this.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

                ImageView imageView = (ImageView) findViewById(R.id.ac_image);
                /* 将Bitmap设定到ImageView */
                imageView.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //初始化cValue信息为卡牌基本信息
    protected  void initcValues(){
        cValues=new ContentValues();
        //"CardID"由数据库自行给出
        cValues.put(DBAdapter.COL_NAME,    cards_name.getText().toString());
        //卡牌图片名称与卡牌同名
        cValues.put(DBAdapter.COL_PIC_NAME,cards_name.getText().toString());
        cValues.put(DBAdapter.COL_HP,      cards_hp.getText().toString());
        cValues.put(DBAdapter.COL_ATTACK,  cards_attack.getText().toString());
        cValues.put(DBAdapter.COL_TYPE,    cards_type.getSelectedItem().toString());
    }
}
