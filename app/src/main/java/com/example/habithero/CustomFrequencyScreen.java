package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class CustomFrequencyScreen extends Fragment {

    private CheckBox checkboxMonday, checkboxTuesday, checkboxWednesday, checkboxThursday, checkboxFriday, checkboxSaturday, checkboxSunday;
    private LinearLayout containerTimeAndReminders;
    private Button btnCancelCustom, btnNextCustom;
    private String habitName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.custom_setup_screen, container, false);

        checkboxMonday = view.findViewById(R.id.checkboxMonday);
        // ... initialize other checkboxes
        containerTimeAndReminders = view.findViewById(R.id.containerTimeAndReminders);
        btnCancelCustom = view.findViewById(R.id.btnCancelCustom);
        btnNextCustom = view.findViewById(R.id.btnNextCustom);

        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
        }

        btnCancelCustom.setOnClickListener(v -> onCancelCustomClicked());
        btnNextCustom.setOnClickListener(v -> onNextCustomClicked());

        return view;
    }

    private void onCancelCustomClicked() {
        // Clear selections or navigate back
        checkboxMonday.setChecked(false);
        // ... clear other checkboxes
        getFragmentManager().popBackStack();
    }

    private void onNextCustomClicked() {
        // Extract selected days
        ArrayList<String> selectedDays = new ArrayList<>();
        if (checkboxMonday.isChecked()) selectedDays.add("Monday");
        if (checkboxTuesday.isChecked()) selectedDays.add("Tuesday");
        if (checkboxWednesday.isChecked()) selectedDays.add("Wednesday");
        if (checkboxThursday.isChecked()) selectedDays.add("Thursday");
        if (checkboxFriday.isChecked()) selectedDays.add("Friday");
        if (checkboxSaturday.isChecked()) selectedDays.add("Saturday");
        if (checkboxSunday.isChecked()) selectedDays.add("Sunday");

        // Create the bundle with all necessary data
        Bundle args = new Bundle();
        args.putString("habitName", habitName);
        args.putString("frequency", "Custom");
        args.putStringArrayList("days", selectedDays);

        // Create and set the arguments for the next screen
        ReviewAndConfirmHabitInfoScreen reviewScreen = new ReviewAndConfirmHabitInfoScreen();
        reviewScreen.setArguments(args);

        // Navigate to the ReviewAndConfirmHabitInfoScreen
        assert getFragmentManager() != null;
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, reviewScreen)
                .addToBackStack(null)
                .commit();
    }

}

