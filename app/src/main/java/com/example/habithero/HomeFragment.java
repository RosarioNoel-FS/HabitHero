package com.example.habithero;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
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
    private FirebaseHelper firebaseHelper;

    private ProgressBar progressBar;


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
        progressBar = view.findViewById(R.id.progressBar);
        ImageButton infoButton = view.findViewById(R.id.home_info_button);
        infoButton.setOnClickListener(v -> showInfoModal());


        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        homeAdapter = new HomeFragmentAdapter(new ArrayList<>(), this, progressBar);
        recyclerView.setAdapter(homeAdapter);

        fabAddHabit.setOnClickListener(v -> navigateToHabitCategorySelection());
        Log.d("HomeFragment", "navigateToHabitCategorySelection called");

        fetchUserHabitsAndUpdateUI();

        return view;
    }

    private void fetchUserHabitsAndUpdateUI() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseHelper = new FirebaseHelper();
        progressBar.setVisibility(View.VISIBLE);

        firebaseHelper.fetchUserHabits(userId, new FirebaseHelper.FirestoreCallback<List<Habit>>() {
            @Override
            public void onCallback(List<Habit> habits) {
                LocalDate today = LocalDate.now();

                for (Habit habit : habits) {
                    LocalDate lastCompletionDate = habit.getLastCompletionDate();

                    // Check if the habit's last completion date is not today
                    if (lastCompletionDate == null || !lastCompletionDate.isEqual(today)) {
                        habit.setCompleted(false);
                        firebaseHelper.updateCompletedHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Void>() {
                            @Override
                            public void onCallback(Void result) {
                                Log.d("HomeFragment", "Habit completion status reset for ID: " + habit.getId());
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("HomeFragment", "Error updating habit completion status: " + e.getMessage(), e);
                            }
                        });
                    }
                }

                Collections.sort(habits, (h1, h2) -> h2.getTimestamp().compareTo(h1.getTimestamp()));
                updateHabitListUI(habits);
                checkAndShowFirstHabitModal(userId, habits);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Log.e("HomeFragment", "Error fetching habits: " + e.getMessage(), e);
                progressBar.setVisibility(View.GONE);
            }
        });
    }



    private void updateHabitListUI(List<Habit> habits) {
        homeAdapter = new HomeFragmentAdapter(habits, this, progressBar);
        recyclerView.setAdapter(homeAdapter);
        updateUIBasedOnHabits(habits);
    }

    public void updateUIBasedOnHabits(List<Habit> habits) {
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

        homeAdapter.setOnItemClickListener(new HomeFragmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Habit habit) {
                openDetailFragment(habit);
            }
        });
    }

    private void navigateToHabitCategorySelection() {
        HabitCategorySelectionFragment habitCategorySelectionFragment = new HabitCategorySelectionFragment();
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, habitCategorySelectionFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    private void openDetailFragment(Habit habit) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString("habitId", habit.getId()); // Ensure habitId is being set here

        args.putString("habitName", habit.getName());
        args.putString("iconUrl", habit.getIconUrl());
        args.putLong("timestampMillis", habit.getTimestamp().toDate().getTime()); // Convert Timestamp to milliseconds
        args.putInt("completionCount", habit.getCompletionCount());
        args.putInt("streakCount", habit.getStreakCount());
        args.putBoolean("isCompleted", habit.getCompleted());
        detailFragment.setArguments(args);


        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();

    }

    private void checkAndShowFirstHabitModal(String userId, List<Habit> habits) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean hasSeenFirstHabitModal = documentSnapshot.getBoolean("hasSeenFirstHabitModal");
                if (hasSeenFirstHabitModal == null || !hasSeenFirstHabitModal) {
                    if (!habits.isEmpty()) {
                        showInfoModal();
                        db.collection("users").document(userId)
                                .update("hasSeenFirstHabitModal", true);
                    }
                }
            }
        }).addOnFailureListener(e -> Log.e("HomeFragment", "Error fetching user data: " + e.getMessage()));
    }

    private void showInfoModal() {
        ModalInfoFragment modalFragment = new ModalInfoFragment();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, modalFragment)
                .addToBackStack(null)
                .commit();
    }
}
