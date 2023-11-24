package com.example.habithero;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class CustomHabitCreationDialogFragment extends DialogFragment implements HabitPreferenceDialogFragment.HabitAddListener{

    private EditText editTextHabitTitle;

    public CustomHabitCreationDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate and set the layout for the dialog
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.custom_habit_creation_dialog, null);

        editTextHabitTitle = view.findViewById(R.id.editTextHabitTitle);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonConfirm = view.findViewById(R.id.buttonConfirm);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonConfirm.setOnClickListener(v -> onConfirmClicked());

        builder.setView(view);
        return builder.create();
    }

    // Inside CustomHabitCreationDialogFragment


    private void onConfirmClicked() {
        String habitTitle = editTextHabitTitle.getText().toString().trim();
        Log.d("DebugLog", "Custom habit confirmed: " + habitTitle);

        if (!habitTitle.isEmpty()) {
            HabitPreferenceDialogFragment dialogFragment = HabitPreferenceDialogFragment.newInstance(habitTitle, "Create Your Own");
            dialogFragment.show(getParentFragmentManager(), "HabitPreferenceDialog");
            dismiss();
        } else {
            editTextHabitTitle.setError("Please enter a habit title");
        }
    }

    @Override
    public void onHabitAdded(Habit habit) {
        Log.d("DebugLog", "onHabitAdded called in CategoryListFragment/CustomHabitCreationDialogFragment, Habit ID: " + habit.getId());

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onHabitAdded(habit);
        }
    }




}
