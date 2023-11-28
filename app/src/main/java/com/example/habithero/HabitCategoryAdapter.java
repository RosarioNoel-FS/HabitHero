package com.example.habithero;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;


public class HabitCategoryAdapter extends RecyclerView.Adapter<HabitCategoryAdapter.HabitViewHolder> {

    private final List<Habit> habits;
    private HabitCategoryAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Habit habit);
    }

    public void setOnItemClickListener(HabitCategoryAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    public HabitCategoryAdapter(List<Habit> habits) {
        this.habits = habits;
    }

    @NonNull
    @Override
    public HabitCategoryAdapter.HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.habit_item, parent, false);
        return new HabitCategoryAdapter.HabitViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitCategoryAdapter.HabitViewHolder holder, int position) {
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

        public HabitViewHolder(View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            habitTextView = itemView.findViewById(R.id.habitTextView);
        }

        public void bind(Habit habit, final HabitCategoryAdapter.OnItemClickListener listener) {
            habitTextView.setText(habit.getName());
            Glide.with(itemView.getContext())
                    .load(habit.getIconUrl())
                    .into(iconImageView);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onItemClick(habit);
                }
            });
        }

    }
}
