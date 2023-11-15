package com.example.habithero;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddHabit;
    private ImageView imageViewCreateHabitInstruction;
    private ImageView imageViewArrowToFAB;
    private TextView createFirstHabitText;
    private ImageView consistencyText;
    private HomeFragmentAdapter homeAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        Log.d("HomeFragment", "onCreateView called");

        recyclerView = view.findViewById(R.id.rv_habits);
        fabAddHabit = view.findViewById(R.id.fab_add_habit);
        imageViewCreateHabitInstruction = view.findViewById(R.id.imageViewCreateHabitInstruction);
        imageViewArrowToFAB = view.findViewById(R.id.imageViewArrowToFAB);
        createFirstHabitText = view.findViewById(R.id.creat_first_habit_text);
        consistencyText = view.findViewById(R.id.consistency_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeAdapter = new HomeFragmentAdapter(new ArrayList<>());
        recyclerView.setAdapter(homeAdapter);

        fabAddHabit.setOnClickListener(v -> navigateToHabitCategorySelection());
        Log.d("HomeFragment", "navigateToHabitCategorySelection called");

        fetchUserHabitsAndUpdateUI();

        return view;
    }

    private void fetchUserHabitsAndUpdateUI() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchUserHabits(userId, new FirebaseHelper.FirestoreCallback<List<Habit>>() {
            @Override
            public void onCallback(List<Habit> habits) {
                Collections.sort(habits, (h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()));
                homeAdapter = new HomeFragmentAdapter(habits);
                recyclerView.setAdapter(homeAdapter);
                updateUIBasedOnHabits(habits);
            }

            @Override
            public void onError(Exception e) {
                // Handle the error
            }
        });
    }

    private void updateUIBasedOnHabits(List<Habit> habits) {
        if (habits.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            imageViewCreateHabitInstruction.setVisibility(View.VISIBLE);
            imageViewArrowToFAB.setVisibility(View.VISIBLE);
            createFirstHabitText.setVisibility(View.VISIBLE);
            consistencyText.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            imageViewCreateHabitInstruction.setVisibility(View.GONE);
            imageViewArrowToFAB.setVisibility(View.GONE);
            createFirstHabitText.setVisibility(View.GONE);
            consistencyText.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToHabitCategorySelection() {
        HabitCategorySelectionFragment habitCategorySelectionFragment = new HabitCategorySelectionFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, habitCategorySelectionFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
