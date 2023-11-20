package com.example.habithero;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.HabitViewHolder> {

    private List<Habit> habits;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HomeFragmentAdapter(List<Habit> habits) {
        this.habits = new ArrayList<>(habits);
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item_home, parent, false);
        return new HabitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habits.get(position);
        holder.iconImageView.setImageResource(habit.getIcon()); // Set icon based on habit

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        holder.bind(habit, listener, firebaseHelper, userId);
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

        public HabitViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageViewHome);
            habitTextView = itemView.findViewById(R.id.habitTextViewHome);
            habitCheckBox = itemView.findViewById(R.id.habitCheckBox);
            // Initialize additional views
        }

        public void bind(Habit habit, final OnItemClickListener listener, FirebaseHelper firebaseHelper, String userId) {
            habitTextView.setText(habit.getName());
            iconImageView.setImageResource(habit.getIcon());
            habitCheckBox.setImageResource(habit.getCompleted() ? R.drawable.checked_box : R.drawable.unchecked_box);

            // Set up the click listener for the checkbox
            habitCheckBox.setOnClickListener(v -> {
                Log.d("HomeFragmentAdapter", "Checkbox clicked for habit ID: " + habit.getId());
                if (!habit.getCompleted() && habit.getId() != null) {
                    new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Habit Completion")
                            .setMessage("Did you complete the habit today?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                Calendar deadline = Calendar.getInstance();
                                deadline.set(Calendar.HOUR_OF_DAY, habit.getCompletionHour());
                                deadline.set(Calendar.MINUTE, habit.getCompletionMinute());
                                deadline.set(Calendar.SECOND, 0);
                                deadline.set(Calendar.MILLISECOND, 0);

                                Calendar now = Calendar.getInstance();

                                if (now.before(deadline)) {
                                    habit.incrementStreakCount();
                                    Log.d("HomeFragmentAdapter", "Habit completed before deadline. Streak incremented to: " + habit.getStreakCount());
                                } else {
                                    habit.resetStreakCount();
                                    Log.d("HomeFragmentAdapter", "Habit completed after deadline. Streak reset.");
                                }

                                habit.setCompleted(true);
                                habitCheckBox.setImageResource(R.drawable.checked_box);

                                // Update the habit in Firestore
                                firebaseHelper.updateHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Void>() {
                                    @Override
                                    public void onCallback(Void result) {
                                        Log.d("HomeFragmentAdapter", "Habit updated successfully in Firestore.");
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        Log.e("HomeFragmentAdapter", "Error updating habit in Firestore: " + e.getMessage(), e);
                                    }
                                });
                            })
                            .setNegativeButton("No", null)
                            .show();
                } else {
                    Log.d("HomeFragmentAdapter", "Habit already completed or ID is null.");
                }
            });



            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(habit);
                }
            });
        }
    }
}
