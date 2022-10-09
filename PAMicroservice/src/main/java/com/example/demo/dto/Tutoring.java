package com.example.demo.dto;

public class Tutoring {
    private String date;
    private String hour;

    public Tutoring (String date, String hour) {
        this.date = date;
        this.hour = hour;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }
}
