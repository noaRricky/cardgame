package com.zsh.ricky.cardmanager.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ricky on 2017/11/4.
 */

public class CardFragment extends Fragment{
    private View view;
    private GridView gv_info;
    private List<Map<String,Object>> data;
    private SimpleAdapter sim_adapter;
    private String[] from={"image","name","attack","hp","type"};
    private int[] to={R.id.card_item_iv_pic,R.id.card_item_tv_name,R.id.card_item_tv_attack,R.id.card_item_tv_hp,R.id.card_item_tv_type};
    Context context;
    public CardFragment(){}
    @SuppressLint("ValidFragment")
    public CardFragment(Context ct){
        context=ct;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_cardfragment,null);
        initWeight();
        initData();
        sim_adapter = new SimpleAdapter(context, data, R.layout.card_item, from, to);
        //配置适配器
        gv_info.setAdapter(sim_adapter);

        gv_info.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             //Toast.makeText(getView().getApplicationContext(),data.get(position).get("name").toString()+id,Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
    protected void initWeight() {
        gv_info=(GridView) view.findViewById(R.id.cards_info_gv_info);
    }
    private void initData(){
        data=new ArrayList<Map<String, Object>>();
        Map<String, Object> item=new HashMap<String,Object>();
        item.put("image",R.drawable.ac_02);
        item.put("name","小狗");
        item.put("attack",5);
        item.put("hp",100);
        item.put("type","肉食");
        data.add(item);
        item=new HashMap<String,Object>();
        item.put("image",R.drawable.ac_02);
        item.put("name","老虎");
        item.put("attack",10);
        item.put("hp",150);
        item.put("type","肉食");
        data.add(item);
    }
}
