package com.example.koledarapp;

import java.time.LocalDate;
import java.util.Set;

public class Calendar {
    private int year;
    private int month;
    private Set<Holiday> holidays;

    public Calendar(int year, int month, Set<Holiday> holidays) {
        this.year = year;
        this.month = month;
        this.holidays = holidays;
    }

    public int getDaysInMonth() {
        switch(month) {
            case 4: case 6: case 9: case 11:
                return 30;

            case 2:
                if(isLeapYear(year)) {
                    return 29;
                } else {
                   return 28;
                }

            default:
                return 31;
        }
    }

    // A year is a leap year if it is divisible with four but at the same time not divisible with 100 or if it is divisible with 400
    public boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    // returns the day of the week for the first day of the month (May 1st 2024 was a Wednesday --> 2)
    public int firstDayOfTheMonth() {
        LocalDate firstDay = LocalDate.of(year, month, 1);
        return (firstDay.getDayOfWeek().getValue() - 1); // numeric value of the enum
    }

    public boolean isHoliday(int day) {
        LocalDate date = LocalDate.of(year, month, day);
        for (Holiday holiday : holidays) {
            if (holiday.isReapeating()) {
                if (holiday.getDate().getMonthValue() == date.getMonthValue() && holiday.getDate().getDayOfMonth()  == date.getDayOfMonth()) {
                    return true;
                }
            } else {
                if (holiday.getDate() == date) {
                    return true;
                }
            }
        }
        return false;
    }


    // setters
    public void setYear(int year) {
        this.year = year;
    }

    public void setMonth(int month) {
        this.month = month;
    }

}
