/**
 * Главный класс приложения JavaFX для учёта посещаемости.
 * Отвечает за запуск программы, загрузку FXML‑интерфейса и отображение главного окна.
 * @author: A.A. Dmitriev
 * @version: 1.0
 */


package com.kingartur1000.javacourseproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("index.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Учёт посещений");
        stage.setScene(scene);
        stage.setWidth(1280);
        stage.setHeight(720);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
