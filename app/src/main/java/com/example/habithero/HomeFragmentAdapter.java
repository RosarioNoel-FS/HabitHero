package com.example.habithero;

import android.app.AlertDialog;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.HabitViewHolder> {

    private ProgressBar progressBar;
    private List<Habit> habits;
    private OnItemClickListener listener;
    private FirebaseHelper firebaseHelper = new FirebaseHelper();
    private HomeFragment homeFragment;


    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public interface HabitViewHolderCallback {
        void onDeleteHabit(int position);
        void onUpdateHabit(int position, Habit updatedHabit); // Added this method
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item_home, parent, false);
        return new HabitViewHolder(itemView,progressBar);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HomeFragmentAdapter(List<Habit> habits, HomeFragment homeFragment,ProgressBar progressBar) {
        this.habits = new ArrayList<>(habits);
        this.homeFragment = homeFragment;
        this.progressBar = progressBar;

    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.habitCheckBox.setImageResource(habit.getCompleted() ? R.drawable.checked_box : R.drawable.unchecked_box);
        holder.bind(habit, listener, firebaseHelper, FirebaseAuth.getInstance().getCurrentUser().getUid(), position, new HabitViewHolderCallback() {
            public void onDeleteHabit(int position) {
                if (!habits.isEmpty() && position >= 0 && position < habits.size()) {
                    habits.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged(); // Notify the adapter that data set has changed

                    // Check if list is empty to update UI
                    if (habits.isEmpty() && homeFragment != null) {
                        homeFragment.updateUIBasedOnHabits(habits);
                    }

                    Log.d("HomeFragmentAdapter", "Habit at position " + position + " deleted.");
                } else {
                    Log.e("HomeFragmentAdapter", "Attempted to delete item at invalid position: " + position);
                }
            }




            @Override
            public void onUpdateHabit(int pos, Habit updatedHabit) {
                habits.set(pos, updatedHabit);
                notifyItemChanged(pos);
            }
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && position != RecyclerView.NO_POSITION) {
                Habit selectedHabit = habits.get(position);
                listener.onItemClick(selectedHabit);
            }
        });
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public void updateData(List<Habit> newHabits) {
        this.habits = new ArrayList<>(newHabits);
        notifyDataSetChanged();
    }

    public void addHabitAtTop(Habit habit) {
        this.habits.add(0, habit);
        notifyItemInserted(0);
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconImageView;
        private final TextView habitTextView;
        private final ImageView habitCheckBox;
        private final ProgressBar progressBar;

        public HabitViewHolder(View itemView, ProgressBar progressBar) {
            super(itemView);
            this.progressBar = progressBar;
            iconImageView = itemView.findViewById(R.id.iconImageViewHome);
            habitTextView = itemView.findViewById(R.id.habitTextViewHome);
            habitCheckBox = itemView.findViewById(R.id.habitCheckBox);

        }

        public void bind(Habit habit, final OnItemClickListener listener, FirebaseHelper firebaseHelper, String userId, int position, HabitViewHolderCallback callback) {
            habitTextView.setText(habit.getName());

            // Load the icon using Glide
            Glide.with(itemView.getContext())
                    .load(habit.getIconUrl())
                    .placeholder(R.drawable.default_icon) // Default icon in case of failure or while loading
                    .into(iconImageView);

            Log.d("HomeFragmentAdapter", "Loading habit icon URL: " + habit.getIconUrl()); // Added log statement

            habitCheckBox.setImageResource(habit.getCompleted() ? R.drawable.checked_box : R.drawable.unchecked_box);

            habitCheckBox.setOnClickListener(view -> {
                AlertDialog dialog = new AlertDialog.Builder(view.getContext())
                        .setTitle("Manage Habit")
                        .setMessage("Choose your action for this habit:")
                        .setPositiveButton("Complete", null)
                        .setNeutralButton("Delete", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button completeButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    Button deleteButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
                    Button cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    if (completeButton != null) {
                        completeButton.setOnClickListener(innerView -> {
                            new AlertDialog.Builder(view.getContext())
                                    .setTitle("Complete Habit")
                                    .setMessage("Have you completed your habit for the day?")
                                    .setPositiveButton("Yes", (confirmDialog, confirmWhich) -> {
                                        habit.completeHabit();
                                        firebaseHelper.updateCompletedHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Void>() {
                                            @Override
                                            public void onCallback(Void result) {
                                                Log.d("HomeFragmentAdapter", "Habit updated successfully in Firestore.");
                                                progressBar.setVisibility(View.GONE); // Hide progress bar on success
                                                callback.onUpdateHabit(position, habit);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e("HomeFragmentAdapter", "Error updating habit in Firestore: " + e.getMessage(), e);
                                                progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                            }
                                        });
                                        dialog.dismiss();
                                    })
                                    .setNegativeButton("No", null)
                                    .show();
                        });
                    }

                    if (deleteButton != null) {
                        deleteButton.setTextColor(Color.RED);
                        deleteButton.setOnClickListener(innerView -> {
                            new AlertDialog.Builder(view.getContext())
                                    .setTitle("Delete Habit")
                                    .setMessage("Are you sure you want to delete this habit? This action cannot be undone.")
                                    .setPositiveButton("Yes, Delete", (confirmDialog, confirmWhich) -> {
                                        firebaseHelper.deleteHabit(userId, habit.getId(), new FirebaseHelper.FirestoreCallback<Void>() {
                                            @Override
                                            public void onCallback(Void result) {
                                                Log.d("HomeFragmentAdapter", "Habit deleted successfully.");
                                                progressBar.setVisibility(View.GONE); // Hide progress bar on success
                                                callback.onDeleteHabit(position);
                                            }

                                            @Override
                                            public void onError(Exception e) {
                                                Log.e("HomeFragmentAdapter", "Error deleting habit: " + e.getMessage(), e);
                                                progressBar.setVisibility(View.GONE); // Hide progress bar on failure
                                            }
                                        });
                                        dialog.dismiss();
                                    })
                                    .setNegativeButton("Cancel", null)
                                    .show();
                        });
                    }

                    if (cancelButton != null) {
                        cancelButton.setOnClickListener(innerView -> dialog.dismiss());
                    }
                });

                dialog.show();
            });

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(habit);
                }
            });
        }


    }
}
