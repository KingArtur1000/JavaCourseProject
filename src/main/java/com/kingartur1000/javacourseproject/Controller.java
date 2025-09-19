package com.kingartur1000.javacourseproject;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

import java.time.LocalDate;

public class Controller {
    @FXML private TableView<StudentAttendance> attendanceTable;
    @FXML private TableColumn<StudentAttendance, String> fioCol;
    @FXML private TableColumn<StudentAttendance, String> groupCol;
    @FXML private TableColumn<StudentAttendance, LocalDate> dateCol;
    @FXML private TableColumn<StudentAttendance, Integer> visitsCol;
    @FXML private ComboBox<String> group_ComboBox;
    @FXML private DatePicker datePicker;

    private final AttendanceBook book = new AttendanceBook();
    private final ObservableList<StudentAttendance> tableData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        fioCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getFio()));
        groupCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getGroup()));
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDate()));
        visitsCol.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getVisits()).asObject());

        attendanceTable.setItems(tableData);
    }

    @FXML
    private void filterByDate() {
        LocalDate date = datePicker.getValue();
        if (date != null) {
            tableData.setAll(book.filterByDate(date));
        }
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
            group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
            tableData.setAll(book.getAll());
        }
    }

    @FXML
    private void resetFilters() {
        datePicker.setValue(null);
        group_ComboBox.setValue(null);
        tableData.setAll(book.getAll());
    }

    @FXML
    private void addRecord() {
        Dialog<StudentAttendance> dialog = new Dialog<>();
        dialog.setTitle("Добавить запись");
        dialog.setHeaderText("Введите данные студента");

        // Кнопки
        ButtonType okButtonType = new ButtonType("Добавить", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        // Поля ввода
        TextField fioField = new TextField();
        fioField.setPromptText("Фамилия Имя");

        ComboBox<String> groupBox = new ComboBox<>(FXCollections.observableArrayList(book.getGroups()));
        groupBox.setEditable(true);
        groupBox.setPromptText("Группа");

        DatePicker datePickerField = new DatePicker(LocalDate.now());

        Spinner<Integer> visitsSpinner = new Spinner<>(0, 1000, 1);
        visitsSpinner.setEditable(true);

        // Компоновка
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("ФИО:"), 0, 0);
        grid.add(fioField, 1, 0);
        grid.add(new Label("Группа:"), 0, 1);
        grid.add(groupBox, 1, 1);
        grid.add(new Label("Дата:"), 0, 2);
        grid.add(datePickerField, 1, 2);
        grid.add(new Label("Посещений:"), 0, 3);
        grid.add(visitsSpinner, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Логика нажатия OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                String fio = fioField.getText().trim();
                String group = groupBox.getEditor().getText().trim();
                LocalDate date = datePickerField.getValue();
                int visits = visitsSpinner.getValue();

                if (fio.isEmpty() || group.isEmpty() || date == null) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Заполните все поля!", ButtonType.OK);
                    alert.showAndWait();
                    return null;
                }

                // Добавляем группу, если новая
                if (!book.getGroups().contains(group)) {
                    book.addGroup(group);
                    group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
                }

                return new StudentAttendance(fio, group, date, visits);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(record -> {
            book.addRecord(record);
            tableData.setAll(book.getAll());
        });
    }


    @FXML
    private void deleteRecord() {
        StudentAttendance selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            book.deleteRecord(selected);
            tableData.setAll(book.getAll());
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
        book.saveToExcel("attendance.xlsx");
    }

    @FXML
    private void reloadFromExcel() {
        book.loadFromExcel("attendance.xlsx");
        tableData.setAll(book.getAll());
        group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
    }
}
