package com.example.habithero;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CategoryListFragment extends Fragment {

    private RecyclerView recyclerView;
    private HabitCategoryAdapter habitAdapter;
    private String category;
    private TextView titleTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_habit_selection_fragment, container, false);

        if (getArguments() != null) {
            category = getArguments().getString("category");
            List<String> habitNames = getArguments().getStringArrayList("habitsList");
            String iconUrl = getArguments().getString("iconUrl");

            if (category == null || category.trim().isEmpty() || habitNames == null || iconUrl == null) {
                Log.e("CategoryListFragment", "Category, iconUrl or habitsList is null or empty");
                return view;
            }

            List<Habit> habits = new ArrayList<>();
            for (String habitName : habitNames) {
                int defaultCompletionHour = 0;
                int defaultCompletionMinute = 0;
                String defaultFrequency = "defaultFrequency"; // Default or placeholder value
                int defaultDailyCompletionTarget = 1; // Default or placeholder value
                List<String> defaultReminderTimes = new ArrayList<>(); // Default or placeholder value

                habits.add(new Habit(habitName, category, defaultCompletionHour, defaultCompletionMinute, iconUrl, defaultFrequency, defaultDailyCompletionTarget, defaultReminderTimes));
            }

            titleTextView = view.findViewById(R.id.titleTextView);
            recyclerView = view.findViewById(R.id.recyclerView);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            habitAdapter = new HabitCategoryAdapter(habits);
            habitAdapter.setOnItemClickListener(this::showHabitPreferenceDialog);
            recyclerView.setAdapter(habitAdapter);

            titleTextView.setText(category);
        } else {
            Log.e("CategoryListFragment", "No arguments found");
            return view;
        }

        return view;
    }

    private void showHabitPreferenceDialog(Habit habit) {
        HabitPreferenceDialogFragment dialogFragment = HabitPreferenceDialogFragment.newInstance(habit.getName(), habit.getCategory());
        dialogFragment.setHabitAddListener((HabitPreferenceDialogFragment.HabitAddListener) getActivity());
        dialogFragment.show(getParentFragmentManager(), "HabitPreferenceDialog");
    }
}
