package com.example.flipcard;

import android.graphics.Bitmap;

public class Card {
    private Bitmap img;
    private String name,date,rank,scr;
    private int bg;

    public Card(Bitmap n,String s1, String s2,String s3,String s4,int c){
        this.img = n;
        this.name = s1;
        this.date = s2;
        this.rank = s3;
        this.scr = s4;
        this.bg = c;
    }

    public Bitmap getImg(){
        return img;
    }

    public String getName(){
        return name;
    }

    public String getDate(){
        return date;
    }

    public String getRank(){
        return rank;
    }
    public int getColor(){
        return bg;
    }
    public String getScr(){ return scr; }
}
