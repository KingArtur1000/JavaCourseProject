/**
 * Класс Controller отвечает за взаимодействие классом посещаемости (AttendanceBook)
 * и графическим интерфейсом JavaFX. Управляет таблицей студентов, фильтрацией по группе и дате,
 * добавлением и удалением записей, сортировкой, а также сохранением/загрузкой данных в Excel.
 * Обрабатывает действия пользователя (кнопки, выборы, двойные клики) и обновляет отображение.
 *
 * @author: A.A. Dmitriev
 * @version: 1.0
 */



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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

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
        fioCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFio()));
        groupCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGroup()));
        dateCol.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getDate()));

        // Формат даты в таблице
        dateCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(fmt));
            }
        });

        // Кол-во посещений считается автоматически
        visitsCol.setCellValueFactory(c -> {
            String fio = c.getValue().getFio();
            String group = c.getValue().getGroup();
            long count = book.getAll().stream()
                    .filter(r -> r.getFio().equals(fio) && r.getGroup().equals(group))
                    .map(StudentAttendance::getDate)
                    .distinct()
                    .count();
            return new SimpleIntegerProperty((int) count).asObject();
        });

        group_ComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        attendanceTable.setItems(tableData);
        refreshTable();

        // Двойной клик — показать историю
        attendanceTable.setRowFactory(tv -> {
            TableRow<StudentAttendance> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    StudentAttendance sa = row.getItem();
                    showVisitHistory(sa.getFio(), sa.getGroup());
                }
            });
            return row;
        });
    }

    private void applyFilters() {
        String selectedGroup = group_ComboBox.getSelectionModel().getSelectedItem();
        LocalDate selectedDate = datePicker.getValue();

        tableData.setAll(
                book.getAll().stream()
                        .filter(s -> selectedGroup == null || selectedGroup.isBlank() || s.getGroup().equals(selectedGroup))
                        .filter(s -> selectedDate == null || selectedDate.equals(s.getDate()))
                        .toList()
        );
    }

    private void refreshTable() {
        tableData.setAll(book.getAll());
        group_ComboBox.setItems(FXCollections.observableArrayList(book.getGroups()));
    }

    private void showVisitHistory(String fio, String group) {
        var visits = book.getAll().stream()
                .filter(r -> r.getFio().equals(fio) && r.getGroup().equals(group))
                .map(r -> r.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                .sorted()
                .toList();

        ListView<String> listView = new ListView<>(FXCollections.observableArrayList(visits));
        listView.setPrefHeight(200);

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("История посещений");
        dialog.setHeaderText(fio + " (" + group + ")");
        dialog.getDialogPane().setContent(listView);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
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
        StudentAttendance selected = attendanceTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            book.deleteRecord(selected);
            refreshTable();
        }
    }

    @FXML
    private void sortBySurname() {
        tableData.setAll(book.getAll().stream()
                .sorted(Comparator.comparing(r -> r.getFio().split("\\s+")[0]))
                .toList());
    }

    @FXML
    private void sortByVisit() {
        tableData.setAll(book.getAll().stream()
                .sorted(Comparator.comparingInt((StudentAttendance r) ->
                        (int) book.getAll().stream()
                                .filter(x -> x.getFio().equals(r.getFio()) && x.getGroup().equals(r.getGroup()))
                                .map(StudentAttendance::getDate)
                                .distinct()
                                .count()
                ).reversed())
                .toList());
    }

    @FXML
    private void saveToExcel() {
        book.saveToExcel("attendance.xlsx");
    }

    @FXML
    private void reloadFromExcel() {
        book.loadFromExcel("attendance.xlsx");
        refreshTable();
    }

    @FXML
    private void filterByDate() {
        applyFilters();
    }
}
