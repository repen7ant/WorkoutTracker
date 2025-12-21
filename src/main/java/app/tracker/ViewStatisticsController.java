package app.tracker;

import app.service.Navigator;
import app.service.StatisticsService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ViewStatisticsController {
    private static final int COLUMN_WIDTH_SMALL = 80;
    private static final int COLUMN_WIDTH_MEDIUM = 100;
    private static final int COLUMN_WIDTH_LARGE = 120;
    private static final int COLUMN_WIDTH_XLARGE = 150;
    private static final int COLUMN_WIDTH_XXLARGE = 200;
    private static final int COLUMN_WIDTH_XXXLARGE = 250;
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private final Navigator navigator;

    @FXML
    private ComboBox<String> modeCombo;
    @FXML
    private TextField searchField;
    @FXML
    private TableView<Map<String, Object>> table;

    private static final Logger LOG = LoggerFactory.getLogger(ViewStatisticsController.class);
    private final ObservableList<Map<String, Object>> backingData = FXCollections.observableArrayList();
    private final StatisticsService statisticsService;

    public ViewStatisticsController(final StatisticsService statisticsService, final Navigator navigator) {
        this.statisticsService = statisticsService;
        this.navigator = navigator;
    }

    @FXML
    public final void initialize() {
        modeCombo.getItems().addAll("1) Bodyweight by date",
                "2) All exercises, all sets",
                "3) All exercises, best set",
                "4) All sessions summary");
        modeCombo.getSelectionModel().selectFirst();

        modeCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> reloadTable());
        searchField.textProperty().addListener((obs, o, n) -> reloadTable());

        reloadTable();
    }

    private void reloadTable() {
        String mode = modeCombo.getValue();
        if (mode == null) {
            return;
        }

        table.getColumns().clear();
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        backingData.clear();

        try {
            switch (mode.charAt(0)) {
                case '1':
                    loadBodyweightByDate();
                    break;
                case '2':
                    loadAllExercisesAllSets();
                    break;
                case '3':
                    loadAllExercisesBestSet();
                    break;
                case '4':
                    loadAllSessionsSummary();
                    break;
                default:
                    LOG.warn("Unknown mode selected: {}", mode);
                    break;
            }
            table.setItems(backingData);
        } catch (SQLException e) {
            LOG.error("Error loading statistics: {}", e.getMessage(), e);
        }
    }

    private <T> TableColumn<Map<String, Object>, T> col(final String title,
                                                        final String key, final int prefWidth, final Class<T> type) {
        TableColumn<Map<String, Object>, T> column = new TableColumn<>(title);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(data -> new SimpleObjectProperty<>(
                type.cast(data.getValue().get(key))));
        return column;
    }

    private TableColumn<Map<String, Object>, java.util.Date> dateCol(final int prefWidth) {
        TableColumn<Map<String, Object>, java.util.Date> column = col("Date",
                "date", prefWidth, java.util.Date.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        column.setCellFactory(tableColumn -> new TableCell<Map<String, Object>, java.util.Date>() {
            @Override
            protected void updateItem(final java.util.Date item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });
        return column;
    }

    private void loadBodyweightByDate() throws SQLException {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(col("Session ID", "id",
                COLUMN_WIDTH_SMALL, Integer.class));
        table.getColumns().add(dateCol(COLUMN_WIDTH_XLARGE));
        table.getColumns().add(col("Bodyweight", "bw",
                COLUMN_WIDTH_LARGE, Double.class));

        backingData.addAll(statisticsService.getBodyweightByDate());
    }

    private void loadAllExercisesAllSets() throws SQLException {
        String query = searchField.getText();
        List<Map<String, Object>> data = statisticsService.getAllExercisesAllSets(query);

        table.getColumns().add(col("Exercise ID", "id",
                COLUMN_WIDTH_MEDIUM, Integer.class));
        table.getColumns().add(col("Exercise", "name",
                COLUMN_WIDTH_XXXLARGE, String.class));

        Set<String> dates = statisticsService.getAllDates();
        for (String date : dates) {
            table.getColumns().add(col(date, date,
                    COLUMN_WIDTH_XXLARGE, String.class));
        }

        backingData.addAll(data);
    }

    private void loadAllExercisesBestSet() throws SQLException {
        String query = searchField.getText();
        List<Map<String, Object>> data = statisticsService.getAllExercisesBestSet(query);

        table.getColumns().add(col("Exercise ID", "id",
                COLUMN_WIDTH_SMALL, Integer.class));
        table.getColumns().add(col("Exercise", "name",
                COLUMN_WIDTH_XXLARGE, String.class));

        Set<String> dates = statisticsService.getAllDates();
        for (String date : dates) {
            table.getColumns().add(col(date, date,
                    COLUMN_WIDTH_LARGE, String.class));
        }

        backingData.addAll(data);
    }

    private void loadAllSessionsSummary() throws SQLException {
        table.getColumns().add(col("Session ID", "sid",
                COLUMN_WIDTH_SMALL, Integer.class));
        table.getColumns().add(dateCol(COLUMN_WIDTH_LARGE));
        table.getColumns().add(col("Exercise", "name",
                COLUMN_WIDTH_XXLARGE, String.class));
        table.getColumns().add(col("Sets", "sets",
                COLUMN_WIDTH_XXLARGE, String.class));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String query = searchField.getText();
        backingData.addAll(statisticsService.getAllSessionsSummary(query));
    }

    @FXML
    private void goToAddWorkout() throws Exception {
        navigator.showAddWorkout();
    }

    @FXML
    private void goToViewGraphs() throws Exception {
        navigator.showViewGraphs();
    }
}