package com.example.flexclass;

public class Lesson {
    int id;
    String time, format, title, type, day, week, aud, link;
    public Lesson(String time, String format, String title, String type, String day, String week) {
        this.time = time;
        this.format = format;
        this.title = title;
        this.type = type;
        this.day = day;
        this.week = week;
    }
    public Lesson(String time, String format, String title, String type, String day, String week, String aud, String link) {
        this.time = time;
        this.format = format;
        this.title = title;
        this.type = type;
        this.day = day;
        this.week = week;
        this.aud = aud;
        this.link = link;
    }

    public String getAud() {
        return aud;
    }

    public void setAud(String aud) {
        this.aud = aud;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public String getTimeStart() {
        return this.time.substring(0, 5);
    }

    public String getTimeEnd() {
        return this.time.substring(6, 11);
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }
}
