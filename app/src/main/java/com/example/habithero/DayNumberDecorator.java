package com.example.habithero;

import android.content.Context;
import android.graphics.Color;
import android.text.style.ForegroundColorSpan;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class DayNumberDecorator implements DayViewDecorator {
    private final Context context;

    public DayNumberDecorator(Context context) {
        this.context = context;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        CalendarDay today = CalendarDay.today();
        return today.getMonth() == day.getMonth(); // Decorate only days of the current month
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.addSpan(new ForegroundColorSpan(Color.WHITE)); // White color for current month days
    }
}
