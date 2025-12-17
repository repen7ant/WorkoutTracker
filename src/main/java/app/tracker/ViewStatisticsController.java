package app.tracker;

import app.database.DatabaseHelper;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;

import com.j256.ormlite.dao.Dao;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ViewStatisticsController {

    @FXML private ComboBox<String> modeCombo;
    @FXML private TextField searchField;
    @FXML private TableView<Map<String, Object>> table;

    private static final Logger log = LoggerFactory.getLogger(ViewStatisticsController.class);
    private final ObservableList<Map<String, Object>> backingData = FXCollections.observableArrayList();

    private List<WorkoutSession> sessions;
    private List<WorkoutExercise> exercises;

    @FXML
    public void initialize() {
        try {
            Dao<WorkoutSession, Integer> sDao = DatabaseHelper.sessionDao;
            Dao<WorkoutExercise, Integer> eDao = DatabaseHelper.exerciseDao;

            sessions = sDao.queryForAll();
            exercises = eDao.queryForAll();
        } catch (SQLException e) {
            log.error("statistics page init error: {}", e.getMessage(), e);
        }

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

        switch (mode.charAt(0)) {
            case '1' -> buildBodyweightByDate();
            case '2' -> buildAllExercisesAllSets();
            case '3' -> buildAllExercisesBestSet();
            case '4' -> buildAllSessionsSummary();
        }

        table.setItems(backingData);
    }

    private <T> TableColumn<Map<String, Object>, T> col(String title, String key, int prefWidth, Class<T> type) {
        TableColumn<Map<String, Object>, T> c = new TableColumn<>(title);
        c.setPrefWidth(prefWidth);
        c.setCellValueFactory(data ->
                new SimpleObjectProperty<>(type.cast(data.getValue().get(key)))
        );
        return c;
    }

    private TableColumn<Map<String, Object>, Date> dateCol(int prefWidth) {
        TableColumn<Map<String, Object>, Date> c = col("Date", "date", prefWidth, Date.class);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        c.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : df.format(item));
            }
        });
        return c;
    }

    private void buildBodyweightByDate() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(col("Session ID", "id", 80, Integer.class));
        table.getColumns().add(dateCol(150));
        table.getColumns().add(col("Bodyweight", "bw", 120, Double.class));

        for (WorkoutSession s : sessions) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", s.getId());
            row.put("date", s.getDate());
            row.put("bw", s.getBodyweight());
            backingData.add(row);
        }
    }

    private void buildAllExercisesAllSets() {
        String q = searchField.getText();
        List<WorkoutExercise> filtered = filterByName(q);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> dates = sessions.stream()
                .map(s -> df.format(s.getDate()))
                .collect(Collectors.toCollection(TreeSet::new));

        table.getColumns().add(col("Exercise ID", "id", 100, Integer.class));
        table.getColumns().add(col("Exercise", "name", 250, String.class));
        for (String d : dates) {
            table.getColumns().add(col(d, d, 300, String.class));
        }

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        for (var entry : byName.entrySet()) {
            List<WorkoutExercise> exList = entry.getValue();

            Map<String, Object> row = new HashMap<>();
            row.put("id", exList.get(0).getId());
            row.put("name", entry.getKey());

            for (WorkoutExercise ex : exList) {
                String d = df.format(ex.getSession().getDate());
                row.put(d, ex.getSetsString());
            }
            backingData.add(row);
        }
    }

    private void buildAllExercisesBestSet() {
        String q = searchField.getText();
        List<WorkoutExercise> filtered = filterByName(q);

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> dates = sessions.stream()
                .map(s -> df.format(s.getDate()))
                .collect(Collectors.toCollection(TreeSet::new));

        table.getColumns().add(col("Exercise ID", "id", 80, Integer.class));
        table.getColumns().add(col("Exercise", "name", 200, String.class));
        for (String d : dates) {
            table.getColumns().add(col(d, d, 120, String.class));
        }

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        for (var entry : byName.entrySet()) {
            List<WorkoutExercise> exList = entry.getValue();

            Map<String, Object> row = new HashMap<>();
            row.put("id", exList.get(0).getId());
            row.put("name", entry.getKey());

            for (WorkoutExercise ex : exList) {
                String d = df.format(ex.getSession().getDate());
                row.put(d, pickBestSet(ex.getSetsString()));
            }
            backingData.add(row);
        }
    }

    private void buildAllSessionsSummary() {
        table.getColumns().add(col("Session ID", "sid", 80, Integer.class));
        table.getColumns().add(dateCol(120));
        table.getColumns().add(col("Exercise", "name", 200, String.class));
        table.getColumns().add(col("Sets", "sets", 200, String.class));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        String q = searchField.getText();

        for (WorkoutSession s : sessions) {
            for (WorkoutExercise ex : exercises) {
                if (ex.getSession() == null || ex.getSession().getId() != s.getId()) continue;
                if (q != null && !q.isBlank() &&
                        !ex.getName().toLowerCase().contains(q.toLowerCase())) continue;

                Map<String, Object> row = new HashMap<>();
                row.put("sid", s.getId());
                row.put("date", s.getDate());
                row.put("name", ex.getName());
                row.put("sets", ex.getSetsString());
                backingData.add(row);
            }
        }
    }

    private String pickBestSet(String sets) {
        if (sets == null || sets.isBlank()) return "";
        String best = "";
        int bestW = -1, bestR = -1;
        for (String part : sets.split("-")) {
            String[] wr = part.split("x");
            if (wr.length != 2) continue;
            try {
                int w = Integer.parseInt(wr[0]);
                int r = Integer.parseInt(wr[1]);
                if (w > bestW || (w == bestW && r > bestR)) {
                    bestW = w;
                    bestR = r;
                    best = w + "x" + r;
                }
            } catch (NumberFormatException ignore) {}
        }
        return best;
    }

    private List<WorkoutExercise> filterByName(String q) {
        if (q == null || q.isBlank()) return exercises;
        String lower = q.toLowerCase();
        return exercises.stream()
                .filter(ex -> ex.getName().toLowerCase().contains(lower))
                .toList();
    }

    @FXML private void goToAddWorkout() throws Exception { MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { MainApplication.INSTANCE.showViewGraphs(); }
}
