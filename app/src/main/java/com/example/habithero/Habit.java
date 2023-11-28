package com.example.habithero;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class Habit implements Serializable {

    private transient String id; // Mark as transient to exclude from serialization
    private String name;
    private String category;
    private String iconUrl; // Changed from int icon to String iconUrl
    private int completionHour; // Deadline hour
    private int completionMinute; // Deadline minute
    private boolean completed; // Is the habit completed?

    private Timestamp timestamp; // Timestamp for when the habit was created

    private int streakCount; // Count of consecutive days habit is completed
    private int completionCount; // Total count of habit completions

    // Default constructor for Firebase deserialization
    public Habit() {
        // Default values or empty constructor for Firebase
    }

    // Constructor for creating a new habit
    // Constructor for creating a new habit
    public Habit(String name, String category, int completionHour, int completionMinute, String iconUrl) {
        this.name = name;
        this.category = category;
        this.iconUrl = iconUrl; // Assign the icon URL
        this.completionHour = completionHour;
        this.completionMinute = completionMinute;
        this.timestamp = new Timestamp(new Date()); // Set default timestamp
        this.completed = false; // Set default completion status
        this.streakCount = 0;
        this.completionCount = 0;
    }

    // Increment streak count
    public void incrementStreakCount() {
        streakCount++;
        completionCount++;
    }

    // Reset streak count
    public void resetStreakCount() {
        streakCount = 0;
        completionCount++;
    }

    // Check if habit is completed before the deadline
    private boolean isCompletedBeforeDeadline() {
        Calendar deadline = Calendar.getInstance();
        deadline.set(Calendar.HOUR_OF_DAY, completionHour);
        deadline.set(Calendar.MINUTE, completionMinute);
        deadline.set(Calendar.SECOND, 0);
        deadline.set(Calendar.MILLISECOND, 0);

        // Adjust for next day if deadline has passed for today
        if (deadline.before(Calendar.getInstance())) {
            deadline.add(Calendar.DATE, 1);
        }

        return Calendar.getInstance().before(deadline);
    }


    // Update habit completion and streak
    // In Habit class
    public void completeHabit() {
        if (isCompletedBeforeDeadline()) {
            incrementStreakCount();
        } else {
            resetStreakCount();
        }
        setCompleted(true); // Mark the habit as completed
    }

    // Getters and Setters
    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCompletionCount() {
        return completionCount;
    }

    public void setCompletionCount(int completionCount) {
        this.completionCount = completionCount;
    }

    public int getStreakCount() {
        return streakCount;
    }

    public void setStreakCount(int streakCount) {
        this.streakCount = streakCount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
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

    public String getName() {
        return name;
    }
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }
}
