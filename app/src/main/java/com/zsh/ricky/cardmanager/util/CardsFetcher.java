package com.zsh.ricky.cardmanager.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zsh.ricky.cardmanager.model.Card;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 2017/11/4.
 */

public class CardsFetcher {

    private String pic_download_file_name;
    private Context ct;     //上下文
    private DBAdapter dbAdapter;
    private List<Card> cardList;

    /**
     * 获取所有卡牌信息，包括位图
     * @param context 上下文
     * @return 卡牌信息列表
     */
    public List<Card> getCardList(Context context) {
        this.ct = context;
        dbAdapter = new DBAdapter(ct, DBAdapter.DB_NAME, null, 1);
        SQLiteDatabase db = dbAdapter.getReadableDatabase();
        Cursor dataSet = db.query(DBAdapter.TABLE_NAME, null, null, null,
                null, null, null);
        cardList = new ArrayList<>();

        try {
            for (dataSet.moveToFirst(); !dataSet.isAfterLast(); dataSet.moveToNext()) {
                Card card = newCard(
                        dataSet.getInt(dataSet.getColumnIndex(DBAdapter.COL_ID)),
                        dataSet.getString(dataSet.getColumnIndex(DBAdapter.COL_NAME)),
                        dataSet.getString(dataSet.getColumnIndex(DBAdapter.COL_PIC_NAME)),
                        dataSet.getInt(dataSet.getColumnIndex(DBAdapter.COL_HP)),
                        dataSet.getInt(dataSet.getColumnIndex(DBAdapter.COL_ATTACK)),
                        dataSet.getInt(dataSet.getColumnIndex(DBAdapter.COL_TYPE))
                );

                cardList.add(card);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return cardList;
    }

    /**
     * 获取卡牌对象
     * @param id 卡牌ID
     * @param name 卡牌名字
     * @param pic_name 图片名字
     * @param hp 卡牌血量
     * @param attack 卡牌攻击力
     * @param type 卡牌类型
     * @return 获取失败返回null,否则返回正确card
     */
    private Card newCard(int id, String name, String pic_name, int hp, int attack, int type)
    throws InterruptedException{

        Card card = new Card(id, name, pic_name, hp, attack, type);

        File pic = new File(OkHttpHelper.BITMAP_SAVE_PATH + pic_name);
        if (!pic.exists()) {//卡牌图片缺失
            pic_download_file_name = pic_name;
            Thread download;
            download = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpHelper helper = new OkHttpHelper();
                        Bitmap bitmap = helper.getPic(pic_download_file_name);   //下载
                        helper.onSaveBitmap(bitmap, ct, pic_download_file_name);  //保存到本地
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            download.start();
            download.join();
        }

        card.setCardPhoto(BitmapFactory.decodeFile(OkHttpHelper.BITMAP_SAVE_PATH + pic_name));

        return card;
    }
}
