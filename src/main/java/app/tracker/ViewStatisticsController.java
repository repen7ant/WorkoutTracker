package app.tracker;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewStatisticsController {
    @FXML private Label statsLabel;

    @FXML
    public void initialize() {
        statsLabel.setText("Здесь будет статистика тренировок");
    }

    @FXML private void goToAddWorkout() throws Exception { app.tracker.MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { app.tracker.MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { app.tracker.MainApplication.INSTANCE.showViewGraphs(); }
}
