module com.example.robotarmdesktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;


    opens com.example.robotarmdesktop to javafx.fxml;
    exports com.example.robotarmdesktop;
}