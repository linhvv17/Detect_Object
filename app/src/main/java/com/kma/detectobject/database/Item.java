package com.kma.detectobject.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("mean")
    @Expose
    private String mean;
    @SerializedName("path")
    @Expose
    private String path;


    public Item(){

    }

    public Item(int id, String name, String mean, String path) {
        this.id = id;
        this.name = name;
        this.mean = mean;
        this.path = path;
    }

    public Item(String name, String mean, String path) {
        this.name = name;
        this.mean = mean;
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMean() {
        return mean;
    }

    public void setMean(String mean) {
        this.mean = mean;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
