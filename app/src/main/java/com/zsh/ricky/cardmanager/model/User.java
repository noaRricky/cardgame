package com.zsh.ricky.cardmanager.model;

public class User {
    private String userID;    //用户ID
    private String userName;    //用户名
    private String password;    //密码
    private int type;           //用户类型：管理员0， 玩家1

    public User(){}

    public User(String userID, String userName, String password, int type) {
        this.userID = userID;
        this.userName = userName;
        this.password = password;
        this.type = type;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return userID != null ? userID.equals(user.userID) : user.userID == null;
    }

    @Override
    public int hashCode() {
        return userID != null ? userID.hashCode() : 0;
    }
}
