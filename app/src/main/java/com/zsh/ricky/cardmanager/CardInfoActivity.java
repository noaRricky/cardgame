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
import android.os.Handler;
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
import com.zsh.ricky.cardmanager.util.ModelUri;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.PublicFuntion;
import com.zsh.ricky.cardmanager.util.UrlResources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
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
    private Bitmap bitmap_pic;
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

        initWeightItems();
        initEvent();

        //修改按钮事件 ———— 上传卡牌信息到服务器，更新信息到本地数据库
        dbAdapter = new DBAdapter(this, DBAdapter.DB_NAME, null, 1);

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
        //if (rev_arg.get(DBAdapter.COL_TYPE)!="1"){position=1;}
        cards_type.setSelection(Integer.valueOf(rev_arg.get(DBAdapter.COL_TYPE)));
    }

    private void initEvent() {

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

        bt_update.setOnClickListener(new UpdateCardClick());
        //删除卡牌信息
        bt_delete.setOnClickListener(new DeleteCardClick());
        //返回按钮事件 ———— 返回上一级
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CardInfoActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
                bitmap_pic = BitmapFactory.decodeStream(cr.openInputStream(uri));

                //ImageView imageView = (ImageView) findViewById(R.id.ac_image);
                /* 将Bitmap设定到ImageView */
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         /* 将Bitmap设定到ImageView */
                        cards_pic.setImageBitmap(bitmap_pic);

                    }
                });

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
            String[] picName=card_pic_path.split("/");
            cValues.put(DBAdapter.COL_PIC_NAME,picName[picName.length-1]);
        }
        else {
            cValues.put(DBAdapter.COL_PIC_NAME,rev_arg.get(DBAdapter.COL_PIC_NAME));
        }
        cValues.put(DBAdapter.COL_HP,      cards_hp.getText().toString());
        cValues.put(DBAdapter.COL_ATTACK,  cards_attack.getText().toString());
        cValues.put(DBAdapter.COL_TYPE,cards_type.getSelectedItemPosition());
    }

    /**
     * 处理删除卡牌事件
     */
    private class DeleteCardClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                Map<String, String> card_info = new HashMap<String, String>();
                card_info.put(DBAdapter.COL_ID, rev_arg.get(DBAdapter.COL_ID));

                OkHttpHelper httpHelper = new OkHttpHelper();
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
                        db.delete(DBAdapter.TABLE_NAME, DBAdapter.COL_ID + "=?", new String[]{rev_arg.get(DBAdapter.COL_ID)});
                        pf.scanFileAsync(CardInfoActivity.this ,OkHttpHelper.BITMAP_SAVE_PATH + rev_arg.get(DBAdapter.COL_PIC_NAME));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "删除成功！",
                                        Toast.LENGTH_SHORT).show();
                                //跳转到上一级页面
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(CardInfoActivity.this, AdminActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                }, 1000);
                            }
                        });
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 更新卡牌信息操作
     */
    private class UpdateCardClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            try {
                if (isEmpty()) {
                    Toast.makeText(getApplicationContext(), "卡牌信息不完整！",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                //添加卡牌基本信息到cValue中
                initcValues();
                //卡牌基本信息上传到服务器，成功，则继续上传图片
                Map<String, String> card_info = pf.ContentValuesToMap(cValues);
                card_info.put(DBAdapter.COL_ID,rev_arg.get(DBAdapter.COL_ID));
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
                        //检查卡牌信息是否更新成功
                        JSONObject jsonObject = null;
                        boolean flag = false;
                        try {
                            jsonObject = new JSONObject(
                                    response.body().string());
                            flag = jsonObject.getBoolean(ModelUri.RESULT);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if(flag) {
                            //图片上传到服务器
                            if (isImageChange) {
                                OkHttpHelper helper = new OkHttpHelper();
                                Call pic_call = helper.imageUpLoad(UrlResources.UPDATE_CARD_PIC,
                                        card_pic_path);
                                pic_call.enqueue(new Callback() {
                                    @Override
                                    public void onFailure(Call call, IOException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "上传图片失败",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onResponse(Call call, Response response) throws IOException {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                pf.copyFile(CardInfoActivity.this, card_pic_path, OkHttpHelper.BITMAP_SAVE_PATH);
                                                Toast.makeText(getApplicationContext(), "更新成功！",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                });
                                //处理不更新图片操作
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "更新成功",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            //更新到本地数据库中
                            SQLiteDatabase db = dbAdapter.getWritableDatabase();
                            Cursor dataSet = db.query(DBAdapter.TABLE_NAME, null, null, null, null, null, null);
                            String[] args = {rev_arg.get(DBAdapter.COL_ID)};
                            db.update(DBAdapter.TABLE_NAME, cValues, DBAdapter.COL_ID + "=?", args);
                            cValues.clear();
                            dataSet.close();
                            Intent intent = new Intent(CardInfoActivity.this, AdminActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
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
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    private boolean isEmpty(){
        if (cards_pic.getDrawable() == null || isEmpty(cards_attack.getText().toString()) || isEmpty(cards_hp.getText().toString()) || isEmpty(cards_name.getText().toString()) || isEmpty(cards_type.getSelectedItem().toString()))
            return true;
        return false;
    }
    private boolean isEmpty(String str){
        if (str==null)
            return true;
        return str.length()==0;
    }
}
