package com.zsh.ricky.cardmanager.model;

/**
 * Created by Ricky on 2017/11/5.
 */

public class Position {

    private int row;
    private int column;
    private int cardID;   //存储当前位置卡牌在cardList中的位置
    private Type type;

    public static final int CARD_BACK_ID = -1;     //卡牌背面的ID

    public enum Type {
        CARD, LIFE, BUTTON, DECK
    }

    public Position(int row, int column, Type type) {
        this.row = row;
        this.column = column;
        this.type = type;
        this.cardID = -1;
    }

    public Position(int row, int column, int cardID, Type type) {
        this.row = row;
        this.column = column;
        this.cardID = cardID;
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    @Override
    public String toString() {
        return "Position{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
