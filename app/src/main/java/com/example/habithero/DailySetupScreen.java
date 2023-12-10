package com.example.habithero;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class DailySetupScreen extends Fragment {

    private TimePicker timePicker;
    private Switch switchReminder;
    private Button btnNextTimeReminder;
    private String habitName;
    private int reminderHour = -1, reminderMinute = -1; // Default values
    private TextView tvReminderTime;
    private Button btnEditReminder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.day_setup_screen, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        switchReminder = view.findViewById(R.id.switchReminder);
        btnNextTimeReminder = view.findViewById(R.id.btnNextTimeReminder);
        tvReminderTime = view.findViewById(R.id.tvReminderTime);
        btnEditReminder = view.findViewById(R.id.btnEditReminder);


        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
        }

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                showTimePickerDialog();
            } else {
                resetReminder();
            }
        });

        btnNextTimeReminder.setOnClickListener(v -> onNextTimeReminderClicked());
        btnEditReminder.setOnClickListener(v -> showTimePickerDialog());


        return view;
    }

    private void resetReminder() {
        reminderHour = -1;
        reminderMinute = -1;
        tvReminderTime.setVisibility(View.GONE);
        btnEditReminder.setVisibility(View.GONE);
    }

    private void showTimePickerDialog() {
        // Use the current time as the default values for the picker
        final Calendar calendar = Calendar.getInstance();
        int hour = (reminderHour >= 0) ? reminderHour : calendar.get(Calendar.HOUR_OF_DAY);
        int minute = (reminderMinute >= 0) ? reminderMinute : calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            reminderHour = hourOfDay;
            reminderMinute = minuteOfHour;
            updateReminderTimeDisplay();
        }, hour, minute, false);

        timePickerDialog.show();
    }


    private void onNextTimeReminderClicked() {
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        boolean reminderEnabled = switchReminder.isChecked();

        String formattedTime = formatTimeTo12Hour(hour, minute);
        String reminderTimeFormatted = reminderEnabled ? formatTimeTo12Hour(reminderHour, reminderMinute) : "None";

        Bundle args = new Bundle();
        args.putString("habitName", habitName);
        args.putString("frequency", "Daily");
        args.putString("time", formattedTime);
        args.putString("reminderTime", reminderTimeFormatted); // Include the reminder time

        ReviewAndConfirmHabitInfoScreen reviewScreen = new ReviewAndConfirmHabitInfoScreen();
        reviewScreen.setArguments(args);

        assert getActivity() != null;
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, reviewScreen)
                .addToBackStack(null)
                .commit();
    }

    private String formatTimeTo12Hour(int hour, int minute) {
        String amPm = (hour < 12) ? "AM" : "PM";
        hour = hour % 12;
        if (hour == 0) hour = 12; // Convert 0 to 12 for 12AM
        return String.format("%02d:%02d %s", hour, minute, amPm);
    }

    private void updateReminderTimeDisplay() {
        if (reminderHour >= 0 && reminderMinute >= 0) {
            String formattedTime = formatTimeTo12Hour(reminderHour, reminderMinute);
            tvReminderTime.setText("Reminder: " + formattedTime);
            tvReminderTime.setVisibility(View.VISIBLE);
            btnEditReminder.setVisibility(View.VISIBLE);
        } else {
            tvReminderTime.setVisibility(View.GONE);
            btnEditReminder.setVisibility(View.GONE);
        }
    }



}
