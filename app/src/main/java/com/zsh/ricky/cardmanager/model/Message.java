package com.zsh.ricky.cardmanager.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Ricky on 2017/11/5.
 */

public class Message {

    private Type type;
    private Position position;

    private static final String MSG_TYPE = "message_type";
    private static final String PS_ROW = "position_row";
    private static final String PS_COLUMN = "position_column";
    private static final String PS_IMG_TYPE = "position_img_type";
    private static final String PS_CARD_ID = "position_card_id";

    public Message() {}

    public Message(Type type, Position position) {
        this.type = type;
        this.position = position;
    }

    /**
     * 根据json对象初始化message
     * @param json json对象对应字符串
     */
    public Message(String json) {

        try {
            JSONObject jsonObject = new JSONObject(json);

            this.type = (Type) jsonObject.get(MSG_TYPE);
            this.position = new Position(
                    jsonObject.getInt(PS_ROW),
                    jsonObject.getInt(PS_COLUMN),
                    (Position.Type)jsonObject.get(PS_IMG_TYPE)
            );
            this.position.setCardID(jsonObject.getInt(PS_CARD_ID));
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
            jsonObject.put(PS_ROW, position.getRow());
            jsonObject.put(PS_COLUMN, position.getColumn());
            jsonObject.put(PS_IMG_TYPE, position.getType());
            jsonObject.put(PS_CARD_ID, position.getCardID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        FIND, START, END, GAME, DECK, WAIT
    }
}
