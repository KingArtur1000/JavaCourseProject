package com.kingartur1000.javacourseproject;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;
import java.util.Comparator;

public class Controller {
    @FXML private TableView<StudentSummary> attendanceTable;
    @FXML private TableColumn<StudentSummary, String> fioCol;
    @FXML private TableColumn<StudentSummary, String> groupCol;
    @FXML private TableColumn<StudentSummary, Integer> visitsCol;
    @FXML private ComboBox<String> group_ComboBox;
    @FXML private DatePicker datePicker;

    private final AttendanceBook book = new AttendanceBook();
    private final ObservableList<StudentSummary> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        fioCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFio()));
        groupCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGroup()));
        visitsCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getVisits()).asObject());

        group_ComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        attendanceTable.setItems(tableData);
        refreshTable();

        // 🔹 Обработчик двойного клика
        attendanceTable.setRowFactory(tv -> {
            TableRow<StudentSummary> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    StudentSummary summary = row.getItem();
                    showVisitHistory(summary);
                }
            });
            return row;
        });
    }

    /** Показать историю посещений выбранного студента */
    private void showVisitHistory(StudentSummary summary) {
        // Получаем все даты посещений из истории
        var visits = book.getAll().stream()
                .filter(r -> r.getFio().equals(summary.getFio()) && r.getGroup().equals(summary.getGroup()))
                .map(r -> r.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .sorted()
                .toList();

        // Создаём ListView для отображения дат
        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(visits));
        listView.setPrefHeight(200);

        // Диалоговое окно
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("История посещений");
        dialog.setHeaderText(summary.getFio() + " (" + summary.getGroup() + ")");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }


    /** Применение фильтров по группе и дате */
    private void applyFilters() {
        String selectedGroup = group_ComboBox.getSelectionModel().getSelectedItem();
        LocalDate selectedDate = datePicker.getValue();

        if (selectedDate == null && (selectedGroup == null || selectedGroup.isBlank())) {
            refreshTable();
            return;
        }

        if (selectedDate != null) {
            // Фильтр по дате — показываем только тех, кто был в этот день
            tableData.setAll(
                    book.getAll().stream()
                            .filter(r -> selectedGroup == null || selectedGroup.isBlank() || r.getGroup().equals(selectedGroup))
                            .filter(r -> r.getDate().equals(selectedDate))
                            .map(r -> new StudentSummary(r.getFio(), r.getGroup(), 1))
                            .toList()
            );
        } else {
            // Фильтр только по группе — агрегируем
            tableData.setAll(
                    book.getSummarized().stream()
                            .filter(s -> selectedGroup == null || selectedGroup.isBlank() || s.getGroup().equals(selectedGroup))
                            .toList()
            );
        }
    }

    /** Обновить таблицу агрегированными данными */
    private void refreshTable() {
        tableData.setAll(book.getSummarized());
        group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
    }

    @FXML
    private void addGroup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setHeaderText("Введите название группы");
        dialog.showAndWait().ifPresent(book::addGroup);
        group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
    }

    @FXML
    private void deleteGroup() {
        String group = group_ComboBox.getValue();
        if (group != null) {
            book.deleteGroup(group);
            refreshTable();
        }
    }

    @FXML
    private void resetFilters() {
        datePicker.setValue(null);
        group_ComboBox.setValue(null);
        refreshTable();
    }

    @FXML
    private void addRecord() {
        Dialog<StudentAttendance> dialog = new Dialog<>();
        dialog.setTitle("Добавить запись");
        dialog.setHeaderText("Введите данные студента");

        ButtonType okButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        TextField fioField = new TextField();
        fioField.setPromptText("Фамилия Имя");

        ComboBox<String> groupBox = new ComboBox<>(FXCollections.observableArrayList(book.getGroups()));
        groupBox.setEditable(true);
        groupBox.setPromptText("Группа");

        DatePicker datePickerField = new DatePicker(LocalDate.now());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("ФИО:"), 0, 0);
        grid.add(fioField, 1, 0);
        grid.add(new Label("Группа:"), 0, 1);
        grid.add(groupBox, 1, 1);
        grid.add(new Label("Дата:"), 0, 2);
        grid.add(datePickerField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String fio = fioField.getText().trim();
                String group = groupBox.getEditor().getText().trim();
                LocalDate date = datePickerField.getValue();

                if (fio.isEmpty() || group.isEmpty() || date == null) {
                    new Alert(Alert.AlertType.WARNING, "Заполните все поля!", ButtonType.OK).showAndWait();
                    return null;
                }

                if (!book.getGroups().contains(group)) {
                    book.addGroup(group);
                }

                return new StudentAttendance(fio, group, date);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(record -> {
            book.addRecord(record);
            refreshTable();
        });
    }

    @FXML
    private void deleteRecord() {
        StudentSummary selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // Удаляем все визиты этого студента (по ФИО и группе)
            book.getAll().removeIf(r -> r.getFio().equals(selected.getFio()) && r.getGroup().equals(selected.getGroup()));
            refreshTable();
        }
    }

    @FXML
    private void sortBySurname() {
        tableData.setAll(book.sortBySurname());
    }

    @FXML
    private void sortByVisit() {
        tableData.setAll(book.sortByVisits());
    }

    @FXML
    private void saveToExcel() {
        book.saveToExcel("Students_Lectures.xlsx");
    }

    @FXML
    private void reloadFromExcel() {
        book.loadFromExcel("Students_Lectures.xlsx");
        refreshTable();
    }

    @FXML
    private void filterByDate() {
        applyFilters();
    }
}
