package com.example.habithero;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class HabitPreferenceDialogFragment extends DialogFragment {

    private TimePicker timePickerDeadline;
    private Button buttonConfirm;
    private Button buttonCancel;
    private Spinner spinnerFrequency;
    private NumberPicker numberPickerCompletionCount;
    private Switch switchReminders;
    private LinearLayout reminderTimePickerContainer;
    private String habitName;
    private String habitCategory;
    private HabitAddListener habitAddListener;
    private TextView textViewDeadline;
    private Button buttonReminderConfirm;
    private Button buttonReminderCancel;
    private TextView textViewReminderTime;

    public interface HabitAddListener {
        void onHabitAdded(Habit habit);
    }

    public void setHabitAddListener(HabitAddListener listener) {
        this.habitAddListener = listener;
    }

    public static HabitPreferenceDialogFragment newInstance(String habitName, String habitCategory) {
        HabitPreferenceDialogFragment fragment = new HabitPreferenceDialogFragment();
        Bundle args = new Bundle();
        args.putString("habitName", habitName);
        args.putString("habitCategory", habitCategory);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.habit_preference_dialog_fragment, null);

        timePickerDeadline = view.findViewById(R.id.timePickerDeadline);
        spinnerFrequency = view.findViewById(R.id.spinnerFrequency);
        numberPickerCompletionCount = view.findViewById(R.id.numberPickerCompletionCount);
        switchReminders = view.findViewById(R.id.switchReminders);
        reminderTimePickerContainer = view.findViewById(R.id.reminderTimePickerContainer);
        textViewDeadline = view.findViewById(R.id.textViewDeadline);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonReminderConfirm = view.findViewById(R.id.buttonReminderConfirm);
        buttonReminderCancel = view.findViewById(R.id.buttonReminderCancel);
        textViewReminderTime = view.findViewById(R.id.textViewReminderTime);

        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
            habitCategory = getArguments().getString("habitCategory");
        }

        setupFrequencySpinner();
        setupNumberPicker();
        setupReminderSwitch();

        buttonReminderConfirm.setOnClickListener(v -> {
            // Logic to handle reminder time confirmation
            handleReminderConfirmation();
        });

        buttonReminderCancel.setOnClickListener(v -> {
            // Logic to handle reminder cancellation
            handleReminderCancellation();
        });

        buttonConfirm.setOnClickListener(v -> {
            int hour = timePickerDeadline.getCurrentHour();
            int minute = timePickerDeadline.getCurrentMinute();
            saveHabit(habitName, habitCategory, hour, minute);
            dismiss();
        });

        buttonCancel.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }


    private void setupNumberPicker() {
        numberPickerCompletionCount.setMinValue(1);
        numberPickerCompletionCount.setMaxValue(10);
    }

    private void setupReminderSwitch() {
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Show the reminder time picker and buttons
                showReminderSettingUI();
            } else {
                // Hide the reminder time picker and buttons, show other UI elements
                hideReminderSettingUI();
            }
        });
    }



    private boolean isReminderSwitchChecked() {
        return switchReminders.isChecked();
    }

    private void updateDialogLayout() {
        // Refresh layout to accommodate changes
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void addReminderTimePicker() {
        TimePicker reminderTimePicker = new TimePicker(getContext());
        reminderTimePicker.setIs24HourView(true);
        reminderTimePickerContainer.addView(reminderTimePicker);
    }

    private void setupFrequencySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.frequency_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFrequency.setAdapter(adapter);
        spinnerFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFrequency = spinnerFrequency.getSelectedItem().toString();
                setDeadlineText(selectedFrequency);            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setDeadlineText(String frequency) {
        String deadlineText = "Your habit will be due ";
        switch (frequency) {
            case "Daily":
                deadlineText += "every day at this time:";
                break;
            case "Weekly":
                deadlineText += "every week at this time:";
                break;
            case "Bi-Weekly":
                deadlineText += "every other week at this time:";
                break;
            case "Monthly":
                deadlineText += "every month at this time:";
                break;
            default:
                deadlineText = "Set your habit deadline time:";
                break;
        }
        textViewDeadline.setText(deadlineText);
    }


    private void saveHabit(String name, String category, int hour, int minute) {
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Check if the category is "Create Your Own"
        if ("Create Your Own".equals(category)) {
            // Use a predefined icon URL for custom category
            String customIconUrl = "https://firebasestorage.googleapis.com/v0/b/habit-hero-ef682.appspot.com/o/icon_images%2Fcustom.png?alt=media&token=9497217b-2b82-4f49-9291-32c736f9df55";
            Habit habit = createHabitWithNewFeatures(name, category, hour, minute, customIconUrl);
            saveHabitToFirebase(firebaseHelper, userId, habit);
        } else {
            // For other categories, fetch the icon URL
            firebaseHelper.fetchIconUrlForCategory(category, new FirebaseHelper.FirestoreCallback<String>() {
                @Override
                public void onCallback(String iconUrl) {
                    Habit habit = createHabitWithNewFeatures(name, category, hour, minute, iconUrl);
                    saveHabitToFirebase(firebaseHelper, userId, habit);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("HabitPreferenceDialog", "Error fetching icon URL: " + e.getMessage(), e);
                }
            });
        }
    }

    private Habit createHabitWithNewFeatures(String name, String category, int hour, int minute, String iconUrl) {
        String frequency = spinnerFrequency.getSelectedItem().toString();
        int completionCount = numberPickerCompletionCount.getValue();
        List<String> reminderTimes = extractReminderTimes();

        // Updated to match the Habit constructor
        return new Habit(name, category, hour, minute, iconUrl, frequency, completionCount, reminderTimes);
    }


    private void saveHabitToFirebase(FirebaseHelper firebaseHelper, String userId, Habit habit) {
        firebaseHelper.addHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Habit>() {
            @Override
            public void onCallback(Habit result) {
                Log.d("DebugLog", "Habit saved successfully. Navigating to HomeFragment.");
                dismiss();
                if (habitAddListener != null) {
                    habitAddListener.onHabitAdded(result);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HabitPreferenceDialog", "Error saving habit: " + e.getMessage(), e);
            }
        });
    }




    private List<String> extractReminderTimes() {
        List<String> reminderTimes = new ArrayList<>();
        for (int i = 0; i < reminderTimePickerContainer.getChildCount(); i++) {
            TimePicker timePicker = (TimePicker) reminderTimePickerContainer.getChildAt(i);
            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();
            reminderTimes.add(hour + ":" + minute);
        }
        return reminderTimes;
    }

    private void showReminderSettingUI() {
        // Hide all other UI elements
        spinnerFrequency.setVisibility(View.GONE);
        numberPickerCompletionCount.setVisibility(View.GONE);
        timePickerDeadline.setVisibility(View.GONE);
        buttonConfirm.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);

        // Show reminder setting UI
        reminderTimePickerContainer.setVisibility(View.VISIBLE);
        buttonReminderConfirm.setVisibility(View.VISIBLE);
        buttonReminderCancel.setVisibility(View.VISIBLE);
    }

    private void hideReminderSettingUI() {
        // Show all other UI elements
        spinnerFrequency.setVisibility(View.VISIBLE);
        numberPickerCompletionCount.setVisibility(View.VISIBLE);
        timePickerDeadline.setVisibility(View.VISIBLE);
        buttonConfirm.setVisibility(View.VISIBLE);
        buttonCancel.setVisibility(View.VISIBLE);

        // Hide reminder setting UI
        reminderTimePickerContainer.setVisibility(View.GONE);
        buttonReminderConfirm.setVisibility(View.GONE);
        buttonReminderCancel.setVisibility(View.GONE);
    }

    private void handleReminderConfirmation() {
        // Extract the time from the time picker and store it
        TimePicker timePicker = (TimePicker) reminderTimePickerContainer.getChildAt(0);
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();
        String reminderTime = String.format("%02d:%02d", hour, minute);
        textViewReminderTime.setText("Reminder set for: " + reminderTime);

        // Hide the reminder setting UI and show other UI elements
        hideReminderSettingUI();
    }

    private void handleReminderCancellation() {
        // Simply hide the reminder setting UI and show other UI elements
        hideReminderSettingUI();
    }


}
