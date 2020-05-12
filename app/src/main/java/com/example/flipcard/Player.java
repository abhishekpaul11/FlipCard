package com.example.flipcard;

public class Player {
    private String name,date,hs;
    private boolean self;
    private int id;

    public Player(String n, String d, String h,boolean b,int i){
        this.name = n;
        this.date = d;
        this.hs = h;
        this.self = b;
        this.id = i;
    }

    public String getName(){
        return name;
    }
    public String getDate(){
        return date;
    }
    public String getHs(){
        return hs;
    }
    public boolean getSelf(){
        return self;
    }
    public int getId(){ return id; }
}
