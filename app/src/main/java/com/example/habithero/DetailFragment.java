package com.example.habithero;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;


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

        // Initialize views
        detailImageView = view.findViewById(R.id.detail_imageview_detail);
        titleTextView = view.findViewById(R.id.title_textview_detail);
        startDateTextView = view.findViewById(R.id.start_date_textview);
        habitTotalTextView = view.findViewById(R.id.habit_total_textview);
        streakCountTextView = view.findViewById(R.id.streak_count_textview);
        completionTextView = view.findViewById(R.id.completion_textview);
        MaterialCalendarView calendarView = view.findViewById(R.id.calendarView);
        ImageView infoButton = view.findViewById(R.id.detail_info_button); // Assuming the ID of the info button
        infoButton.setOnClickListener(v -> showInfoModal());

        // Retrieve and display habit details
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
            completionTextView.setText(isCompleted ? "Completed" : "Incomplete");

            // Fetch and display completion dates
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String habitId = getArguments().getString("habitId");

            if (habitId == null) {
                Log.e("DetailFragment", "habitId is null");
                // Handle the error here
                return view; // Exit the method to prevent further execution
            }

            FirebaseHelper firebaseHelper = new FirebaseHelper();
            firebaseHelper.fetchCompletionDates(userId, habitId, new FirebaseHelper.FirestoreCallback<List<String>>() {
                @Override
                public void onCallback(List<String> completionDates) {
                    List<CalendarDay> completedDays = new ArrayList<>();
                    for (String dateString : completionDates) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
                            LocalDate localDate = LocalDate.parse(dateString, formatter);
                            CalendarDay day = CalendarDay.from(localDate);
                            completedDays.add(day);
                        } catch (Exception e) {
                            Log.e("DetailFragment", "Error parsing date: " + e.getMessage(), e);
                        }
                    }
                    calendarView.addDecorator(new EventDecorator(completedDays));
                }

                @Override
                public void onError(Exception e) {
                    Log.e("DetailFragment", "Error fetching completion dates: " + e.getMessage(), e);
                }
            });
        }

        return view;
    }

    private void showInfoModal() {
        InfoModalFragment infoModalFragment = new InfoModalFragment();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, infoModalFragment) // Use the correct container ID
                .addToBackStack(null)
                .commit();
    }
}