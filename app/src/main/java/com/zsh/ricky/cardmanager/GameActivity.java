package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.graphics.ColorSpace;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.zsh.ricky.cardmanager.model.Card;
import com.zsh.ricky.cardmanager.model.Position;
import com.zsh.ricky.cardmanager.util.CardsFetcher;
import com.zsh.ricky.cardmanager.util.ModelUri;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import okhttp3.WebSocket;

public class GameActivity extends AppCompatActivity {

    private GameActivity gameActivity = null;

    private GridLayout gameGrid;

    private String userID;     //存储用户ID

    //两次点击事件，存储上一次点击的信息
    private View preClickedView;
    private Position prePosition;

    private AlphaAnimation appearAnimation;
    private AlphaAnimation disappearAnimation;

    private static final int ROW = 4;
    private static final int COLUMN = 7;
    private static final String TAG = "game";

    private WebSocket gameSocket;

    //--------------公共变量---------------------------
    public List<ImageView> playerHandCardViews;     //玩家手牌img
    public List<ImageView> playerBattleCardViews;   //玩家战场卡牌img
    public List<ImageView> matchHandCardViews;      //对手手牌img
    public List<ImageView> matchBattleCardView;    //对手战场卡牌img


    public List<Card> allCards;        //表示所有卡牌
    public List<Integer> playerDeck;    //表示玩家选择的卡组
    public List<Integer> battleDeck;    //表示对手选择的卡组
    public int playerCurCard = 0;     //表示玩家已经抽到的牌的位置
    public int battleCurCard = 0;     //表示对战玩家已经抽道德牌的位置

    public static final int PLAYER_HAND_ROW = 3;
    public static final int PLAYER_BATTLE_ROW = 2;
    public static final int MATCH_BATTLE_ROW = 1;
    public static final int MATCH_HAND_ROW = 0;
    public static final float APPEAR_ALPHA = 1.0f;
    public static final float DISAPPEAR_ALPHA = 0.0f;
    public static final float CLICK_ALPHA = 0.5f;

