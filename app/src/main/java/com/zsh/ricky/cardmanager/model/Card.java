package com.zsh.ricky.cardmanager.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by Ricky on 2017/11/4.
 */

public class Card {

    private int cardID;
    private String cardName;
    private String cardPhotoName;
    private int cardHP;
    private int cardAttack;
    private int cardType;
    private Bitmap cardPhoto;

    public Card(int cardID, String cardName, String cardPhotoName, int cardHP, int cardAttack, int cardType) {
        this.cardID = cardID;
        this.cardName = cardName;
        this.cardPhotoName = cardPhotoName;
        this.cardHP = cardHP;
        this.cardAttack = cardAttack;
        this.cardType = cardType;
    }

    public int getCardID() {
        return cardID;
    }

    public void setCardID(int cardID) {
        this.cardID = cardID;
    }

    public String getCardName() {
        return cardName;
    }

    public void setCardName(String cardName) {
        this.cardName = cardName;
    }

    public String getCardPhotoName() {
        return cardPhotoName;
    }

    public void setCardPhotoName(String cardPhotoName) {
        this.cardPhotoName = cardPhotoName;
    }

    public int getCardHP() {
        return cardHP;
    }

    public void setCardHP(int cardHP) {
        this.cardHP = cardHP;
    }

    public int getCardAttack() {
        return cardAttack;
    }

    public void setCardAttack(int cardAttack) {
        this.cardAttack = cardAttack;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public Bitmap getCardPhoto() {
        return cardPhoto;
    }

    public void setCardPhoto(Bitmap cardPhoto) {
        this.cardPhoto = cardPhoto;
    }

}
