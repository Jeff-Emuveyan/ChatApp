package com.example.jeffemuveyan.Classes;

public class Message {

    String message;
    String userName;
    String time;

    public Message(String message, String userName, String time) {
        this.message = message;
        this.userName = userName;
        this.time = time;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
