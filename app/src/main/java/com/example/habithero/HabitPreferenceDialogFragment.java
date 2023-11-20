package com.example.habithero;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Date;

public class HabitPreferenceDialogFragment extends DialogFragment {

    private TimePicker timePickerDeadline;
    private Button buttonConfirm;
    private Button buttonCancel;
    private String habitName;
    private String habitCategory;

    private HabitAddListener habitAddListener;

    public interface HabitAddListener {
        void onHabitAdded(Habit habit);
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
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
        buttonCancel = view.findViewById(R.id.buttonCancel);

        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
            habitCategory = getArguments().getString("habitCategory");
        }

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

    //creates a new Habit object and uses FirebaseHelper to save it to Firebase under the user's ID
    // Inside HabitPreferenceDialogFragment
    private void saveHabit(String name, String category, int hour, int minute) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        Habit habit = new Habit(name, category, hour, minute);

        firebaseHelper.addHabit(userId, habit, new FirebaseHelper.FirestoreCallback<Habit>() {
            @Override
            public void onCallback(Habit result) {
                if (getTargetFragment() instanceof HabitAddListener) {
                    ((HabitAddListener) getTargetFragment()).onHabitAdded(result);
                } else {
                    Log.e("HabitPreferenceDialog", "Target fragment is not set or wrong type");
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("HabitPreferenceDialog", "Error saving habit: " + e.getMessage(), e);
            }
        });
    }





}