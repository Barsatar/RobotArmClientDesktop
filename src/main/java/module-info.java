module com.example.robotarmdesktop {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.robotarmdesktop to javafx.fxml;
    exports com.example.robotarmdesktop;
}