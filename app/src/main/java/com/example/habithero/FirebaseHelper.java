package com.example.habithero;


import android.net.Uri;
import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {

        private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();




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

    public void loadSpecificCategoryData(String categoryName) {
        db.collection("Category") // Replace with your collection path
                .whereEqualTo("name", categoryName) // Use the field that identifies your category
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                String name = document.getString("name");
                                List<String> habitList = (List<String>) document.get("habit_list");

                                // Do something with the data, e.g., display in UI
                                Log.d("Specific Category", "Category Name: " + name + ", Habits: " + habitList);
                            } else {
                                Log.d("Error", "No such category");
                            }
                        }
                    } else {
                        Log.w("Error", "Error getting documents: ", task.getException());
                    }
                });
    }

    public void fetchCategories(FirestoreCallback<Map<String, Category>> callback) {
        db.collection("Category")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Category> categories = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String categoryName = document.getString("name");
                            List<String> habitList = (List<String>) document.get("habit_list");
                            String iconPath = document.getString("icon"); // Fetch the path


                            categories.put(categoryName, new Category(categoryName, habitList, iconPath));
                        }
                        callback.onCallback(categories);
                    } else {
                        Log.e("FirebaseHelper", "Error fetching categories: " + task.getException().getMessage(), task.getException());
                        callback.onError(task.getException());
                    }
                });
    }





    public void fetchIconUrlForCategory(String category, FirestoreCallback<String> callback) {
        db.collection("Category")
                .whereEqualTo("name", category)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String iconUrl = queryDocumentSnapshots.getDocuments().get(0).getString("icon");//
                        Log.d("FirebaseHelper", "Fetched icon URL: " + iconUrl); // Added log statement
                        callback.onCallback(iconUrl);
                    } else {
                        callback.onError(new Exception("Category not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "2 Error fetching icon URL for category: " + e.getMessage(), e); // Added log statement
                    callback.onError(e);
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
    public void updateCompletedHabit(String userId, Habit habit, final FirestoreCallback<Void> callback) {
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





    // Callback interface for Firestore operations
    public interface FirestoreCallback<T> {
        void onCallback(T result);
        void onError(Exception e);
    }


    public void saveProfileImage(String userId, byte[] imageData, FirestoreCallback<Void> callback) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference profileImageRef = storageRef.child("profile_images/" + userId + ".png");

        profileImageRef.putBytes(imageData)
                .addOnSuccessListener(taskSnapshot -> {
                    // Update Firestore document with the URL of the uploaded image
                    profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImageUrl", uri.toString());
                        db.collection("users").document(userId).update(updates);
                    });
                    callback.onCallback(null);
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    public void loadProfileImage(String userId, FirestoreCallback<Uri> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profileImageUrl")) {
                        String imageUrl = documentSnapshot.getString("profileImageUrl");
                        callback.onCallback(Uri.parse(imageUrl));
                    } else {
                        callback.onError(new Exception("Profile image not found"));
                    }
                })
                .addOnFailureListener(e -> callback.onError(e));
    }

    public void updateProfileImageUrl(String userId, Uri newImageUrl, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> updates = new HashMap<>();
        updates.put("profileImageUrl", newImageUrl.toString());

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onCallback(null))
                .addOnFailureListener(callback::onError);
    }

    //ToDO - Fetch Habits through firestore
    // Method to fetch predefined habits by category
    public void fetchPredefinedHabitsByCategory(String categoryName, FirestoreCallback<List<Habit>> callback) {
        db.collection("predefinedHabits").document(categoryName).collection("habits")
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
                        Log.e("FirebaseHelper", "Error fetching predefined habits: " + task.getException().getMessage(), task.getException());
                        callback.onError(task.getException());
                    }
                });
    }

    public void updateUsername(String userId, String newUsername, final FirestoreCallback<Void> callback) {
        db.collection("users").document(userId)
                .update("username", newUsername)
                .addOnSuccessListener(aVoid -> {
                    Log.d("FirebaseHelper", "Username updated successfully.");
                    callback.onCallback(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error updating username: " + e.getMessage(), e);
                    callback.onError(e);
                });
    }

    // Method to load user data
    public void loadUserData(String userId, FirestoreCallback<User> callback) {
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        callback.onCallback(user);
                    } else {
                        callback.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseHelper", "Error loading user data: " + e.getMessage(), e);
                    callback.onError(e);
                });
    }


}

