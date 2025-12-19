package app.tracker;

import app.service.Navigator;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewGraphsController {
    private final Navigator navigator;

    @FXML
    private Label graphsLabel;

    public ViewGraphsController(Navigator navigator) {
        this.navigator = navigator;
    }

    @FXML
    public void initialize() {
        graphsLabel.setText("Здесь будут графики прогресса");
    }

    @FXML
    private void goToAddWorkout() throws Exception {
        navigator.showAddWorkout();
    }

    @FXML
    private void goToViewStatistics() throws Exception {
        navigator.showViewStatistics();
    }
}
