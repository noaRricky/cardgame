package com.zsh.ricky.cardmanager.util;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import com.zsh.ricky.cardmanager.GameActivity;
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

    private View preClickView;      //第一次点击的控件
    private Position prePosition;   //第一次控件对应位置

    /**
     * 初始化时，要提供使用它的活动
     * @param game 游戏活动
     */
    public GameWebSocketListener(GameActivity game) {
        this.game = game;
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        Message message = new Message();
        message.setType(Message.Type.START);
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
                    case TURN:
                        game.startTurn();
                        break;
                    case END:
                        game.gameLose();
                        break;
                    case PLAY:
                        handlePlayEvent(message.getPosition());
                }
            }
        });
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
    }

    private void handlePlayEvent(Position position) {

        //第一次只能选择对战玩家区域卡牌
        if (preClickView == null) {  //选择对战玩家手牌
            if (position.getRow() == GameActivity.MATCH_HAND_ROW) {
                preClickView = game.matchHandCardViews.get(position.getColumn());
                prePosition = position;
            } else if (position.getRow() == GameActivity.MATCH_BATTLE_ROW) {
                preClickView = game.matchBattleCardView.get(position.getColumn());
                prePosition = position;
            }
        } else { //第二次选择
            //两次选择相同表示放弃之前选择
        }
    }

}
