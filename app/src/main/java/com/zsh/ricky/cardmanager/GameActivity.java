package com.zsh.ricky.cardmanager;

import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.zsh.ricky.cardmanager.model.Card;
import com.zsh.ricky.cardmanager.model.Message;
import com.zsh.ricky.cardmanager.model.Position;
import com.zsh.ricky.cardmanager.util.CardsFetcher;
import com.zsh.ricky.cardmanager.util.ModelUri;
import com.zsh.ricky.cardmanager.util.OkHttpHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

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

    private OkHttpClient client;
    private WebSocket gameSocket;

    private List<ImageView> playerHandCardViews;     //玩家手牌img
    private List<ImageView> playerBattleCardViews;   //玩家战场卡牌img
    private List<ImageView> matchHandCardViews;      //对手手牌img
    private List<ImageView> matchBattleCardView;    //对手战场卡牌img


    private List<Card> allCards;        //表示所有卡牌
    private List<Integer> playerDeck;    //表示玩家选择的卡组
    private List<Integer> battleDeck;    //表示对手选择的卡组
    private int playerCurCard = 0;     //表示玩家已经抽到的牌的位置
    private int battleCurCard = 0;     //表示对战玩家已经抽道德牌的位置

    private static final int PLAYER_HAND_ROW = 3;
    private static final int PLAYER_BATTLE_ROW = 2;
    private static final int MATCH_BATTLE_ROW = 1;
    private static final int MATCH_HAND_ROW = 0;
    private static final float APPEAR_ALPHA = 1.0f;
    private static final float DISAPPEAR_ALPHA = 0.0f;
    private static final float CLICK_ALPHA = 0.5f;

    //------------私有函数区域-----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //初始化私有变量
        gameActivity = GameActivity.this;
        preClickedView = null;
        prePosition = null;

        createCards();
        initAnimation();
        initWidget();
        createBattleCard();
        waitForNext();
        startTurn();
    }


    /**
     * 初始化WebSocket相关变量
     */
    private void initWebSocket() {
        client = new OkHttpClient.Builder()
                .readTimeout(3000, TimeUnit.SECONDS)//设置读取超时时间
                .writeTimeout(3000, TimeUnit.SECONDS)//设置写的超时时间
                .connectTimeout(3000, TimeUnit.SECONDS)//设置连接超时时间
                .build();

        Request request = new Request.Builder()
                .url(OkHttpHelper.GAME_SOCKET)
                .build();

        gameSocket = client.newWebSocket(request,
                new GameWebSocketListener());

    }

    /**
     * 自己构造玩家选择的卡组，用于测试
     */
    private void createCards() {

        userID = "4399";
        playerDeck = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            playerDeck.add(i + 1);
        }

        //获取所有卡牌信息
        CardsFetcher fetcher = new CardsFetcher(GameActivity.this);
        allCards = fetcher.getCardList();
    }

    /**
     * 构造处于对方战斗区域的卡牌，用于测试
     */
    private void createBattleCard() {
        int cardPos = 3;
        ImageView matchView = matchBattleCardView.get(1);
        Card card = allCards.get(cardPos);
        Position position = (Position) matchView.getTag(R.id.img_pos);
        position.setCardPosition(cardPos);
        matchView.setImageBitmap(card.getCardPhoto());
        matchView.setAlpha(APPEAR_ALPHA);

        cardPos = 4;
        matchView = matchBattleCardView.get(3);
        card = allCards.get(cardPos);
        position = (Position) matchView.getTag(R.id.img_pos);
        position.setCardPosition(cardPos);
        matchView.setImageBitmap(card.getCardPhoto());
        matchView.setAlpha(APPEAR_ALPHA);

        matchView = null;
        card = null;
    }

    /**
     * 获取intent中卡牌选择信息构造牌组
     */
    private void initCards() {
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
                    position.setCardPosition(j);
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

                    if (i == MATCH_HAND_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(APPEAR_ALPHA);
                        matchHandCardViews.add(imgView);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == MATCH_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        matchBattleCardView.add(imgView);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == PLAYER_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        playerBattleCardViews.add(imgView);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == PLAYER_HAND_ROW){
                        int cardPos = playerDeck.get(j);
                        Card card = allCards.get(cardPos);
                        position.setCardPosition(cardPos);
                        imgView.setImageBitmap(card.getCardPhoto());
                        imgView.setAlpha(APPEAR_ALPHA);
                        playerHandCardViews.add(imgView);
                        imgView.setTag(R.id.img_pos, position);
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

    private void initMatchHand() {
        for (int i = 1; i < matchHandCardViews.size() - 1; i++) {

        }
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
                    int cardPos = prePosition.getCardPosition();
                    Card card = allCards.get(cardPos);
                    imageView.setImageBitmap(card.getCardPhoto());
                    //设置选择手牌对应的卡牌图片
                    preClickedView.setAlpha(DISAPPEAR_ALPHA);
                    position.setCardPosition(cardPos);

//                    Message message = new Message(Message.Type.PLAY, userID, prePosition, position);
//                   gameSocket.send(message.toJSON());

                    preClickedView = null;
                    prePosition = null;

                }
                //表示玩家想要攻击对方战场上的卡牌
            } else if (prePosition.getRow() == PLAYER_BATTLE_ROW &&
                    position.getRow() == MATCH_BATTLE_ROW) {
                if (view.getAlpha() == APPEAR_ALPHA) {

                    Card playerCard = allCards.get(prePosition.getCardPosition());
                    Card battleCard = allCards.get(position.getCardPosition());
                    if (playerCard.getCardAttack() > battleCard.getCardAttack()) {
                        setDisappearAnimation(view);
                        preClickedView.setAlpha(APPEAR_ALPHA);
                    } else if (playerCard.getCardAttack() < battleCard.getCardAttack()) {
                        setDisappearAnimation(preClickedView);
                    } else {
                        setDisappearAnimation(preClickedView);
                        setDisappearAnimation(view);
                        preClickedView.setAlpha(DISAPPEAR_ALPHA);
                    }

//                    Message message = new Message(Message.Type.PLAY, userID, prePosition, position);
//                    gameSocket.send(message.toJSON());

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

//                Message message = new Message(Message.Type.END, userID);
//                gameSocket.send(message.toJSON());

                //断开WebSocket连接
//                client.dispatcher().executorService().shutdown();

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
//        waitForNext();   //玩家本回合结束，限制玩家操作

        drawDeck(); //抽牌

//        Message message = new Message(Message.Type.TURN, userID);
//        gameSocket.send(message.toJSON());
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
                    int cardPos = playerDeck.get(playerCurCard);
                    Card card = allCards.get(cardPos);
                    img.setImageBitmap(card.getCardPhoto());
                    Position position = (Position) img.getTag(R.id.img_pos);
                    position.setCardPosition(cardPos);
                    playerCurCard++;
                    setAppearAnimation(img);
                    return;
                }
            }
        }
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

    /**
     * 处理WebSocket有关的类
     */
    private class GameWebSocketListener extends WebSocketListener {

        private String WEB_TAG = "gameWebSocket";

        /**
         * 初始化时，要提供使用它的活动
         */
        public GameWebSocketListener() {}

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.i(WEB_TAG, "onOpen: send Message");
            Message message = new Message(Message.Type.START, userID, playerDeck);
            webSocket.send(message.toJSON());
        }

        @Override
        public void onMessage(final WebSocket webSocket, String text) {
            Log.i(WEB_TAG, "onMessage: receive Message");
            final Message message = new Message(text);
            switch (message.getType()) {
                case WAIT:
                    Log.i(WEB_TAG, "onMessage: wait for message");
                    break;
                case FIRST:   //处理玩家先手进攻
                    battleDeck = message.getDeck();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initWidget();
                        }
                    });
                    break;
                case SECOND:   //处理玩家后手进攻
                    battleDeck = message.getDeck();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initWidget();
                        }
                    });
                    break;
                case TURN:  //玩家开始自己回合处理
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startTurn();
                        }
                    });
                    break;
                case END:  //玩家战败处理
                    Message end = new Message(Message.Type.END, userID);
                    webSocket.send(end.toJSON());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameLose();
                        }
                    });
                    client.dispatcher().executorService().shutdown();
                    break;
                case PLAY: //卡牌选择处理
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handlePlayEvent(message.getPrePos(), message.getNextPos());
                        }
                    });
                    break;
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.i(WEB_TAG, "onClosed: game");
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
            Log.i(WEB_TAG, "onClosing: game");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
            Log.i(TAG, "onFailure: game");
        }

        /**
         * 处理对战玩家对卡牌的操作
         * @param prePos 前一次选中卡牌的位置
         * @param nextPos 后一次选中卡牌的位置
         */
        private void handlePlayEvent(Position prePos, Position nextPos) {

            //将手牌放到场上的操作
            if (prePos.getRow() == GameActivity.MATCH_HAND_ROW &&
                    nextPos.getRow() == GameActivity.MATCH_BATTLE_ROW) {

                ImageView preView = matchHandCardViews.get(prePos.getColumn());
                ImageView nextView = matchBattleCardView.get(nextPos.getColumn());
                Card card = allCards.get(prePos.getCardPosition());

                //设定之前的卡牌消失
                preView.setAlpha(GameActivity.DISAPPEAR_ALPHA);
                //放在战场上的卡牌显示

                nextView.setImageBitmap(card.getCardPhoto());
                nextView.setAlpha(GameActivity.APPEAR_ALPHA);
            } else if (prePos.getRow() == GameActivity.MATCH_BATTLE_ROW &&
                    nextPos.getRow() == GameActivity.PLAYER_BATTLE_ROW) {
                //表示对方玩家向对方卡牌发动攻击
                Card playerCard = allCards.get(nextPos.getCardPosition());
                Card battleCard = allCards.get(prePos.getCardPosition());
                ImageView playerView = playerBattleCardViews.get(nextPos.getColumn());
                ImageView battleView = matchBattleCardView.get(prePos.getColumn());

                if (playerCard.getCardAttack() > battleCard.getCardAttack()) {
                    setDisappearAnimation(battleView);
                } else if (playerCard.getCardAttack() < battleCard.getCardAttack()) {
                    setDisappearAnimation(playerView);
                } else if (playerCard.getCardAttack() == battleCard.getCardAttack()) {
                    setDisappearAnimation(playerView);
                    setDisappearAnimation(battleView);
                }

            }  //玩家直接进攻会在Type为end,不需要处理

        }

    }

}