    //------------私有函数区域-----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //初始化私有变量
        gameActivity = GameActivity.this;
        preClickedView = null;
        prePosition = null;

    }

    /**
     * 获取intent中卡牌选择信息构造牌组
     */
    private void createCards() {
        Intent intent = getIntent();

        userID = intent.getStringExtra(ModelUri.USER_ID);
        playerDeck = intent.getIntegerArrayListExtra(ModelUri.SELECT_LIST);

        //获取所有卡牌信息
        CardsFetcher fetcher = new CardsFetcher(GameActivity.this);
        allCards = fetcher.getCardList();
    }

    /**
     * 初始化游戏布局
     */
    private void initWidget() {

        gameGrid = (GridLayout) this.findViewById(R.id.game_grid);
        matchHandCardViews = new ArrayList<>();
        matchBattleCardView = new ArrayList<>();
        playerHandCardViews = new ArrayList<>();
        playerBattleCardViews = new ArrayList<>();

        for (int i = 0; i < ROW; i++) {
            for (int j = 0; j < COLUMN; j++) {
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sp_001);
//                float scale = (float) windowHeight / ROW / bitmap.getHeight();
//                Matrix matrix = new Matrix();
//                matrix.postScale(scale, scale);
//                Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0,
//                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                ImageView imgView = new ImageView(gameActivity);
                if ((i == 0 && j == 0) || (i == 3 && j == 0)) {
                    imgView.setImageResource(R.drawable.life_ball);
                    Position position = new Position(i, j, Position.Type.LIFE);
                    position.setCardID(j);
                    imgView.setTag(R.id.img_pos, position);

                } else if (i == 0 && j == (COLUMN - 1)) {
                    imgView.setImageResource(R.drawable.card_box);
                    Position position = new Position(i, j, Position.Type.DECK);
                    imgView.setTag(R.id.img_pos, position);
                } else if (i == 3 && j == (COLUMN - 1)) {
                    imgView.setImageResource(R.drawable.red_button);
                    Position position = new Position(i, j, Position.Type.BUTTON);
                    imgView.setTag(R.id.img_pos, position);
                } else {
                    imgView.setVisibility(View.VISIBLE);
                    Position position = new Position(i, j, Position.Type.CARD);
                    imgView.setTag(R.id.img_pos, position);
                    if (i == MATCH_HAND_ROW) {
                        Card card = allCards.get(battleDeck.get(j));
                        imgView.setImageBitmap(card.getCardPhoto());
                        imgView.setAlpha(APPEAR_ALPHA);
                        matchHandCardViews.add(imgView);
                    } else if (i == MATCH_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        matchBattleCardView.add(imgView);
                    } else if (i == PLAYER_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        playerBattleCardViews.add(imgView);
                    } else if (i == PLAYER_HAND_ROW){
                        imgView.setImageResource(playerDeck.get(j));
                        imgView.setAlpha(APPEAR_ALPHA);
                        playerHandCardViews.add(imgView);
                    }
                }

                imgView.setOnClickListener(new ImageClickListener());
                GridLayout.Spec rowSpec = GridLayout.spec(i, 1f);
                GridLayout.Spec columnSpec = GridLayout.spec(j, 1f);
                GridLayout.LayoutParams layoutParams =
                        new GridLayout.LayoutParams(rowSpec, columnSpec);
                layoutParams.height = 0;
                layoutParams.width = 0;
                gameGrid.addView(imgView, layoutParams);
            }
        }

        //因为已经抽取了5张牌，设置当前应该抽取的卡牌
        playerCurCard = 5;
        battleCurCard = 5;

    }

    /**
     * 初始化动画
     */
    private void initAnimation() {
        appearAnimation = new AlphaAnimation(DISAPPEAR_ALPHA, APPEAR_ALPHA);
        appearAnimation.setDuration(500);

        disappearAnimation = new AlphaAnimation(APPEAR_ALPHA, DISAPPEAR_ALPHA);
        disappearAnimation.setDuration(500);
    }

    private class ImageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Position position = (Position) v.getTag(R.id.img_pos);

            switch (position.getType()) {
                case BUTTON:
                    Log.i(TAG, "button");
                    handleButtonClick(position, v);
                    break;
                case CARD:
                    Log.i(TAG, "card");
                    //如果选中卡牌不是对手手牌
                    handleCardClick(position, v);
                    break;
                case DECK:
                    Log.i(TAG, "deck");
                case LIFE:
                    Log.i(TAG, "onClick: life");
                    handleLifeClick(position, v);
            }
        }
    }

    /**
     * 处理选中的视图是card的事件
     * @param position card位置
     * @param view 当前视图
     */
    private void handleCardClick(Position position,View view) {

        //第一次只能选择己方手牌
        if (preClickedView == null ) {
            if (position.getRow() == PLAYER_BATTLE_ROW || position.getRow() == PLAYER_HAND_ROW) {
                if (view.getAlpha() == APPEAR_ALPHA) {
                    view.setAlpha(CLICK_ALPHA);
                    preClickedView = view;
                    prePosition = position;
                }
            }
            //第二次选择
        } else {
            //两次选择相同表示放弃之前选择
            if (preClickedView == view) {
                view.setAlpha(APPEAR_ALPHA);
                preClickedView = null;
                prePosition = null;
                //表示将手牌放到场上
            } else if (prePosition.getRow() == PLAYER_HAND_ROW &&
                    position.getRow() == PLAYER_BATTLE_ROW) {
                if (view.getAlpha() == DISAPPEAR_ALPHA) {
                    view.setAlpha(APPEAR_ALPHA);
                    ImageView imageView = (ImageView) view;
                    Card card = allCards.get(playerDeck.get(prePosition.getCardID()));
                    imageView.setImageBitmap(card.getCardPhoto());
                    //设置选择手牌对应的卡牌图片
                    preClickedView.setAlpha(DISAPPEAR_ALPHA);
                    preClickedView = null;
                    prePosition = null;
                }
                //表示玩家想要攻击对方战场上的卡牌
            } else if (prePosition.getRow() == PLAYER_BATTLE_ROW &&
                    position.getRow() == MATCH_BATTLE_ROW) {
                if (view.getAlpha() == APPEAR_ALPHA) {

                    Card playerCard = allCards.get(playerDeck.get(prePosition.getCardID()));
                    Card battleCard = allCards.get(battleDeck.get(position.getCardID()));
                    if (playerCard.getCardAttack() > battleCard.getCardAttack()) {
                        setDisappearAnimation(view);
                    } else if (playerCard.getCardAttack() < battleCard.getCardAttack()) {
                        setDisappearAnimation(preClickedView);
                    } else {
                        setDisappearAnimation(preClickedView);
                        setDisappearAnimation(view);
                    }

                    preClickedView = null;
                    prePosition = null;
                }

            }
        }


    }

    /**
     * 直接攻击玩家生命
     * @param position 卡牌位置
     * @param view 当前点击的img
     */
    private void handleLifeClick(Position position, View view) {
        if (preClickedView != null) {
            if (prePosition.getRow() == PLAYER_BATTLE_ROW && isMatchBattleEmpty()) {
                setDisappearAnimation(view);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.game_win);
                    }
                }, 1000);
            }
        }
    }

    /**
     * 回合结束时间
     * @param position 位置
     * @param view 点击的视图
     */
    private void handleButtonClick(Position position, View view) {
        drawDeck();
    }

    /**
     * 判断对方战场上是否有卡牌
     * @return 如果战场上有卡牌返回true,否则返回false
     */
    private boolean isMatchBattleEmpty() {
        for (ImageView img : matchBattleCardView) {
            if (img.getAlpha() == APPEAR_ALPHA) {
                return false;
            }
        }

        return true;
    }

    /**
     * 抽牌事件
     */
    private void drawDeck() {

        if (playerCurCard < playerDeck.size()) {
            for (ImageView img : playerHandCardViews) {
                if (img.getAlpha() == DISAPPEAR_ALPHA) {
                    Card card = allCards.get(playerDeck.get(playerCurCard));
                    img.setImageBitmap(card.getCardPhoto());
                    playerCurCard++;
                    setAppearAnimation(img);
                    return;
                }
            }
        }
    }

    /**
     * 初始化所有游戏内容
     */
    public void initAllGame() {
        initWidget();
        initAnimation();
    }

    /**
     * 通过将所有控件设置能不能点击，禁止玩家操作
     */
    public void waitForNext() {
        for (ImageView view : playerBattleCardViews) {
            view.setClickable(false);
        }
        for (ImageView view : playerHandCardViews) {
            view.setClickable(false);
        }
        for (ImageView view : matchBattleCardView) {
            view.setClickable(false);
        }
        for (ImageView view : matchHandCardViews) {
            view.setClickable(false);
        }
    }

    /**
     * 通过将所有控件设置为可点击，让玩家进行操作
     */
    public void startTurn() {

        //首先完成抽排后，玩家才能操作
        drawDeck();

        for (ImageView view : playerHandCardViews) {
            view.setClickable(true);
        }
        for (ImageView view : playerBattleCardViews) {
            view.setClickable(true);
        }
        for (ImageView view : matchBattleCardView) {
            view.setClickable(true);
        }
        for (ImageView view : matchHandCardViews) {
            view.setClickable(true);
        }
    }

    public void gameLose() {
        ImageView img = playerHandCardViews.get(0);
        setDisappearAnimation(img);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.game_lose);
            }
        }, 1000);
    }

    /**
     * 卡牌出现动画
     * @param view 要出现的卡牌
     */
    public void setAppearAnimation(final View view) {

        //使卡牌逐渐出现
        view.startAnimation(appearAnimation);
        appearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                view.setAlpha(APPEAR_ALPHA);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    /**
     * 卡牌消失动画
     * @param view 要消失的卡牌
     */
    public void setDisappearAnimation(final View view) {
        view.startAnimation(disappearAnimation);
        disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAlpha(DISAPPEAR_ALPHA);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

}
