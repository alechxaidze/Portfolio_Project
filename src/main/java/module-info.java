module org.isep.project_work {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;

    opens org.isep.project_work to javafx.fxml;
    exports org.isep.project_work;
}