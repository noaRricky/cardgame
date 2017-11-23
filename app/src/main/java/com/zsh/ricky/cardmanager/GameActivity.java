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
import android.widget.Toast;

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
    private AlphaAnimation disappearAnimation2;

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
    private static final int NORMAL_CLOSURE_STATUS = 1000;

    //------------私有函数区域-----------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //初始化私有变量
        gameActivity = GameActivity.this;
        preClickedView = null;
        prePosition = null;

//        initCards();
        createCards();
        initAnimation();
        initWidget();
        waitForNext();
        initWebSocket();
    }

    @Override
    public void onBackPressed() {
        handleSurrenderEvent();
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

        userID = "120";
        playerDeck = new ArrayList<>();

        playerDeck.add(0);
        playerDeck.add(1);
        playerDeck.add(2);
        playerDeck.add(3);
        playerDeck.add(4);
        playerDeck.add(5);
        playerDeck.add(6);
        playerDeck.add(10);
        playerDeck.add(11);
        playerDeck.add(12);

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

        cardPos = 5;
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

    private void initMatchHand() {
        battleCurCard = 0;

       for (int i = 0; i < matchHandCardViews.size(); i++) {
           int cardPos = battleDeck.get(battleCurCard);
           final ImageView img = matchHandCardViews.get(i);
           Position position = (Position) img.getTag(R.id.img_pos);
           position.setCardPosition(cardPos);

           AlphaAnimation appear = new AlphaAnimation(DISAPPEAR_ALPHA, APPEAR_ALPHA);
           img.startAnimation(appear);
           appear.setAnimationListener(new Animation.AnimationListener() {
               @Override
               public void onAnimationStart(Animation animation) {

               }

               @Override
               public void onAnimationEnd(Animation animation) {
                    img.setAlpha(APPEAR_ALPHA);
               }

               @Override
               public void onAnimationRepeat(Animation animation) {

               }
           });
           //当前卡牌位置加一
           battleCurCard++;
       }
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

        playerCurCard = 0;      //初始化游戏玩家一开始卡牌位置

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

                    if (i == MATCH_HAND_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        matchHandCardViews.add(imgView);
                        Position position = new Position(i, j, Position.Type.CARD_BACK);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == MATCH_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        matchBattleCardView.add(imgView);
                        Position position = new Position(i, j, Position.Type.CARD_BACK);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == PLAYER_BATTLE_ROW) {
                        imgView.setImageResource(R.drawable.card_back);
                        imgView.setAlpha(DISAPPEAR_ALPHA);
                        playerBattleCardViews.add(imgView);
                        Position position = new Position(i, j, Position.Type.CARD_BACK);
                        imgView.setTag(R.id.img_pos, position);
                    } else if (i == PLAYER_HAND_ROW){
                        int cardPos = playerDeck.get(playerCurCard);
                        playerCurCard++;
                        Card card = allCards.get(cardPos);
                        Position position = new Position(i, j, Position.Type.CARD);
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

    }


    /**
     * 初始化动画
     */
    private void initAnimation() {
        appearAnimation = new AlphaAnimation(DISAPPEAR_ALPHA, APPEAR_ALPHA);
        appearAnimation.setDuration(500);

        disappearAnimation = new AlphaAnimation(APPEAR_ALPHA, DISAPPEAR_ALPHA);
        disappearAnimation.setDuration(500);

        disappearAnimation2 = new AlphaAnimation(APPEAR_ALPHA, DISAPPEAR_ALPHA);
        disappearAnimation2.setDuration(500);
    }

    private class ImageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            Position position = (Position) v.getTag(R.id.img_pos);

            if (position.isUseable()) {
                switch (position.getType()) {
                    case BUTTON:
                        Log.i(TAG, "button");
                        handleButtonClick(position, v);
                        break;
                    case CARD_BACK:   //CARD_BAK和CARD处理事务相同
                    case CARD:
                        Log.i(TAG, "card");
                        //如果选中卡牌不是对手手牌
                        handleCardClick(position, v);
                        break;
                    case DECK:
                        Log.i(TAG, "deck");
                        handleSurrenderEvent();
                    case LIFE:
                        Log.i(TAG, "onClick: life");
                        handleLifeClick(position, v);
                }
            }
        }
    }

    /**
     * 处理选中的视图是card的事件
     * @param position card位置
     * @param view 当前视图
     */
    private void handleCardClick(Position position, final View view) {

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
                    prePosition.setType(Position.Type.CARD_BACK);
                    preClickedView.setAlpha(DISAPPEAR_ALPHA);
                    position.setCardPosition(cardPos);
                    position.setType(Position.Type.CARD);

                    position.setUseable(false); //设定当前位置不能使用

                    Message message = new Message(Message.Type.PLAY, userID, prePosition, position);
                    gameSocket.send(message.toJSON());

                    preClickedView = null;
                    prePosition = null;

                }
                //表示玩家想要攻击对方战场上的卡牌
            } else if (prePosition.getRow() == PLAYER_BATTLE_ROW &&
                    position.getRow() == MATCH_BATTLE_ROW) {
                if (view.getAlpha() == APPEAR_ALPHA) {

                    int cardPos = position.getCardPosition();
                    Position.Type card_type = position.getType();
                    //如果对战的卡牌为背面，将卡牌翻转
                    if (card_type == Position.Type.CARD_BACK) {
                        ImageView img = (ImageView) view;
                        img.setImageBitmap(allCards.get(cardPos).getCardPhoto());
                    }

                    Card playerCard = allCards.get(prePosition.getCardPosition());
                    Card battleCard = allCards.get(position.getCardPosition());

                    playerCard.injured(battleCard.getCardAttack());
                    battleCard.injured(playerCard.getCardAttack());

                    if (playerCard.getCardHP() <= 0) {
                        setDisappearAnimation(preClickedView);
                        prePosition.setType(Position.Type.CARD_BACK);
                        prePosition.setUseable(true);
                    }
                    if (battleCard.getCardHP() <= 0) {
                        setDisappearAnimation2(view);
                        preClickedView.setAlpha(APPEAR_ALPHA);
                        position.setType(Position.Type.CARD_BACK);
                        prePosition.setUseable(false);  //当前位置不能使用
                    }


                    Message message = new Message(Message.Type.PLAY, userID, prePosition, position);
                    gameSocket.send(message.toJSON());

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

                Message message = new Message(Message.Type.END, userID);
                gameSocket.send(message.toJSON());

                //断开WebSocket连接
                gameSocket.close(NORMAL_CLOSURE_STATUS, "GAME END");
                client.dispatcher().executorService().shutdown();

                setDisappearAnimation(view);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setContentView(R.layout.game_win);

                        new Handler().postDelayed(new BackHomeRun(),1000);
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
        waitForNext();   //玩家本回合结束，限制玩家操作

        drawBattleDeck();   //本机模拟对战玩家抽牌操作，减少传输次数

        Message message = new Message(Message.Type.TURN, userID);
        gameSocket.send(message.toJSON());

        Toast.makeText(getApplicationContext(), "回合结束", Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理投降事件
     */
    private void handleSurrenderEvent() {

        Toast.makeText(getApplicationContext(), "你投降啦", Toast.LENGTH_SHORT).show();

        Message message = new Message(Message.Type.WIN, userID);
        gameSocket.send(message.toJSON());

        gameSocket.close(NORMAL_CLOSURE_STATUS, "surrender");
        client.dispatcher().executorService().shutdown();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.game_lose);
                new Handler().postDelayed(new BackHomeRun(),1000);
            }
        },1000);
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
                }
            }
        }
    }

    //对站者抽牌事件
    private void drawBattleDeck() {

        if (battleCurCard < battleDeck.size()) {
            for (ImageView img : matchHandCardViews) {
                if (img.getAlpha() == DISAPPEAR_ALPHA) {
                    int cardPos = battleDeck.get(battleCurCard);
                    img.setImageResource(R.drawable.card_back);
                    Position position = (Position) img.getTag(R.id.img_pos);
                    position.setCardPosition(cardPos);
                    battleCurCard++;
                    setAppearAnimation(img);
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
            Position position = (Position) view.getTag(R.id.img_pos);
            position.setUseable(true);
        }
        for (ImageView view : playerBattleCardViews) {
            view.setClickable(true);
            Position position = (Position) view.getTag(R.id.img_pos);
            position.setUseable(true);
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
    private void setAppearAnimation(final View view) {

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
    private void setDisappearAnimation(final View view) {
        view.setAlpha(APPEAR_ALPHA);
        view.startAnimation(disappearAnimation);
        disappearAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAlpha(DISAPPEAR_ALPHA);
                Log.i(TAG, "onAnimationEnd: set disappear view");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    private void setDisappearAnimation2(final View view) {
        view.setAlpha(APPEAR_ALPHA);
        view.startAnimation(disappearAnimation2);
        disappearAnimation2.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setAlpha(DISAPPEAR_ALPHA);
                Log.i(TAG, "onAnimationEnd: set disappear view");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    //返回主页面
    private class BackHomeRun implements Runnable {

        @Override
        public void run() {
            Intent intent = new Intent(GameActivity.this,
                    StartActivity.class);
            startActivity(intent);
            finish();
        }
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
                            startTurn();
                            initMatchHand();
                            Toast.makeText(getApplication(), "" +
                                    "你的回合", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case SECOND:   //处理玩家后手进攻
                    battleDeck = message.getDeck();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initMatchHand();
                            Toast.makeText(getApplication(), "" +
                                    "等待对方回合", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case TURN:  //玩家开始自己回合处理
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startTurn();
                            Toast.makeText(getApplication(), "" +
                                    "你的回合", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case END:  //玩家战败处理

                    webSocket.close(NORMAL_CLOSURE_STATUS, "GAME END");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gameLose();
                        }
                    });
                    client.dispatcher().executorService().shutdown();
                    break;
                case PLAY: //卡牌选择处理
                    handlePlayEvent(message.getPrePos(), message.getNextPos());
                    break;
                case WIN:
                    webSocket.close(NORMAL_CLOSURE_STATUS, "GAME END");
                    handleWinEvent();
                    client.dispatcher().executorService().shutdown();
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
        private void handlePlayEvent(final Position prePos,final Position nextPos) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //将手牌放到场上的操作
                    if (prePos.getRow() == GameActivity.PLAYER_HAND_ROW &&
                            nextPos.getRow() == GameActivity.PLAYER_BATTLE_ROW) {

                        //减去1因为手牌位置与实际图片位置差一
                        ImageView preView = matchHandCardViews.get(prePos.getColumn() - 1);
                        ImageView nextView = matchBattleCardView.get(nextPos.getColumn());

                        int cardPos = prePos.getCardPosition();
                        Card card = allCards.get(cardPos);
                        Position nextPosInfo = (Position) nextView.getTag(R.id.img_pos);
                        nextPosInfo.setCardPosition(cardPos);

                        //设定之前的卡牌消失
                        preView.setAlpha(DISAPPEAR_ALPHA);
                        //放在战场上的卡牌显示

                        nextView.setImageResource(R.drawable.card_back);
                        nextView.setAlpha(APPEAR_ALPHA);
                    } else if (prePos.getRow() == GameActivity.PLAYER_BATTLE_ROW &&
                            nextPos.getRow() == GameActivity.MATCH_BATTLE_ROW) {

                        //表示对方玩家向对方卡牌发动攻击
                        Card playerCard = allCards.get(nextPos.getCardPosition());
                        Card battleCard = allCards.get(prePos.getCardPosition());
                        ImageView playerView = playerBattleCardViews.get(nextPos.getColumn());
                        ImageView battleView = matchBattleCardView.get(prePos.getColumn());
                        Position player_pos = (Position) playerView.getTag(R.id.img_pos);
                        Position battle_pos = (Position) battleView.getTag(R.id.img_pos);

                        if (battle_pos.getType() == Position.Type.CARD_BACK) {
                            battleView.setImageBitmap(
                                    allCards.get(battle_pos.getCardPosition()).getCardPhoto());
                            battle_pos.setType(Position.Type.CARD);
                        }


                        playerCard.injured(battleCard.getCardAttack());
                        battleCard.injured(playerCard.getCardAttack());
                        if (playerCard.getCardHP() <= 0) {
                            setDisappearAnimation(playerView);
                            player_pos.setType(Position.Type.CARD_BACK);
                        }
                        if (battleCard.getCardHP() <= 0) {
                            setDisappearAnimation2(battleView);
                            battle_pos.setType(Position.Type.CARD_BACK);
                        }

                    }  //玩家直接进攻会在Type为end,不需要处理
                }
            });

        }

        /**
         * 处理获胜事件
         */
        private void handleWinEvent() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplication(), "敌方投降", Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(R.layout.game_win);

                            new Handler().postDelayed(new BackHomeRun(),1000);
                        }
                    }, 1000);
                }
            });
        }

    }

}
