package com.example.flexclass;

import java.util.List;

public class DaySchedule {
    private String dayName;
    private List<Lesson> lessons;

    public DaySchedule(String dayName, List<Lesson> lessons) {
        this.dayName = dayName;
        this.lessons = lessons;
    }

    public String getDayName() {
        return dayName;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }
}

