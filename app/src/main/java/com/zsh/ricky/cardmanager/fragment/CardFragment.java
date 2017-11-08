package com.zsh.ricky.cardmanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.CardInfoActivity;
import com.zsh.ricky.cardmanager.R;
import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.ImageSimpleAdapter;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ricky on 2017/11/4.
 */

public class CardFragment extends Fragment{
    private View view;
    private ListView gv_info;
    private List<Map<String,Object>> data;
    private List<String> cards_id_list;
    private List<String> cards_pic_name_list;
    private ImageSimpleAdapter sim_adapter;
    private String[] from={DBAdapter.COL_PIC_NAME,DBAdapter.COL_NAME,DBAdapter.COL_ATTACK,DBAdapter.COL_HP,DBAdapter.COL_TYPE};
    private int[] to={R.id.card_item_iv_pic,R.id.card_item_tv_name,R.id.card_item_tv_attack,R.id.card_item_tv_hp,R.id.card_item_tv_type};
    private Context context;
    private DBAdapter dbAdapter;

    public CardFragment(){}
    @SuppressLint("ValidFragment")
    public CardFragment(Context ct){
        context=ct;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_card_fragment,null);

        dbAdapter=new DBAdapter(context,DBAdapter.DB_NAME,null,1);
        initWeight();
        initData();
        sim_adapter = new ImageSimpleAdapter(context, data, R.layout.card_item, from, to);
        //配置适配器
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gv_info.setAdapter(sim_adapter);
                sim_adapter.notifyDataSetChanged();
            }
        });
        gv_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent =new Intent(context,CardInfoActivity.class);
                //用Bundle携带数据
                Bundle bundle=new Bundle();
                //传递当前项的卡牌信息
                Map<String,Object> send_arg=data.get(position);
                bundle.putString(DBAdapter.COL_ID,cards_id_list.get(position));
                bundle.putString(DBAdapter.COL_NAME, send_arg.get(DBAdapter.COL_NAME).toString());
                bundle.putString(DBAdapter.COL_PIC_NAME, cards_pic_name_list.get(position));
                bundle.putString(DBAdapter.COL_ATTACK, send_arg.get(DBAdapter.COL_ATTACK).toString());
                bundle.putString(DBAdapter.COL_HP, send_arg.get(DBAdapter.COL_HP).toString());
                bundle.putString(DBAdapter.COL_TYPE, send_arg.get(DBAdapter.COL_TYPE).toString());
                intent.putExtras(bundle);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    protected void initWeight() {
        gv_info=(ListView) view.findViewById(R.id.cards_info_gv_info);
    }
    private void initData(){
        //数据库中加载卡牌信息，将id信息存入cards_id_list，其余信息转入 data,其中data中的 CardPhotoName的Object存对应的Bitmap信息
        data=new ArrayList<Map<String, Object>>();
        cards_id_list=new ArrayList<String>();
        cards_pic_name_list=new ArrayList<String>();
        SQLiteDatabase db=dbAdapter.getReadableDatabase();
        Cursor dataset=db.query(DBAdapter.TABLE_NAME,null,null,null,null,null,null);
        for (dataset.moveToFirst();dataset.isAfterLast()==false;dataset.moveToNext()){
            Map<String, Object> item=new HashMap<String,Object>();
            cards_id_list.add(dataset.getString(dataset.getColumnIndex(DBAdapter.COL_ID)));

            cards_pic_name_list.add(dataset.getString(dataset.getColumnIndex(DBAdapter.COL_PIC_NAME)));
            //String picName=cards_pic_name_list.get(cards_pic_name_list.size()-1);
            Bitmap pic= BitmapFactory.decodeFile(OkHttpHelper.BITMAP_SAVE_PATH+cards_pic_name_list.get(cards_pic_name_list.size()-1));
            item.put(DBAdapter.COL_PIC_NAME,pic);

            item.put(DBAdapter.COL_NAME,dataset.getString(dataset.getColumnIndex(DBAdapter.COL_NAME)));
            item.put(DBAdapter.COL_ATTACK,dataset.getString(dataset.getColumnIndex(DBAdapter.COL_ATTACK)));
            item.put(DBAdapter.COL_HP,dataset.getString(dataset.getColumnIndex(DBAdapter.COL_HP)));
            item.put(DBAdapter.COL_TYPE,dataset.getString(dataset.getColumnIndex(DBAdapter.COL_TYPE)));
            data.add(item);
        }
    }
}
