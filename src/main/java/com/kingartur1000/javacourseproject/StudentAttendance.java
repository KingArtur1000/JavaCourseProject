package com.kingartur1000.javacourseproject;

import java.time.LocalDate;

public class StudentAttendance {
    private String fio;
    private String group;
    private LocalDate date;

    public StudentAttendance(String fio, String group, LocalDate date) {
        this.fio = fio;
        this.group = group;
        this.date = date;
    }

    public String getFio() { return fio; }
    public String getGroup() { return group; }
    public LocalDate getDate() { return date; }
}

