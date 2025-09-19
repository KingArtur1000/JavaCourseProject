package com.kingartur1000.javacourseproject;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceBook {
    private final List<StudentAttendance> records = new ArrayList<>();
    private final Set<String> groups = new TreeSet<>();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public void addRecord(StudentAttendance record) {
        records.add(record);
        groups.add(record.getGroup());
    }

    public void deleteRecord(StudentAttendance record) {
        records.remove(record);
    }

    public void addGroup(String group) {
        groups.add(group);
    }

    public void deleteGroup(String group) {
        groups.remove(group);
        records.removeIf(r -> r.getGroup().equals(group));
    }

    public List<StudentAttendance> filterByDate(LocalDate date) {
        return records.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<StudentAttendance> sortBySurname() {
        return records.stream()
                .sorted(Comparator.comparing(r -> r.getFio().split("\\s+")[0]))
                .collect(Collectors.toList());
    }

    public List<StudentAttendance> sortByVisits() {
        return records.stream()
                .sorted(Comparator.comparingInt(StudentAttendance::getVisits).reversed())
                .collect(Collectors.toList());
    }

    public Set<String> getGroups() {
        return groups;
    }

    public List<StudentAttendance> getAll() {
        return new ArrayList<>(records);
    }

    public void saveToExcel(String filePath) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Attendance");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ФИО");
            header.createCell(1).setCellValue("Группа");
            header.createCell(2).setCellValue("Дата");
            header.createCell(3).setCellValue("Кол-во посещений");

            int rowIndex = 1;
            for (StudentAttendance r : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getFio());
                row.createCell(1).setCellValue(r.getGroup());
                row.createCell(2).setCellValue(r.getDate().format(DATE_FMT));
                row.createCell(3).setCellValue(r.getVisits());
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromExcel(String filePath) {
        records.clear();
        groups.clear();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook wb = new XSSFWorkbook(fis)) {

            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                String fio = row.getCell(0).getStringCellValue();
                String group = row.getCell(1).getStringCellValue();
                LocalDate date = LocalDate.parse(row.getCell(2).getStringCellValue(), DATE_FMT);
                int visits = (int) row.getCell(3).getNumericCellValue();
                addRecord(new StudentAttendance(fio, group, date, visits));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
