package com.example.habithero;

public class HabitCategory {
    private String name;
    private int imageResourceId;

    public HabitCategory(String name, int imageResourceId) {
        this.name = name;
        this.imageResourceId = imageResourceId;
    }

    public String getName() {
        return name;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
