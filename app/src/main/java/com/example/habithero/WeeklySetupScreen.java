package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class WeeklySetupScreen extends Fragment {

    private Spinner spinnerDayOfWeek;
    private TimePicker timePickerWeekly;
    private Switch switchReminderWeekly;
    private Button weekNextBtn;
    private String habitName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.week_setup_screen, container, false);

        spinnerDayOfWeek = view.findViewById(R.id.spinnerDayOfWeek);
        timePickerWeekly = view.findViewById(R.id.timePickerWeekly);
        switchReminderWeekly = view.findViewById(R.id.switchReminderWeekly);
        weekNextBtn = view.findViewById(R.id.week_next_btn);

        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
        }

        // Initialize Spinner with days of the week
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDayOfWeek.setAdapter(adapter);

        weekNextBtn.setOnClickListener(v -> onNextWeeklyClicked());

        return view;
    }

    private void onNextWeeklyClicked() {
        String selectedDay = spinnerDayOfWeek.getSelectedItem().toString();
        int hour = timePickerWeekly.getCurrentHour();
        int minute = timePickerWeekly.getCurrentMinute();
        boolean reminderEnabled = switchReminderWeekly.isChecked();

        ReviewAndConfirmHabitInfoScreen reviewScreen = new ReviewAndConfirmHabitInfoScreen();
        Bundle args = new Bundle();
        args.putString("habitName", habitName);
        args.putString("frequency", "Weekly");
        args.putString("dayOfWeek", selectedDay);
        args.putInt("hour", hour);
        args.putInt("minute", minute);
        args.putBoolean("reminder", reminderEnabled);
        reviewScreen.setArguments(args);

        assert getActivity() != null;
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, reviewScreen)
                .addToBackStack(null)
                .commit();
    }
}

