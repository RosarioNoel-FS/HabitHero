package com.example.habithero;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
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
    private TextView frequencyTextView;
    private TextView completionTargetTextView;
    private TextView remindersTextView;
    private MaterialCalendarView calendarView;


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

        initializeViews(view);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String habitId = getArguments() != null ? getArguments().getString("habitId") : null;
        if (habitId != null) {
            fetchHabitDetails(userId, habitId);
        } else {
            Log.e("DetailFragment", "Habit ID is null");
        }

        return view;
    }


    private void initializeViews(View view) {
        detailImageView = view.findViewById(R.id.detail_imageview_detail);
        titleTextView = view.findViewById(R.id.title_textview_detail);
        startDateTextView = view.findViewById(R.id.start_date_textview);
        habitTotalTextView = view.findViewById(R.id.habit_total_textview);
        streakCountTextView = view.findViewById(R.id.streak_count_textview);
        completionTextView = view.findViewById(R.id.completion_textview);
        frequencyTextView = view.findViewById(R.id.frequency_textview);
        completionTargetTextView = view.findViewById(R.id.completion_target_textview);
        remindersTextView = view.findViewById(R.id.reminders_textview);
        calendarView = view.findViewById(R.id.calendarView);
        setupCalendarView();

        ImageView infoButton = view.findViewById(R.id.detail_info_button);
        infoButton.setOnClickListener(v -> showInfoModal());
    }

    private void setupCalendarView() {

        calendarView.addDecorator(new DayNameDecorator(getContext()));
        calendarView.addDecorator(new DayNumberDecorator(getContext()));
        calendarView.addDecorator(new NoSelectionDecorator());
    }


    private void fetchHabitDetails(String userId, String habitId) {
        FirebaseHelper firebaseHelper = new FirebaseHelper();
        firebaseHelper.fetchUserHabits(userId, new FirebaseHelper.FirestoreCallback<List<Habit>>() {
            @Override
            public void onCallback(List<Habit> habits) {
                for (Habit habit : habits) {
                    if (habit.getId().equals(habitId)) {
                        setHabitDetails(habit);
                        break;
                    }
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("DetailFragment", "Error fetching habit details: " + e.getMessage(), e);
            }
        });
    }

    private void setHabitDetails(Habit habit) {
        titleTextView.setText(habit.getName());
        Glide.with(this).load(habit.getIconUrl()).placeholder(R.drawable.detail_placeholder_img).into(detailImageView);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        startDateTextView.setText(dateFormat.format(new Date(habit.getTimestamp().toDate().getTime())));
        habitTotalTextView.setText(String.valueOf(habit.getCompletionCount()));
        streakCountTextView.setText(String.valueOf(habit.getStreakCount()));
        completionTextView.setText(habit.getCompleted() ? "Completed" : "Incomplete");

        frequencyTextView.setText("Frequency: " + habit.getFrequency());
        completionTargetTextView.setText("Target: " + habit.getDailyCompletionTarget());
        remindersTextView.setText("Reminders: " + TextUtils.join(", ", habit.getReminderTimes()));

        List<CalendarDay> completedDays = new ArrayList<>();
        for (String dateString : habit.getCompletionDates()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault());
                LocalDate localDate = LocalDate.parse(dateString, formatter);
                CalendarDay day = CalendarDay.from(localDate);
                completedDays.add(day);
            } catch (Exception e) {
                Log.e("DetailFragment", "Error parsing date: " + e.getMessage(), e);
            }
        }
        calendarView.addDecorator(new EventDecorator(getContext(), completedDays));

    }

    private void setCompletionDates(MaterialCalendarView calendarView, List<String> completionDates) {
        List<CalendarDay> completedDays = new ArrayList<>();
        for (String dateString : completionDates) {
            try {
                LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                completedDays.add(CalendarDay.from(localDate));
            } catch (Exception e) {
                Log.e("DetailFragment", "Error parsing date: " + e.getMessage(), e);
            }
        }
        EventDecorator eventDecorator = new EventDecorator(getContext(), completedDays);
        calendarView.addDecorator(eventDecorator);
    }


    private void showInfoModal() {
        DetailInfoModal infoModalFragment = new DetailInfoModal();
        getParentFragmentManager().beginTransaction()
                .add(R.id.fragment_container, infoModalFragment) // Use the correct container ID
                .addToBackStack(null)
                .commit();
    }

    private void setCalendarHeaderAndWeekDayColor(MaterialCalendarView calendarView) {
        // This is a pseudo-code and might need adjustments based on MaterialCalendarView's structure
        ViewGroup headerView = (ViewGroup) calendarView.getChildAt(0); // Assuming header is the first child
        for (int i = 0; i < headerView.getChildCount(); i++) {
            View child = headerView.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(getResources().getColor(R.color.dark_yellow)); // Set text color
            }
        }

        // If week day labels are separate from the header, find and set their color similarly
    }

    private void checkAndShowInfoModal() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Boolean hasSeenInfoModal = documentSnapshot.getBoolean("hasSeenInfoModal");
                if (hasSeenInfoModal == null || !hasSeenInfoModal) {
                    showInfoModal();
                    userDocRef.update("hasSeenInfoModal", true);
                }
            }
        }).addOnFailureListener(e -> Log.e("DetailFragment", "Error checking info modal status: " + e.getMessage()));
    }
}

class NoSelectionDecorator implements DayViewDecorator {
    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
}
