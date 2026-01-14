module org.isep.project_work {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.sql;

    opens controller to javafx.fxml;
    opens org.isep.project_work to javafx.fxml;
    opens model to javafx.base, com.fasterxml.jackson.databind;
    opens service to javafx.base, com.fasterxml.jackson.databind;

    exports controller;
    exports org.isep.project_work;
    exports model;
    exports service;
}