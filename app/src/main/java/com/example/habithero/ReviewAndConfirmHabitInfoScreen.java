package com.example.habithero;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;

import java.util.List;

public class ReviewAndConfirmHabitInfoScreen extends Fragment {

    private TextView tvHabitName, tvFrequency, tvDays, tvTime, tvReminder;
    private Button btnEditHabit, btnConfirmHabit;

    // Variables to store the habit details
    private String habitName, category, frequency, days, time;
    private List<String> reminderTimes; // If there are multiple reminders
    private String iconUrl; // URL for the category icon

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.review_and_confirm_habitinfo_screen, container, false);

        initializeViews(view);
        extractArguments();
        displayHabitDetails();

        btnEditHabit.setOnClickListener(v -> navigateToEditHabitScreen());
        btnConfirmHabit.setOnClickListener(v -> onConfirmClicked());

        return view;
    }

    private void initializeViews(View view) {
        tvHabitName = view.findViewById(R.id.tvHabitName);
        tvFrequency = view.findViewById(R.id.tvFrequency);
        tvDays = view.findViewById(R.id.tvDays);
        tvTime = view.findViewById(R.id.tvTime);
        tvReminder = view.findViewById(R.id.tvReminder);
        btnEditHabit = view.findViewById(R.id.btnEditHabit);
        btnConfirmHabit = view.findViewById(R.id.btnConfirmHabit);
    }

    private void extractArguments() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            habitName = arguments.getString("habitName");
            frequency = arguments.getString("frequency");
            time = arguments.getString("time");
            String reminderTimeString = arguments.getString("reminderTime");
            reminderTimes = new ArrayList<>();
            if (reminderTimeString != null && !reminderTimeString.equals("None")) {
                reminderTimes.add(reminderTimeString); // Use the selected reminder time
            }
        }
    }

    private void displayHabitDetails() {
        tvHabitName.setText("Habit Name: " + habitName);
        tvFrequency.setText("Frequency: " + frequency);
        tvDays.setText("Days: " + days);
        tvTime.setText("Time: " + time); // Assuming 'time' is already in 12-hour format

        String reminderTimeString = getArguments().getString("reminderTime");
        if (reminderTimeString != null && !reminderTimeString.isEmpty() && !reminderTimeString.equals("None")) {
            tvReminder.setText("Reminder: " + reminderTimeString);
        } else {
            tvReminder.setText("Reminder: None");
        }
    }

    private String formatTimeTo12Hour(int hour, int minute) {
        String amPm = (hour < 12) ? "AM" : "PM";
        hour = hour % 12;
        if (hour == 0) hour = 12;
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }




    private void navigateToEditHabitScreen() {
        // Implement navigation to the habit editing screen
        // Here, you can use FragmentManager to replace the current fragment
        // with the appropriate editing screen
    }

    private void onConfirmClicked() {
        Habit newHabit = new Habit(habitName, category, extractHour(time), extractMinute(time), iconUrl, frequency, 1, reminderTimes);
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        firebaseHelper.addHabit(userId, newHabit, new FirebaseHelper.FirestoreCallback<Habit>() {
            @Override
            public void onCallback(Habit result) {
                Log.d("ReviewScreen", "Habit added successfully");
                navigateToHomeFragment();
            }

            @Override
            public void onError(Exception e) {
                Log.e("ReviewScreen", "Error adding habit: " + e.getMessage());
                // Show error to the user
            }
        });
    }

    private int extractHour(String time) {
        // Check if time contains AM/PM
        if (time.contains("AM") || time.contains("PM")) {
            String[] parts = time.split("[: ]"); // Split by colon and space
            int hour = Integer.parseInt(parts[0]);
            if (parts[2].equalsIgnoreCase("PM") && hour != 12) {
                hour += 12;
            } else if (parts[2].equalsIgnoreCase("AM") && hour == 12) {
                hour = 0;
            }
            return hour;
        } else {
            // 24-hour format
            return Integer.parseInt(time.split(":")[0]);
        }
    }

    private int extractMinute(String time) {
        return Integer.parseInt(time.split("[: ]")[1]);
    }


    private void navigateToHomeFragment() {
        assert getActivity() != null;
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }



}
