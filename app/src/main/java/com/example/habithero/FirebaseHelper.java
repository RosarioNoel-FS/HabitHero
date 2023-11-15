package com.example.habithero;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

    public class FirebaseHelper {

        private FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Method to fetch all habits for a specific user
        public void fetchUserHabits(String userId, final FirestoreCallback<List<Habit>> callback) {
            db.collection("users").document(userId).collection("habits")
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Order by creation timestamp, newest first
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            List<Habit> habits = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Habit habit = document.toObject(Habit.class);
                                habits.add(habit);
                            }
                            callback.onCallback(habits);
                        } else {
                            // Handle the error
                            callback.onError(task.getException());
                        }
                    });
        }

        // Method to add a new habit for a user
        public void addHabit(String userId, Habit habit, final FirestoreCallback<Void> callback) {
            // Set the current timestamp as the createdAt value
            habit.setTimestamp(new Timestamp(new Date()));
            db.collection("users").document(userId).collection("habits")
                    .add(habit)
                    .addOnSuccessListener(documentReference -> callback.onCallback(null))
                    .addOnFailureListener(e -> callback.onError(e));
        }

        // Method to update an existing habit
        public void updateHabit(String userId, Habit habit, final FirestoreCallback<Void> callback) {
            db.collection("users").document(userId).collection("habits").document(habit.getId())
                    .set(habit)
                    .addOnSuccessListener(aVoid -> callback.onCallback(null))
                    .addOnFailureListener(e -> callback.onError(e));
        }

        // Method to delete a habit
        public void deleteHabit(String userId, String habitId, final FirestoreCallback<Void> callback) {
            db.collection("users").document(userId).collection("habits").document(habitId)
                    .delete()
                    .addOnSuccessListener(aVoid -> callback.onCallback(null))
                    .addOnFailureListener(e -> callback.onError(e));
        }

        // Callback interface for Firestore operations
        public interface FirestoreCallback<T> {
            void onCallback(T result);
            void onError(Exception e);
        }
    }

