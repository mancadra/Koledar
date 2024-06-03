package com.example.koledarapp;

import java.time.LocalDate;

public class Holiday {
    private LocalDate date;
    private boolean annual;

    public Holiday(LocalDate date, boolean annual) {
        this.date = date;
        this.annual = annual;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isReapeating() {
        return annual;
    }


    // checks if the objects are the same
    public boolean equalDates(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Holiday holiday = (Holiday) obj;

        return date.equals(holiday.date);
    }

    // allows for only one holiday per day (in future updates if we add a name to the holidays that must be included as well)
    @Override
    public int hashCode() {
        return date.hashCode();
    }
}
