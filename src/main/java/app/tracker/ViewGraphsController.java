package app.tracker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewGraphsController {
    @FXML private Label graphsLabel;

    @FXML
    public void initialize() {
        graphsLabel.setText("Здесь будут графики прогресса");
    }


    @FXML private void goToAddWorkout() throws Exception { app.tracker.MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { app.tracker.MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { app.tracker.MainApplication.INSTANCE.showViewGraphs(); }
}
