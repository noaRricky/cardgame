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

import java.io.File;
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
    private Button      bt_update;
    private ImageButton bt_back;
    private Button      bt_delete;
    private EditText    cards_name;
    private ImageView   cards_pic;
    private EditText    cards_hp;
    private EditText    cards_attack;
    private Spinner     cards_type;
    private DBAdapter   dbAdapter;
    private ContentValues cValues;
    private String      card_pic_path;
    private PublicFuntion pf=new PublicFuntion();
    private Map<String,String> rev_arg=new HashMap<>();
    private boolean isImageChange=false;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardinfo);
        //接收卡牌信息参数
        Bundle bundle = this.getIntent().getExtras();
        rev_arg.put(DBAdapter.COL_ID,bundle.getString(DBAdapter.COL_ID));
        rev_arg.put(DBAdapter.COL_NAME,bundle.getString(DBAdapter.COL_NAME));
        rev_arg.put(DBAdapter.COL_PIC_NAME,bundle.getString(DBAdapter.COL_PIC_NAME));
        rev_arg.put(DBAdapter.COL_ATTACK,bundle.getString(DBAdapter.COL_ATTACK));
        rev_arg.put(DBAdapter.COL_HP,bundle.getString(DBAdapter.COL_HP));
        rev_arg.put(DBAdapter.COL_TYPE,bundle.getString(DBAdapter.COL_TYPE));

        ActivityCompat.requestPermissions(CardInfoActivity.this, new String[]{
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        initWeightItems();

        //ImageView点击事件，获取一个图片，并为 card_pic_path 赋值
        cards_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        "image/*");
                startActivityForResult(intent, 0x1);
            }
        });
        //修改按钮事件 ———— 上传卡牌信息到服务器，更新信息到本地数据库
        dbAdapter = new DBAdapter(this, DBAdapter.DB_NAME, null, 1);
        bt_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (cards_pic.getDrawable() == null || cards_attack.getText().toString() == null || cards_hp.getText().toString() == null || cards_name.getText().toString() == null || cards_type.getSelectedItem().toString() == null) {
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
                            if (isImageChange){
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
                                                    //保存图片到本地目录
                                                    pf.copyFile(card_pic_path,OkHttpHelper.BITMAP_SAVE_PATH);
                                                    Toast.makeText(getApplicationContext(), "更新成功！",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }//更新到本地数据库中
                            SQLiteDatabase db = dbAdapter.getWritableDatabase();
                            Cursor dataset = db.query(DBAdapter.TABLE_NAME, null, null, null, null, null, null);
                            String[] args={rev_arg.get(DBAdapter.COL_ID)};
                            db.update(DBAdapter.TABLE_NAME, cValues,DBAdapter.COL_ID+"=?",args);
                            cValues.clear();

                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        //删除卡牌信息
        bt_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    OkHttpHelper httpHelper = new OkHttpHelper();
                    Map<String, String> card_info = new HashMap<String, String>();
                    card_info.put(DBAdapter.COL_ID,rev_arg.get(DBAdapter.COL_ID));
                    Call call = httpHelper.postRequest(UrlResources.DELETE_CARD, card_info);
                    call.enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),
                                            "服务器删除卡牌失败！", Toast.LENGTH_SHORT).show();
                                }
                            });
                            e.printStackTrace();
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            //删除本地卡牌信息，并跳转到上一级页面
                            SQLiteDatabase db = dbAdapter.getWritableDatabase();
                            //Cursor dataset = db.query(DBAdapter.TABLE_NAME, null, null, null, null, null, null);
                            db.delete(DBAdapter.TABLE_NAME,DBAdapter.COL_ID+"=?",new String[]{rev_arg.get(DBAdapter.COL_ID)});
                            File pic=new File(OkHttpHelper.BITMAP_SAVE_PATH+rev_arg.get(DBAdapter.COL_PIC_NAME));
                            pic.delete();
                            Toast.makeText(getApplicationContext(), "删除成功！",
                                    Toast.LENGTH_SHORT).show();
                            //跳转到上一级页面
                            Intent intent =new Intent(CardInfoActivity.this,AdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //返回按钮事件 ———— 返回上一级
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardInfoActivity.this, AdminActivity.class);
                startActivity(intent);
            }
        });
    }
    public void initWeightItems() {
        bt_update=(Button)this.findViewById(R.id.cardinfo_bt_update);
        bt_delete=(Button)this.findViewById(R.id.cardinfo_bt_delete);
        bt_back=(ImageButton)this.findViewById(R.id.cardInfo_backImageButton);
        cards_name=(EditText)this.findViewById(R.id.cardinfo_name);
        cards_pic=(ImageView)this.findViewById(R.id.cardinfo_image);
        cards_hp=(EditText)this.findViewById(R.id.cardinfo_hp);
        cards_attack=(EditText)this.findViewById(R.id.cardinfo_attack);
        cards_type=(Spinner)this.findViewById(R.id.cardinfo_type);

        //添加初始信息
        cards_name.setText(rev_arg.get(DBAdapter.COL_NAME));
        Bitmap pic=BitmapFactory.decodeFile(OkHttpHelper.BITMAP_SAVE_PATH+rev_arg.get(DBAdapter.COL_PIC_NAME));
        cards_pic.setImageBitmap(pic);
        cards_attack.setText(rev_arg.get(DBAdapter.COL_ATTACK));
        cards_hp.setText(rev_arg.get(DBAdapter.COL_HP));
        int position=0;
        if (rev_arg.get(DBAdapter.COL_TYPE)!="肉食动物"){position=1;}
        cards_type.setSelection(position);
    }
    //显示图片
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            Log.e("uri", uri.toString());

            PublicFuntion pf=new PublicFuntion();
            card_pic_path=pf.getRealPathFromUri(this,uri);
            isImageChange=true;
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
        if (isImageChange==true){
            String[] picName=card_pic_path.split("\\");
            cValues.put(DBAdapter.COL_PIC_NAME,picName[picName.length-1]);
        }
        else {
            cValues.put(DBAdapter.COL_PIC_NAME,rev_arg.get(DBAdapter.COL_PIC_NAME));
        }
        cValues.put(DBAdapter.COL_HP,      cards_hp.getText().toString());
        cValues.put(DBAdapter.COL_ATTACK,  cards_attack.getText().toString());
        cValues.put(DBAdapter.COL_TYPE,    cards_type.getSelectedItem().toString());
    }
}
