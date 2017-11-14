package com.zsh.ricky.cardmanager.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.zsh.ricky.cardmanager.R;
import com.zsh.ricky.cardmanager.model.Card;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ricky on 2017/11/9.
 */

public class DeckAdapter extends RecyclerView.Adapter<DeckAdapter.ViewHolder> {

    private List<Card> cardList;
    private Map<Integer, Boolean> map;

    private static final String MEAT = "食肉";
    private static final String GRASS = "食草";

    public DeckAdapter(List<Card> cardList) {
        this.cardList = cardList;
        this.map = new HashMap<>();
        initMap();
    }

    public Map<Integer, Boolean> getMap() {return this.map;}

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.deck_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cbSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Integer pos = holder.getAdapterPosition();
                map.put(pos, isChecked);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Card card = cardList.get(position);

        holder.ivCardImage.setImageBitmap(card.getCardPhoto());
        holder.tvCardName.setText(card.getCardName());

        String attack = "A: " + card.getCardAttack();
        holder.tvCardAttack.setText(attack);

        String life = "L: " + card.getCardHP();
        holder.tvCardLife.setText(life);

        if (card.getCardType() == 0) {
            holder.tvCardType.setText(MEAT);
        } else {
            holder.tvCardType.setText(GRASS);
        }

        Integer pos = position;
        if (map.get(pos) == null) {
            map.put(pos, false);
        }
        holder.cbSelect.setChecked(map.get(pos));
    }

    @Override
    public int getItemCount() {
        return cardList.size();
    }

    /**
     * 初始化map存储CheckBox的状态
     */
    private void initMap() {
        for (int i = 0; i < cardList.size(); i++) {
            map.put(i, false);
        }
    }

    //私有构建每个item对应的控件
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivCardImage;    //卡牌图片
        TextView tvCardName;      //卡牌名字
        TextView tvCardAttack;    //卡牌攻击力
        TextView tvCardLife;     //卡牌生命值
        TextView tvCardType;     //卡牌类型
        CheckBox cbSelect;       //卡牌是否被选择

        ViewHolder(View itemView) {
            super(itemView);
            ivCardImage = (ImageView) itemView.findViewById(R.id.deck_item_cardImage);
            tvCardName = (TextView) itemView.findViewById(R.id.deck_item_cardName);
            tvCardAttack = (TextView) itemView.findViewById(R.id.deck_item_cardAttack);
            tvCardLife = (TextView) itemView.findViewById(R.id.deck_item_cardLife);
            tvCardType = (TextView) itemView.findViewById(R.id.deck_item_cardType);
            cbSelect = (CheckBox) itemView.findViewById(R.id.deck_item_cbSelect);
        }
    }


}
