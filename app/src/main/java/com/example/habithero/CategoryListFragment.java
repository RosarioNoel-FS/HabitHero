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

public class CategoryListFragment extends Fragment implements HabitPreferenceDialogFragment.HabitAddListener {

    private RecyclerView recyclerView;
    private HabitCategoryAdapter habitAdapter;
    private String category;
    private TextView titleTextView;


    @Override //receives habit category name and habit list from HabitCategorySelectionFragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.category_habit_selection_fragment, container, false);


        if (getArguments() != null) {
            category = getArguments().getString("category");
            if (category == null || category.trim().isEmpty()) {
                Log.e("CategoryListFragment", "Category is null or empty");
                // Handle error case here
                return view;
            }
        } else {
            Log.e("CategoryListFragment", "No arguments found");
            // Handle error case here
            return view;
        }

        List<String> habitNames = DataHelper.getHabitsByCategory().get(category);
        if (habitNames == null) {
            Log.e("CategoryListFragment", "No habits found for category: " + category);
            // Handle error case here
            return view;
        }

        int categoryIcon = DataHelper.getCategoryIcon(category);
        List<Habit> habits = new ArrayList<>();
        for (String habitName : habitNames) {
            habits.add(new Habit(habitName, category, categoryIcon));
        }
        //set up UI
        titleTextView = view.findViewById(R.id.titleTextView);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        habitAdapter = new HabitCategoryAdapter(habits);
        habitAdapter.setOnItemClickListener(this::showHabitPreferenceDialog);
        recyclerView.setAdapter(habitAdapter);

        //set title to category name
        titleTextView.setText(category);

        return view;
    }




    private void showHabitPreferenceDialog(Habit habit) {
        HabitPreferenceDialogFragment dialogFragment = HabitPreferenceDialogFragment.newInstance(habit.getName(), habit.getCategory());
        dialogFragment.setTargetFragment(this, 0); // Set this fragment as the target for results
        dialogFragment.show(getParentFragmentManager(), "HabitPreferenceDialog");
    }

    @Override
    public void onHabitAdded() {
        // Handle the navigation to the HomeFragment here
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToHomeFragment();
        }
    }
}
