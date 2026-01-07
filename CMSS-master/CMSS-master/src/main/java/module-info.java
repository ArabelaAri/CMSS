module com.projekt.paint {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.swing;
    requires java.desktop;

    opens com.projekt.paint to javafx.fxml;
    exports com.projekt.paint;
}
