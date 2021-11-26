package com.techo.bikebuddy.Models;

public class JoinedUsersPojo {

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public JoinedUsersPojo(String userName) {
        this.userName = userName;
    }

    String userName;
}
