package com.example.crystaldave.chatapp.models;


public class Request {

    private long date;

    public Request() {
    }

    public Request(long date) {

        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}
