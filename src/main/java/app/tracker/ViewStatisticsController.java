package app.tracker;

import app.database.DatabaseHelper;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;

import app.service.ExerciseService;
import com.j256.ormlite.dao.Dao;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class ViewStatisticsController {

    @FXML private ComboBox<String> modeCombo;
    @FXML private TextField searchField;
    @FXML private TableView<Map<String, String>> table;

    private static final Logger log = Logger.getLogger(ExerciseService.class.getName());
    private final ObservableList<Map<String, String>> backingData = FXCollections.observableArrayList();

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
            log.severe("statistics page init error: " + e.getMessage());
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

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
        backingData.clear();

        switch (mode.charAt(0)) {
            case '1' -> buildBodyweightByDate();
            case '2' -> buildAllExercisesAllSets();
            case '3' -> buildAllExercisesBestSet();
            case '4' -> buildAllSessionsSummary();
        }

        table.setItems(backingData);
    }

    private TableColumn<Map<String, String>, String> col(String title, String key, int prefWidth) {
        TableColumn<Map<String, String>, String> c = new TableColumn<>(title);
        c.setPrefWidth(prefWidth);
        c.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getOrDefault(key, "")));
        return c;
    }

    private void buildBodyweightByDate() {
        table.getColumns().add(col("Session ID", "id", 80));
        table.getColumns().add(col("Date", "date", 150));
        table.getColumns().add(col("Bodyweight", "bw", 120));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-dd-MM");

        for (WorkoutSession s : sessions) {
            Map<String, String> row = new HashMap<>();
            row.put("id", String.valueOf(s.getId()));
            row.put("date", df.format(s.getDate()));
            row.put("bw", String.valueOf(s.getBodyweight()));
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

        table.getColumns().add(col("Exercise ID", "id", 80));
        table.getColumns().add(col("Exercise", "name", 200));
        for (String d : dates) {
            table.getColumns().add(col(d, d, 180));
        }

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        for (var entry : byName.entrySet()) {
            String name = entry.getKey();
            List<WorkoutExercise> exList = entry.getValue();

            Map<String, String> row = new HashMap<>();
            row.put("id", String.valueOf(exList.get(0).getId()));
            row.put("name", name);

            for (WorkoutExercise ex : exList) {
                WorkoutSession s = ex.getSession();
                String d = df.format(s.getDate());
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

        table.getColumns().add(col("Exercise ID", "id", 80));
        table.getColumns().add(col("Exercise", "name", 200));
        for (String d : dates) {
            table.getColumns().add(col(d, d, 120));
        }

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        for (var entry : byName.entrySet()) {
            String name = entry.getKey();
            List<WorkoutExercise> exList = entry.getValue();

            Map<String, String> row = new HashMap<>();
            row.put("id", String.valueOf(exList.get(0).getId()));
            row.put("name", name);

            for (WorkoutExercise ex : exList) {
                WorkoutSession s = ex.getSession();
                String d = df.format(s.getDate());

                String best = pickBestSet(ex.getSetsString());
                row.put(d, best);
            }
            backingData.add(row);
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

    private void buildAllSessionsSummary() {
        table.getColumns().add(col("Session ID", "sid", 80));
        table.getColumns().add(col("Date", "date", 120));
        table.getColumns().add(col("Exercise", "name", 200));
        table.getColumns().add(col("Sets", "sets", 200));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String q = searchField.getText();

        for (WorkoutSession s : sessions) {
            List<WorkoutExercise> exForSession = exercises.stream()
                    .filter(ex -> ex.getSession() != null && ex.getSession().getId() == s.getId())
                    .filter(ex -> q == null || q.isBlank() ||
                            ex.getName().toLowerCase().contains(q.toLowerCase()))
                    .toList();

            for (WorkoutExercise ex : exForSession) {
                Map<String, String> row = new HashMap<>();
                row.put("sid", String.valueOf(s.getId()));
                row.put("date", df.format(s.getDate()));
                row.put("name", ex.getName());
                row.put("sets", ex.getSetsString());
                backingData.add(row);
            }
        }
    }

    private List<WorkoutExercise> filterByName(String q) {
        if (q == null || q.isBlank()) return exercises;
        String lower = q.toLowerCase();
        return exercises.stream()
                .filter(ex -> ex.getName().toLowerCase().contains(lower))
                .toList();
    }

    // навигация
    @FXML private void goToAddWorkout() throws Exception { MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { MainApplication.INSTANCE.showViewGraphs(); }
}
