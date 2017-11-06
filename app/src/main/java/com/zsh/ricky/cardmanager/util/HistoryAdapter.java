package com.zsh.ricky.cardmanager.util;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.view.View;
import android.widget.TextView;

import com.zsh.ricky.cardmanager.R;
import com.zsh.ricky.cardmanager.model.GameHistory;

import java.util.List;

/**
 * Created by Ricky on 2017/11/5.
 */

public class HistoryAdapter extends BaseAdapter {

    private Context context;
    private List<GameHistory> data;
    public HistoryAdapter(List<GameHistory> d,Context ct){
        context=ct;
        data=d;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (context == null) {
            context = parent.getContext();
        }
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.history_item, null);
            viewHolder = new ViewHolder();
            viewHolder.hPlayerTv = (TextView) convertView.findViewById(R.id.history_item_players);
            viewHolder.hWinnerTv = (TextView) convertView.findViewById(R.id.history_item_winner);
            viewHolder.hDateTv = (TextView) convertView.findViewById(R.id.history_item_date);
            convertView.setTag(viewHolder);
        }

        //获取viewHolder实例
        viewHolder = (ViewHolder) convertView.getTag();
        //设置数据
        GameHistory history = data.get(position);
        String players = "对战者：" + history.getPlayerA() + " vs " + history.getPlayerB();
        viewHolder.hPlayerTv.setText(players);
        String winner = "胜者：" + history.getWinner();
        viewHolder.hWinnerTv.setText(winner);
        viewHolder.hDateTv.setText(history.getDate());

        return convertView;
    }

    static private class ViewHolder {
        private TextView hPlayerTv;   //当前对战文本
        private TextView hWinnerTv;   //当前胜者文本
        private TextView hDateTv;     //当前对战日期
    }
}
