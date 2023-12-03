package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class InfoModalFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_info_modal, container, false);

        Button okButton = view.findViewById(R.id.ok_button_detail);
        okButton.setOnClickListener(v -> {
            // Close the modal when OK button is clicked
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}

