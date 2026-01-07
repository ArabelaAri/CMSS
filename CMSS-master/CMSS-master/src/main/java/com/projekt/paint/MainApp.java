package com.projekt.paint;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/projekt/paint/paint.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/projekt/paint/style.css").toExternalForm());
        stage.setTitle("CMSS Paint");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) { launch(); }
}
