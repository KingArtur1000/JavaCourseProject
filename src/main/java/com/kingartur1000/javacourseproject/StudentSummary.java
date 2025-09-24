/**
 * Класс StudentSummary представляет агрегированную информацию о студенте.
 * Содержит ФИО, название группы и общее количество уникальных посещений.
 * Используется для отображения сводной статистики в журнале посещаемости.
 *
 * @author: A.A. Dmitriev
 * @version: 1.0
 */


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
