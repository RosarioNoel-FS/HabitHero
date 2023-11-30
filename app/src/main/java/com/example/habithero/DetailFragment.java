package com.example.habithero;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DetailFragment extends Fragment {

    private ImageView detailImageView;
    private TextView titleTextView;
    private TextView startDateTextView;
    private TextView habitTotalTextView;
    private TextView streakCountTextView;
    private TextView completionTextView;

    private String habitName;
    private String iconUrl;
    private long timestampMillis;
    private int completionCount;
    private int streakCount;
    private boolean isCompleted;

    public DetailFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        detailImageView = view.findViewById(R.id.detail_imageview_detail);
        titleTextView = view.findViewById(R.id.title_textview_detail);
        startDateTextView = view.findViewById(R.id.start_date_textview);
        habitTotalTextView = view.findViewById(R.id.habit_total_textview);
        streakCountTextView = view.findViewById(R.id.streak_count_textview);
        completionTextView = view.findViewById(R.id.completion_textview);

        if (getArguments() != null) {
            habitName = getArguments().getString("habitName");
            iconUrl = getArguments().getString("iconUrl");
            timestampMillis = getArguments().getLong("timestampMillis");
            completionCount = getArguments().getInt("completionCount");
            streakCount = getArguments().getInt("streakCount");
            isCompleted = getArguments().getBoolean("isCompleted");

            titleTextView.setText(habitName);
            Glide.with(this)
                    .load(iconUrl)
                    .placeholder(R.drawable.detail_placeholder_img) // Placeholder image
                    .into(detailImageView);

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            startDateTextView.setText(dateFormat.format(new Date(timestampMillis)));

            habitTotalTextView.setText(String.valueOf(completionCount));
            streakCountTextView.setText(String.valueOf(streakCount));
            completionTextView.setText(isCompleted ? "Completed" : "Incompleted");
        }

        return view;
    }
}
