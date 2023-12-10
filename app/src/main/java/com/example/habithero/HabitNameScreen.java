package com.example.habithero;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class HabitNameScreen extends DialogFragment {

    private EditText editTextHabitTitle;
    private HabitNameScreenListener listener;

    public interface HabitNameScreenListener {
        void onHabitNameConfirmed(String habitName);
    }

    public HabitNameScreen() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof HabitNameScreenListener) {
            listener = (HabitNameScreenListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement HabitNameScreenListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.naming_custom_habit_screen, null);

        editTextHabitTitle = view.findViewById(R.id.editTextHabitTitle);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        Button buttonNext = view.findViewById(R.id.buttonConfirm);

        buttonCancel.setOnClickListener(v -> dismiss());
        buttonNext.setOnClickListener(v -> onNextClicked());

        builder.setView(view);
        return builder.create();
    }

    private void onNextClicked() {
        String habitTitle = editTextHabitTitle.getText().toString().trim();
        if (!habitTitle.isEmpty()) {
            listener.onHabitNameConfirmed(habitTitle);
            dismiss();
        } else {
            editTextHabitTitle.setError("Please enter a habit title");
        }
    }
}
