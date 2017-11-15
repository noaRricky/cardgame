package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.zsh.ricky.cardmanager.model.Card;
import com.zsh.ricky.cardmanager.util.CardsFetcher;
import com.zsh.ricky.cardmanager.util.DeckAdapter;
import com.zsh.ricky.cardmanager.util.ModelUri;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DeckActivity extends AppCompatActivity {

    private List<Card> cardList;
    private RecyclerView rvCards;
    private Button btStart;
    private ImageButton ibBack;

    DeckAdapter deckAdapter;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deck);

        initWidget();
        initEvent();
        try {
            initCardData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "请选择10张卡牌", Toast.LENGTH_SHORT).show();
    }

    /**
     * 初始化所有控件
     */
    private void initWidget() {
        rvCards = (RecyclerView) this.findViewById(R.id.deck_rvCards);
        btStart = (Button) this.findViewById(R.id.deck_btStart);
        ibBack = (ImageButton) this.findViewById(R.id.deck_ibBack);

        Intent intent = getIntent();
        userID = intent.getStringExtra(ModelUri.USER_ID);

    }

    /**
     * 初始化所有事件
     */
    private void initEvent() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeckActivity.this, StartActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                startActivity(intent);
                finish();
            }
        });
        btStart.setOnClickListener(new StartClick());
    }

    /**
     * 获取并且显示recyclerView的数据
     */
    private void initCardData() {
        //获取数据
        CardsFetcher fetcher = new CardsFetcher(DeckActivity.this);
        cardList = fetcher.getCardList();
        deckAdapter = new DeckAdapter(cardList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(DeckActivity.this);
        rvCards.setLayoutManager(layoutManager);
        rvCards.setAdapter(deckAdapter);
    }

    private class StartClick implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Map<Integer, Boolean> map = deckAdapter.getMap();

            int count = countSelected(map);
            if (count != 10) {
                Toast.makeText(getApplicationContext(),
                        "当前选择" + String.valueOf(count) + "卡牌",
                        Toast.LENGTH_SHORT).show();
            } else {
                ArrayList<Integer> selectedList = getSelectedCards(map);
                Intent intent = new Intent(DeckActivity.this, GameActivity.class);
                intent.putExtra(ModelUri.USER_ID, userID);
                intent.putIntegerArrayListExtra(ModelUri.SELECT_LIST, selectedList);
                startActivity(intent);
                finish();
            }
        }

        /**
         * 计算玩家选择的卡牌的数量
         * @param map 存储卡牌选择的信息
         * @return 选择卡牌的数目
         */
        private int countSelected(Map<Integer, Boolean> map) {
            int count = 0;
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i)) {
                    count++;
                }
            }

            return count;
        }

        /**
         * 获取选择的卡牌编号
         * @param map 存储选择的卡牌
         * @return 选择的卡牌编号信息
         */
        private ArrayList<Integer> getSelectedCards(Map<Integer, Boolean> map) {
            ArrayList<Integer> selectedList = new ArrayList<>();
            for (int i = 0; i < map.size(); i++) {
                if (map.get(i)) {
                    selectedList.add(i);
                }
            }
            return selectedList;
        }
    }
}
