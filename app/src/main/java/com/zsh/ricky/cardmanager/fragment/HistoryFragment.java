package com.zsh.ricky.cardmanager.fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.CardInfoActivity;
import com.zsh.ricky.cardmanager.HistoryInfoActivity;
import com.zsh.ricky.cardmanager.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.zsh.ricky.cardmanager.model.GameHistory;
import com.zsh.ricky.cardmanager.util.DBAdapter;
import com.zsh.ricky.cardmanager.util.HistoryAdapter;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;
import com.zsh.ricky.cardmanager.util.PublicFuntion;
import com.zsh.ricky.cardmanager.util.UrlResources;

/**
 * Created by Thinker on 2017/11/5.
 */

public class HistoryFragment extends Fragment {
    private ListView lvInfo;
    private Context context;
    private View view;
    List<GameHistory> data;

    public HistoryFragment(){}
    @SuppressLint("ValidFragment")
    public HistoryFragment(Context context){ this.context = context;}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_history_fragment,null);
        setupItem();
        setupList();
        lvInfo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent =new Intent(context,HistoryInfoActivity.class);
                //用Bundle携带数据
                Bundle bundle=new Bundle();
                //传递当前项的历史信息
                GameHistory send_arg=data.get(position);
                bundle.putString(PublicFuntion.HistoryNum,String.valueOf(send_arg.getHistoryNum()));
                bundle.putString(PublicFuntion.PlayerA,String.valueOf(send_arg.getPlayerA()));
                bundle.putString(PublicFuntion.PlayerB,String.valueOf(send_arg.getPlayerB()));
                bundle.putString(PublicFuntion.Winner,String.valueOf(send_arg.getWinner()));
                bundle.putString(PublicFuntion.Date,String.valueOf(send_arg.getDate()));
                bundle.putString(PublicFuntion.Time,String.valueOf(send_arg.getTime()));
                intent.putExtras(bundle);
                startActivity(intent);
                getActivity().finish();
            }
        });
        return view;
    }
    private void setupItem() {
        lvInfo = (ListView) view.findViewById(R.id.history_info_lv_info);
    }

    private void setupList() {
        OkHttpHelper helper = new OkHttpHelper();
        Call call = helper.getRequest(UrlResources.HISTORY);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    data = convertHistoryData(response.body().string());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (data.size() != 0) {
                                HistoryAdapter adapter = new HistoryAdapter(data, context);
                                lvInfo.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    /**
     * 将服务器返回的字符串转换成List<GameHistory>信息
     * @param str 服务器返回的信息
     * @return 返回转换后的数据,转换失败返回null
     */
    private List<GameHistory> convertHistoryData(String str) {

        List<GameHistory> temp = null;

        try {
            JSONArray jsonArray = new JSONArray(str);
            temp = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                GameHistory gh = new GameHistory();

                gh.setPlayerA(jsonObj.getString("playerA"));
                gh.setPlayerB(jsonObj.getString("playerB"));
                gh.setWinner(jsonObj.getString("winner"));
                gh.setHistoryNum(jsonObj.getInt("historyNum"));
                gh.setDate(jsonObj.getString("date"));
                gh.setTime(jsonObj.getString("time"));

                temp.add(gh);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return temp;
    }

}
