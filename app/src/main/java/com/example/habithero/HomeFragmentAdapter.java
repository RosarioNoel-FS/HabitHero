package com.example.habithero;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeFragmentAdapter extends RecyclerView.Adapter<HomeFragmentAdapter.HabitViewHolder> {

    private final List<Habit> habits;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public HomeFragmentAdapter(List<Habit> habits) {
        this.habits = habits;
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
        holder.bind(habit, listener);
    }

    @Override
    public int getItemCount() {
        return habits.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconImageView;
        private final TextView habitTextView;
        // Add additional views for completion stats, streaks, etc.

        public HabitViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageViewHome);
            habitTextView = itemView.findViewById(R.id.habitTextViewHome);
            // Initialize additional views
        }

        public void bind(Habit habit, final OnItemClickListener listener) {
            habitTextView.setText(habit.getName());
            iconImageView.setImageResource(habit.getIcon());
            // Set additional views with habit data

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(habit);
                }
            });
        }
    }

    public void addHabitAtTop(Habit habit) {
        this.habits.add(0, habit);
        notifyItemInserted(0);
    }

}
