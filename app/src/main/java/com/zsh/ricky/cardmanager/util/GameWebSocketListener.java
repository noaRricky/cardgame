package com.zsh.ricky.cardmanager.util;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.zsh.ricky.cardmanager.GameActivity;
import com.zsh.ricky.cardmanager.model.Card;
import com.zsh.ricky.cardmanager.model.Message;
import com.zsh.ricky.cardmanager.model.Position;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by Ricky on 2017/11/6.
 */

public class GameWebSocketListener extends WebSocketListener{

    private GameActivity game;   //运行它的GameActivity
    private String player;       //玩家ID号码

    private static String TAG = "gameWebSocket";

    /**
     * 初始化时，要提供使用它的活动
     * @param game 游戏活动
     */
    public GameWebSocketListener(GameActivity game, String player) {
        this.game = game;
        this.player = player;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Message message = new Message(Message.Type.START, player);
        webSocket.send(message.toJSON());
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        final Message message = new Message(text);
        game.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (message.getType()) {
                    case GAME:   //处理玩家可以开始处理的信息
                        game.initAllGame();
                        game.startTurn();
                        break;
                    case WAIT:   //处理玩家等待对战玩家处理
                        game.initAllGame();
                        game.waitForNext();
                        break;
                    case TURN:  //玩家开始自己回合处理
                        game.startTurn();
                        break;
                    case END:  //玩家战败处理
                        game.gameLose();
                        break;
                    case PLAY: //卡牌选择处理
                        handlePlayEvent(message.getPrePos(), message.getNextPos());
                        break;
                }
            }
        });
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        Log.i(TAG, "onClosed: game");
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        webSocket.close(1000, null);
        Log.i(TAG, "onClosing: game");
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

            ImageView preView = game.matchHandCardViews.get(prePos.getColumn());
            ImageView nextView = game.matchBattleCardView.get(nextPos.getColumn());

            //设定之前的卡牌消失
            preView.setAlpha(GameActivity.DISAPPEAR_ALPHA);
            //放在战场上的卡牌显示
            nextView.setImageBitmap(game.cards.get(prePos.getColumn()).getCardPhoto());
            nextView.setAlpha(GameActivity.APPEAR_ALPHA);
        } else if (prePos.getRow() == GameActivity.MATCH_BATTLE_ROW &&
                nextPos.getRow() == GameActivity.PLAYER_BATTLE_ROW) {
            //表示对方玩家向对方卡牌发动攻击
            Card playerCard = game.cards.get(nextPos.getColumn());
            Card battleCard = game.cards.get(prePos.getColumn());
            ImageView playerView = game.playerBattleCardViews.get(nextPos.getColumn());
            ImageView battleView = game.matchBattleCardView.get(prePos.getColumn());

            if (playerCard.getCardAttack() > battleCard.getCardAttack()) {
                game.setDisappearAnimation(battleView);
            } else if (playerCard.getCardAttack() < battleCard.getCardAttack()) {
                game.setDisappearAnimation(playerView);
            } else if (playerCard.getCardAttack() == battleCard.getCardAttack()) {
                game.setDisappearAnimation(playerView);
                game.setDisappearAnimation(battleView);
            }

        }  //玩家直接进攻会在Type为end,不需要处理

    }

}
