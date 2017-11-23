package com.zsh.ricky.cardmanager.model;

/**
 * Created by Ricky on 2017/11/5.
 */

public class Position {

    private int row;
    private int column;
    private int cardPosition;   //存储当前位置卡牌在allCard中的位置
    private Type type;

    public static final int CARD_BACK_ID = -1;     //卡牌背面的ID

    public enum Type {
        CARD, LIFE, BUTTON, DECK, CARD_BACK;
    }

    public Position(int row, int column, Type type) {
        this.row = row;
        this.column = column;
        this.type = type;
        this.cardPosition = CARD_BACK_ID;
    }

    public Position(int row, int column, int cardID, Type type) {
        this.row = row;
        this.column = column;
        this.cardPosition = cardID;
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

    public int getCardPosition() {
        return cardPosition;
    }

    public void setCardPosition(int cardPosition) {
        this.cardPosition = cardPosition;
    }

    @Override
    public String toString() {
        return "Position{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
