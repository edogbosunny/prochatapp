package com.example.crystaldave.chatapp.models;

public class Friend {

    private long date;
    private String name;


    public Friend(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}