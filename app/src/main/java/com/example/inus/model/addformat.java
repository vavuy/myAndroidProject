package com.example.inus.model;

import java.io.Serializable;

public class addformat implements Serializable {
    public String name;
    public String price;

    public addformat(){
    }

    public addformat(String name,String price) {
        this.name = name;
        this.price = price;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
