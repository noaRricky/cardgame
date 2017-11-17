package com.zsh.ricky.cardmanager.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ricky on 2017/11/5.
 */

public class Message {

    private Type type;
    private String player;        //用户名字
    private Position prePos;      //第一次选择位置
    private Position nextPos;    //第二次选择位置
    private List<Integer> deck;    //用于游戏开始的时候传送卡组信息

    private static final String MSG_TYPE = "message_type";
    private static final String PLAYER = "player";
    private static final String PRE_ROW = "pre_row";
    private static final String PRE_COLUMN = "pre_column";
    private static final String PRE_IMG_TYPE = "pre_img_type";
    private static final String PRE_CARD_ID = "pre_card_id";
    private static final String NEXT_ROW = "next_row";
    private static final String NEXT_COLUMN = "next_column";
    private static final String NEXT_IMG_TYPE = "next_img_type";
    private static final String NEXT_CARD_ID = "next_card_id";
    private static final String DECK = "deck";
    private static final String EACH_CARD = "each_card";

    public Message(Type type, String player) {
        this.type = type;
        this.player = player;
        prePos = null;
        nextPos = null;
        deck = null;
    }

    public Message(Type type,String player, Position prePos, Position nextPos) {
        this.type = type;
        this.player = player;
        this.prePos = prePos;
        this.nextPos = nextPos;
        this.deck = null;
    }

    public Message(Type type, String player, List<Integer> deck) {
        this.type = type;
        this.player = player;
        this.deck = deck;

        prePos = null;
        nextPos = null;
    }

    /**
     * 根据json对象初始化message
     * @param json json对象对应字符串
     */
    public Message(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);

            this.type = Type.valueOf(jsonObject.getString(MSG_TYPE));
            this.player = jsonObject.getString(PLAYER);
            if (type == Type.PLAY) {
                this.prePos = new Position(
                        jsonObject.getInt(PRE_ROW),
                        jsonObject.getInt(PRE_COLUMN),
                        jsonObject.getInt(PRE_CARD_ID),
                        (Position.Type) jsonObject.get(PRE_IMG_TYPE)
                );
                this.nextPos = new Position(
                        jsonObject.getInt(NEXT_ROW),
                        jsonObject.getInt(NEXT_COLUMN),
                        jsonObject.getInt(NEXT_CARD_ID),
                        (Position.Type) jsonObject.get(NEXT_IMG_TYPE)
                );
            } else if (type == Type.START || type == Type.FIRST || type == Type.SECOND) {
                JSONArray deckArray = jsonObject.getJSONArray(DECK);
                deck = new ArrayList<>();
                for (int i = 0; i < deckArray.length(); i++) {
                    JSONObject eachObject = deckArray.getJSONObject(i);
                    deck.add(eachObject.getInt(EACH_CARD));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 转化为json字符串用于发送
     * @return json字符串
     */
    public String toJSON() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(MSG_TYPE, type);
            jsonObject.put(PLAYER, player);
            if (type == Type.PLAY) {
                jsonObject.put(PRE_ROW, prePos.getRow());
                jsonObject.put(PRE_COLUMN, prePos.getColumn());
                jsonObject.put(PRE_CARD_ID, prePos.getCardID());
                jsonObject.put(PRE_IMG_TYPE, prePos.getType());

                jsonObject.put(NEXT_ROW, nextPos.getRow());
                jsonObject.put(NEXT_COLUMN, nextPos.getColumn());
                jsonObject.put(NEXT_CARD_ID, nextPos.getCardID());
                jsonObject.put(NEXT_IMG_TYPE, nextPos.getType());
            }
            else if (type == Type.START || type == Type.FIRST || type == Type.SECOND) {
                JSONArray deckArray = new JSONArray();

                for(Integer each_card : deck) {
                    JSONObject each_object = new JSONObject();
                    each_object.put(EACH_CARD, each_card);
                    deckArray.put(each_object);
                }

                jsonObject.put(DECK, deckArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public Position getPrePos() {
        return prePos;
    }

    public void setPrePos(Position prePos) {
        this.prePos = prePos;
    }

    public Position getNextPos() {
        return nextPos;
    }

    public void setNextPos(Position nextPos) {
        this.nextPos = nextPos;
    }

    public List<Integer> getDeck() {
        return deck;
    }

    public enum Type {
        START, END, FIRST, TURN, SECOND, PLAY
    }


}
