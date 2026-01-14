package controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.Event;

import java.time.LocalDate;

public class AddEventDialogController {

    @FXML
    private TextField titleField;
    @FXML
    private TextField descriptionField;
    @FXML
    private DatePicker datePicker;

    public Event getResult() {
        String title = titleField.getText();
        String desc = descriptionField.getText();
        LocalDate date = datePicker.getValue();

        if (title.isEmpty() || date == null)
            return null;

        return new Event(title, desc, date, null);
    }
}
