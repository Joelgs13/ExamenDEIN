module com.example.examendein {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.examendein to javafx.fxml;
    exports com.example.examendein;
    exports com.example.examendein.controller;
    opens com.example.examendein.controller to javafx.fxml;
}