package com.kingartur1000.javacourseproject;

public class StudentSummary {
    private String fio;
    private String group;
    private int visits;

    public StudentSummary(String fio, String group, int visits) {
        this.fio = fio;
        this.group = group;
        this.visits = visits;
    }

    public String getFio() { return fio; }
    public String getGroup() { return group; }
    public int getVisits() { return visits; }
}
