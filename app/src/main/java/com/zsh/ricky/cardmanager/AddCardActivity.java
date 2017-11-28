package com.zsh.ricky.cardmanager;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.content.*;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import android.net.Uri;

import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.ModelUri;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.UrlResources;
import com.zsh.ricky.cardmanager.util.PublicFuntion;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AddCardActivity extends AppCompatActivity {
    private Button      bt_add;
    private ImageButton ib_back;
    private EditText    cards_name;
    private ImageView   cards_pic_name;
    private EditText    cards_hp;
    private EditText    cards_attack;
    private Spinner     cards_type;
    private DBAdapter   dbAdapter;
    private ContentValues cValues;
    private String      card_pic_path;
    private PublicFuntion pf=new PublicFuntion();
    Bitmap bitmap_pic;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_card);

        initWeightItems();
        initEvent();
    }

    /**
     * 初始化按钮事件
     */
    private void initEvent() {
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
        dbAdapter=new DBAdapter(this,DBAdapter.DB_NAME,null,1);
        bt_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (isEmpty()) {
                        Toast.makeText(getApplicationContext(), "卡牌信息不完整！",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //添加卡牌基本信息到cValue中
                    initcValues();
                    //卡牌基本信息上传到服务器，成功，则继续上传图片
                    Map<String,String> card_info=ContentValuesToMap(cValues);
                    OkHttpHelper httpHelper = new OkHttpHelper();
                    Call call = httpHelper.postRequest(UrlResources.NEW_CARD_INFO, card_info);
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
                            JSONObject jsonObject = null;
                            boolean flag = false;
                            try {
                                jsonObject = new JSONObject(
                                        response.body().string());
                                flag = jsonObject.getBoolean(ModelUri.RESULT);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (flag) {
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
                                                    db.insert(DBAdapter.TABLE_NAME, null, cValues);
                                                    pf.copyFile(AddCardActivity.this, card_pic_path, OkHttpHelper.BITMAP_SAVE_PATH + cValues.get(DBAdapter.COL_PIC_NAME));
                                                    cValues.clear();
                                                    Intent intent = new Intent(AddCardActivity.this, AdminActivity.class);
                                                    startActivity(intent);
                                                    finish();

                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "添加数据失败！请检查你要添加的信息是否有误",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    });
                }
                catch (Exception e){
                    Toast.makeText(getApplicationContext(),e.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //返回按钮事件 ———— 返回上一级
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddCardActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void initWeightItems() {
        bt_add=(Button)this.findViewById(R.id.ac_btAdd);
        cards_name=(EditText)this.findViewById(R.id.ac_etCardName);
        cards_pic_name=(ImageView)this.findViewById(R.id.ac_image);
        cards_hp=(EditText)this.findViewById(R.id.ac_etCardHp);
        cards_attack=(EditText)this.findViewById(R.id.ac_etCardAttack);
        cards_type=(Spinner)this.findViewById(R.id.ac_spCardType);
        ib_back = (ImageButton) this.findViewById(R.id.ac_ibBack);
    }
    //显示图片
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());

            PublicFuntion pf=new PublicFuntion();
            card_pic_path=pf.getRealPathFromUri(this,uri);
            //isImageChange=true;
            ContentResolver cr = this.getContentResolver();
            try {
                bitmap_pic = BitmapFactory.decodeStream(cr.openInputStream(uri));

                //ImageView imageView = (ImageView) findViewById(R.id.ac_image);
                /* 将Bitmap设定到ImageView */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         /* 将Bitmap设定到ImageView */
                        cards_pic_name.setImageBitmap(bitmap_pic);
                    }
                });
            } catch (Exception e) {
                Log.e("Exception", e.getMessage(),e);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    //ContentValues 转成 Map<String,String>
    protected  Map<String,String> ContentValuesToMap(ContentValues cv){
        Map<String,String> temp=new HashMap<>();
        for (String key:cv.keySet()){
            temp.put(key,cv.get(key).toString());
        }
        return temp;
    }
    //初始化cValue信息为卡牌基本信息
    protected  void initcValues(){
        cValues=new ContentValues();
        //"CardID"由数据库自行给出
        cValues.put(DBAdapter.COL_NAME,    cards_name.getText().toString());
        String[] picName=card_pic_path.split("/");
        cValues.put(DBAdapter.COL_PIC_NAME,picName[picName.length-1]);
        cValues.put(DBAdapter.COL_HP,      cards_hp.getText().toString());
        cValues.put(DBAdapter.COL_ATTACK,  cards_attack.getText().toString());
        cValues.put(DBAdapter.COL_TYPE,cards_type.getSelectedItemPosition());
    }
    private boolean isEmpty(){
        if (cards_pic_name.getDrawable() == null || isEmpty(cards_attack.getText().toString()) || isEmpty(cards_hp.getText().toString()) || isEmpty(cards_name.getText().toString()) || isEmpty(cards_type.getSelectedItem().toString()))
            return true;
        return false;
    }
    private boolean isEmpty(String str){
        if (str==null)
            return true;
        return str.length()==0;
    }
}
