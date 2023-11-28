package com.example.habithero;

import java.util.List;

public class Category {
    private String name;
    private List<String> habitList;
    private String iconUrl;

    public Category(String name, List<String> habitList, String iconUrl) {
        this.name = name;
        this.habitList = habitList;
        this.iconUrl = iconUrl;
    }

    // Getters
    public String getName() { return name; }
    public List<String> getHabitList() { return habitList; }
    public String getIconUrl() { return iconUrl; }
}
