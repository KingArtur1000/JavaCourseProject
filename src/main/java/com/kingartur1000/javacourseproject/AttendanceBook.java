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

    /** Добавить посещение (одна дата = один визит) */
    public void addRecord(StudentAttendance record) {
        records.add(record);
        groups.add(record.getGroup());
    }

    /** Удалить конкретную запись */
    public void deleteRecord(StudentAttendance record) {
        records.remove(record);
    }

    /** Добавить новую группу */
    public void addGroup(String group) {
        groups.add(group);
    }

    /** Удалить группу и все её записи */
    public void deleteGroup(String group) {
        groups.remove(group);
        records.removeIf(r -> r.getGroup().equals(group));
    }

    /** Фильтр по дате */
    public List<StudentAttendance> filterByDate(LocalDate date) {
        return records.stream()
                .filter(r -> r.getDate().equals(date))
                .collect(Collectors.toList());
    }

    /** Сортировка по фамилии */
    public List<StudentSummary> sortBySurname() {
        return getSummarized().stream()
                .sorted(Comparator.comparing(s -> s.getFio().split("\\s+")[0]))
                .collect(Collectors.toList());
    }

    /** Сортировка по количеству посещений */
    public List<StudentSummary> sortByVisits() {
        return getSummarized().stream()
                .sorted(Comparator.comparingInt(StudentSummary::getVisits).reversed())
                .collect(Collectors.toList());
    }

    public Set<String> getGroups() {
        return groups;
    }

    /** История посещений (сырые данные) */
    public List<StudentAttendance> getAll() {
        return new ArrayList<>(records);
    }

    /** Сохранить историю посещений в Excel */
    public void saveToExcel(String filePath) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Attendance");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ФИО");
            header.createCell(1).setCellValue("Группа");
            header.createCell(2).setCellValue("Дата");

            int rowIndex = 1;
            for (StudentAttendance r : records) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(r.getFio());
                row.createCell(1).setCellValue(r.getGroup());
                row.createCell(2).setCellValue(r.getDate().format(DATE_FMT));
            }

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                wb.write(fos);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Загрузить историю посещений из Excel */
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
                addRecord(new StudentAttendance(fio, group, date));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Вернуть список студентов с подсчитанным количеством посещений */
    public List<StudentSummary> getSummarized() {
        Map<String, Set<LocalDate>> visitsMap = new LinkedHashMap<>();
        Map<String, String> groupMap = new HashMap<>();

        for (StudentAttendance r : records) {
            String key = r.getFio() + "|" + r.getGroup();
            visitsMap.computeIfAbsent(key, k -> new HashSet<>()).add(r.getDate());
            groupMap.putIfAbsent(key, r.getGroup());
        }

        List<StudentSummary> result = new ArrayList<>();
        for (var entry : visitsMap.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            result.add(new StudentSummary(parts[0], groupMap.get(entry.getKey()), entry.getValue().size()));
        }
        return result;
    }
}
