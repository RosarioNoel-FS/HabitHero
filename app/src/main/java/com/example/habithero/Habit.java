package com.example.habithero;

import com.google.firebase.Timestamp;

import java.io.Serializable;

public class Habit implements Serializable {

    private String id; // Unique identifier
    private String name;
    private String category;
    private int icon;
    private int completionHour;
    private int completionMinute;

    private Timestamp timestamp; // Timestamp for when the habit was created


    // Default constructor for Firebase deserialization
    public Habit() {
        // Default values or empty constructor for Firebase
    }

    public Habit(String name, String category, int completionHour, int completionMinute) {
        this.name = name;
        this.category = category;
        this.completionHour = completionHour;
        this.completionMinute = completionMinute;
        // Assign default icon if not provided
        this.icon = DataHelper.getCategoryIcon(this.category);
    }

    public Habit(String name, String category, int icon) {
        this.name = name;
        this.category = category;
        this.icon = icon;
        // Default values for the hour and minute or some logic to assign them
        this.completionHour = 0; // Example default value
        this.completionMinute = 0; // Example default value
    }

    // Additional constructor to include the timestamp
    public Habit(String name, String category, int completionHour, int completionMinute, Timestamp timestamp) {
        this.name = name;
        this.category = category;
        this.completionHour = completionHour;
        this.completionMinute = completionMinute;
        this.timestamp = timestamp;
    }
    // Getters and Setters
    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }

    public String getCategory() {
        return category;
    }

    public int getCompletionHour() {
        return completionHour;
    }

    public int getCompletionMinute() {
        return completionMinute;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
