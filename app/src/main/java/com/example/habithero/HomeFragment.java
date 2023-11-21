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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.lang.Exception;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddHabit;
    private ImageView imageViewCreateHabitInstruction;
    private ImageView imageViewArrowToFAB;
    private TextView createFirstHabitText;
    private ImageView consistencyText;
    private HomeFragmentAdapter homeAdapter;
    private FirebaseHelper firebaseHelper;

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

        if (getArguments() != null && getArguments().getSerializable("newHabit") != null) {
            Habit newHabit = (Habit) getArguments().getSerializable("newHabit");
            updateHabitList(newHabit);
        } else {
            fetchUserHabitsAndUpdateUI();
        }

        return view;
    }

    public void updateHabitList(Habit newHabit) {
        Log.d("HomeFragment", "Updating habit list with Habit ID: " + newHabit.getId());
        if (homeAdapter != null) {
            homeAdapter.addHabitAtTop(newHabit);
            recyclerView.scrollToPosition(0);
        }
    }

    private void fetchUserHabitsAndUpdateUI() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchUserHabits(userId, new FirebaseHelper.FirestoreCallback<List<Habit>>() {
            @Override
            public void onCallback(List<Habit> habits) {
                Calendar now = Calendar.getInstance();

                // Process each habit for streak reset
                for (Habit habit : habits) {
                    Calendar deadline = Calendar.getInstance();
                    deadline.set(Calendar.HOUR_OF_DAY, habit.getCompletionHour());
                    deadline.set(Calendar.MINUTE, habit.getCompletionMinute());
                    deadline.set(Calendar.SECOND, 0);
                    deadline.set(Calendar.MILLISECOND, 0);

                    // Adjust for the next day if the deadline has passed for today
                    if (deadline.before(now)) {
                        deadline.add(Calendar.DATE, 1);
                    }

                    // Reset streak if it's a new day and the habit wasn't completed
                    if (now.after(deadline) && !habit.getCompleted()) {
                        habit.resetStreakCount();
                        firebaseHelper.updateHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onCallback(Void result) {
                                Log.d("HomeFragment", "Streak count reset for habit ID: " + habit.getId());
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("HomeFragment", "Error resetting streak count: " + e.getMessage(), e);
                            }
                        });
                    }
                }

                // Sort the habits and update UI
                Collections.sort(habits, (h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()));
                homeAdapter = new HomeFragmentAdapter(habits);
                recyclerView.setAdapter(homeAdapter);
                updateUIBasedOnHabits(habits);
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeFragment", "Error fetching habits: " + e.getMessage(), e);
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

    public void addNewHabit(Habit newHabit) {
        if (homeAdapter != null) {
            homeAdapter.addHabitAtTop(newHabit);
            recyclerView.scrollToPosition(0);
        }
    }



}
