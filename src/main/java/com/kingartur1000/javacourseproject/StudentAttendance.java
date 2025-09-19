package com.kingartur1000.javacourseproject;

import java.time.LocalDate;

public class StudentAttendance {
    private String fio;
    private String group;
    private LocalDate date;
    private int visits;

    public StudentAttendance(String fio, String group, LocalDate date, int visits) {
        this.fio = fio;
        this.group = group;
        this.date = date;
        this.visits = visits;
    }

    public String getFio() { return fio; }
    public String getGroup() { return group; }
    public LocalDate getDate() { return date; }
    public int getVisits() { return visits; }

    public void setFio(String fio) { this.fio = fio; }
    public void setGroup(String group) { this.group = group; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setVisits(int visits) { this.visits = visits; }
}
