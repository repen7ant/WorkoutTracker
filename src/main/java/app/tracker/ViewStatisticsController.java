package app.tracker;

import app.service.StatisticsService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ViewStatisticsController {
    @FXML private ComboBox<String> modeCombo;
    @FXML private TextField searchField;
    @FXML private TableView<Map<String, Object>> table;

    private static final Logger log = LoggerFactory.getLogger(ViewStatisticsController.class);
    private final ObservableList<Map<String, Object>> backingData = FXCollections.observableArrayList();
    private final StatisticsService statisticsService;

    public ViewStatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @FXML
    public void initialize() {
        modeCombo.getItems().addAll(
                "1) Bodyweight by date",
                "2) All exercises, all sets",
                "3) All exercises, best set",
                "4) All sessions summary"
        );
        modeCombo.getSelectionModel().selectFirst();

        modeCombo.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> reloadTable());
        searchField.textProperty().addListener((obs, o, n) -> reloadTable());

        reloadTable();
    }

    private void reloadTable() {
        String mode = modeCombo.getValue();
        if (mode == null) return;

        table.getColumns().clear();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        backingData.clear();

        try {
            switch (mode.charAt(0)) {
                case '1' -> loadBodyweightByDate();
                case '2' -> loadAllExercisesAllSets();
                case '3' -> loadAllExercisesBestSet();
                case '4' -> loadAllSessionsSummary();
            }
            table.setItems(backingData);
        } catch (SQLException e) {
            log.error("Error loading statistics: {}", e.getMessage(), e);
        }
    }

    private <T> TableColumn<Map<String, Object>, T> col(String title, String key, int prefWidth, Class<T> type) {
        TableColumn<Map<String, Object>, T> c = new TableColumn<>(title);
        c.setPrefWidth(prefWidth);
        c.setCellValueFactory(data ->
                new SimpleObjectProperty<>(type.cast(data.getValue().get(key)))
        );
        return c;
    }

    private TableColumn<Map<String, Object>, java.util.Date> dateCol(int prefWidth) {
        TableColumn<Map<String, Object>, java.util.Date> c = col("Date", "date", prefWidth, java.util.Date.class);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(java.util.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : df.format(item));
            }
        });
        return c;
    }

    private void loadBodyweightByDate() throws SQLException {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(col("Session ID", "id", 80, Integer.class));
        table.getColumns().add(dateCol(150));
        table.getColumns().add(col("Bodyweight", "bw", 120, Double.class));

        backingData.addAll(statisticsService.getBodyweightByDate());
    }

    private void loadAllExercisesAllSets() throws SQLException {
        String query = searchField.getText();
        List<Map<String, Object>> data = statisticsService.getAllExercisesAllSets(query);

        table.getColumns().add(col("Exercise ID", "id", 100, Integer.class));
        table.getColumns().add(col("Exercise", "name", 250, String.class));

        Set<String> dates = statisticsService.getAllDates();
        for (String d : dates) {
            table.getColumns().add(col(d, d, 200, String.class));
        }

        backingData.addAll(data);
    }

    private void loadAllExercisesBestSet() throws SQLException {
        String query = searchField.getText();
        List<Map<String, Object>> data = statisticsService.getAllExercisesBestSet(query);

        table.getColumns().add(col("Exercise ID", "id", 80, Integer.class));
        table.getColumns().add(col("Exercise", "name", 200, String.class));

        Set<String> dates = statisticsService.getAllDates();
        for (String d : dates) {
            table.getColumns().add(col(d, d, 120, String.class));
        }

        backingData.addAll(data);
    }

    private void loadAllSessionsSummary() throws SQLException {
        table.getColumns().add(col("Session ID", "sid", 80, Integer.class));
        table.getColumns().add(dateCol(120));
        table.getColumns().add(col("Exercise", "name", 200, String.class));
        table.getColumns().add(col("Sets", "sets", 200, String.class));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String query = searchField.getText();
        backingData.addAll(statisticsService.getAllSessionsSummary(query));
    }

    @FXML private void goToAddWorkout() throws Exception { MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { MainApplication.INSTANCE.showViewGraphs(); }
}
