package com.example.habithero;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import java.util.List;

public class EventDecorator implements DayViewDecorator {
    private final List<CalendarDay> dates;
    private final Drawable heartDrawable;

    public EventDecorator(Context context, List<CalendarDay> dates) {
        this.dates = dates;
        this.heartDrawable = ContextCompat.getDrawable(context, R.drawable.heart_calendar); // Your heart drawable
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(heartDrawable);
        view.addSpan(new ForegroundColorSpan(Color.WHITE)); // White text for days with the heart
    }
}
