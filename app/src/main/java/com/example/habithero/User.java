package com.example.habithero;

import java.io.Serializable;

public class User implements Serializable {
    private String username;
    // other fields if needed

    // Constructor
    public User() {
        // Empty constructor needed for Firestore deserialization
    }

    // Getters and setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
