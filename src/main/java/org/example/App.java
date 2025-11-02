package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Buttons
        ToggleButton drawButton = new ToggleButton("Draw");
        ToggleButton rubberButton = new ToggleButton("Rubber");

        ToggleButton[] toolsArr = {drawButton, rubberButton};
        ToggleGroup tools = new ToggleGroup();

        for (ToggleButton tool : toolsArr) {
            tool.setMinWidth(90);
            tool.setToggleGroup(tools);
            tool.setCursor(Cursor.HAND);
        }

        ColorPicker cpLine = new ColorPicker(Color.BLACK);

        Slider slider = new Slider(1, 50, 3);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        Label line_color = new Label("Line Color");
        Label line_width = new Label("3.0");

        VBox buttons = new VBox(10);
        buttons.getChildren().addAll(drawButton, rubberButton, line_color, cpLine, line_width, slider);
        buttons.setPadding(new Insets(5));
        buttons.setStyle("-fx-background-color: #999");
        buttons.setPrefWidth(100);

        // Canvas
        Canvas canvas = new Canvas(1080, 790);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1);

        canvas.setOnMousePressed(e -> {
            if (drawButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.beginPath();
                gc.lineTo(e.getX(), e.getY());
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (drawButton.isSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (drawButton.isSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                gc.closePath();
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
        });

        // Color pickers
        cpLine.setOnAction(e -> gc.setStroke(cpLine.getValue()));

        // Slider
        slider.valueProperty().addListener(e -> {
            double width = slider.getValue();
            line_width.setText(String.format("%.1f", width));
            gc.setLineWidth(width);
        });
        // Layout
        BorderPane pane = new BorderPane();
        pane.setRight(buttons);
        pane.setCenter(canvas);


        Scene scene = new Scene(pane, 1200, 800);
        primaryStage.setTitle("Paint");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
