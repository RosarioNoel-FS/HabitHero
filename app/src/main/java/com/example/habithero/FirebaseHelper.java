package com.example.habithero;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Method to fetch all habits for a specific user
        public void fetchUserHabits(String userId, final FirestoreCallback<List<Habit>> callback) {
            db.collection("users").document(userId).collection("habits")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Habit> habits = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Habit habit = document.toObject(Habit.class);
                                habit.setId(document.getId());
                                habits.add(habit);
                                Log.d("FirebaseHelper", "Fetched Habit ID: " + habit.getId() + ", Name: " + habit.getName());
                            }
                            callback.onCallback(habits);
                        } else {
                            Log.e("FirebaseHelper", "Error fetching habits: " + task.getException().getMessage(), task.getException());
                            callback.onError(task.getException());
                        }
                    });
        }




        // Method to add a new habit for a user
        public void addHabit(String userId, Habit habit, final FirestoreCallback<Habit> callback) {
            // Set initial values for the habit
            habit.setTimestamp(new Timestamp(new Date()));
            habit.setCompleted(false); // Using 'completed' field

            db.collection("users").document(userId).collection("habits")
                    .add(habit)
                    .addOnSuccessListener(documentReference -> {
                        // Fetch the document ID and set it as the habit's ID
                        String habitId = documentReference.getId();
                        habit.setId(habitId);

                        Log.d("FirebaseHelper", "Habit successfully added to Firestore with ID: " + habitId);

                        // Pass the updated habit object to the callback
                        callback.onCallback(habit);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("FirebaseHelper", "Error adding habit: " + e.getMessage(), e);
                        callback.onError(e);
                    });
        }







    // Method to update an existing habit
    public void updateHabit(String userId, Habit habit, final FirestoreCallback<Void> callback) {
        Log.d("FirebaseHelper", "Updating habit with ID: " + habit.getId());

        // Create a map for updating specific fields
        Map<String, Object> updates = new HashMap<>();
        updates.put("completed", habit.getCompleted());
        updates.put("streakCount", habit.getStreakCount());
        updates.put("completionCount", habit.getCompletionCount());

        // Update only the specific fields in the document
        db.collection("users").document(userId).collection("habits").document(habit.getId())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Habit updated successfully: " + habit.getId());
                    callback.onCallback(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error updating habit: " + e.getMessage(), e);
                    callback.onError(e);
                });
    }




    // Method to delete a habit
        public void deleteHabit(String userId, String habitId, final FirestoreCallback<Void> callback) {
            db.collection("users").document(userId).collection("habits").document(habitId)
                    .delete()
                    .addOnSuccessListener(aVoid -> callback.onCallback(null))
                    .addOnFailureListener(e -> callback.onError(e));
        }

    public void updateHabitCompletion(String userId, String habitId, boolean isCompleted, FirestoreCallback<Void> callback) {
        db.collection("users").document(userId).collection("habits").document(habitId)
                .update("completed", isCompleted) // Use the isCompleted parameter here
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(e -> callback.onError(e));
    }



    // Callback interface for Firestore operations
        public interface FirestoreCallback<T> {
            void onCallback(T result);
            void onError(Exception e);
        }
    }
