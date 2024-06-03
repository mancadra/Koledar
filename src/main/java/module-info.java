module com.example.koledarapp {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.koledarapp to javafx.fxml;
    exports com.example.koledarapp;
}