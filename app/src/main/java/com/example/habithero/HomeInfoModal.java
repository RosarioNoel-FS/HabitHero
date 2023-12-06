package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class HomeInfoModal extends Fragment {

    public HomeInfoModal() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_info_modal, container, false);

        Button okButton = view.findViewById(R.id.ok_button);
        okButton.setOnClickListener(v -> dismissModal());

        return view;
    }

    private void dismissModal() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().beginTransaction().remove(this).commit();
        }
    }
}
