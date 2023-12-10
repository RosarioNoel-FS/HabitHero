package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FrequencySelectionScreen extends Fragment {

    private RadioButton radioDaily, radioWeekly, radioCustom;
    private Button btnNext;

    public FrequencySelectionScreen() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_frequency_screen, container, false);

        radioDaily = view.findViewById(R.id.radioDaily);
        radioWeekly = view.findViewById(R.id.radioWeekly);
        radioCustom = view.findViewById(R.id.radioWeekly_multipul);
        btnNext = view.findViewById(R.id.btnNextFrequency);

        btnNext.setOnClickListener(v -> onNextClicked());
        return view;
    }

    private void onNextClicked() {
        Fragment nextScreen = null;
        Bundle args = getArguments();
        String habitName = args != null ? args.getString("habitName") : "";

        if (radioDaily.isChecked()) {
            nextScreen = new DailySetupScreen();
            // Similar steps for Weekly and Custom screens
        }

        if (nextScreen != null) {
            // Pass the habit name to the next screen
            Bundle nextScreenArgs = new Bundle();
            nextScreenArgs.putString("habitName", habitName);
            nextScreen.setArguments(nextScreenArgs);

            assert getActivity() != null;
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, nextScreen)
                    .addToBackStack(null)
                    .commit();
        }
    }

}
