package com.zsh.ricky.cardmanager.model;

import java.util.List;

/**
 * Created by Ricky on 2017/10/23.
 */

public class GameHistory {
    private int historyNum;
    private String playerA;
    private String playerB;
    private String winner;
    private String date;
    private String time;

    public GameHistory() {}

    public GameHistory(int historyNum, String playerA, String playerB, String winner, String date, String time) {
        this.historyNum = historyNum;
        this.playerA = playerA;
        this.playerB = playerB;
        this.winner = winner;
        this.date = date;
        this.time = time;
    }

    public int getHistoryNum() {
        return historyNum;
    }

    public void setHistoryNum(int historyNum) {
        this.historyNum = historyNum;
    }

    public String getPlayerA() {
        return playerA;
    }

    public void setPlayerA(String playerA) {
        this.playerA = playerA;
    }

    public String getPlayerB() {
        return playerB;
    }

    public void setPlayerB(String playerB) {
        this.playerB = playerB;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
