package app.tracker;

import app.database.DatabaseHelper;
import app.model.Exercise;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import app.service.ExerciseService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import static java.lang.String.valueOf;

public class AddWorkoutController {
    @FXML private VBox workoutInfoContainer;
    @FXML private VBox exercisesContainer;
    private List<Exercise> allExercises;

    @FXML
    public void initialize() {
        ExerciseService.loadExercises();
        allExercises = ExerciseService.getExercises();

        addWorkoutInfoSection();
        addExerciseSection();
    }

    private void addWorkoutInfoSection() {
        var workoutInfoBox = new VBox(10);
        workoutInfoBox.setAlignment(Pos.CENTER);

        var infoLabel = new Label("Session information");
        var datePicker = new DatePicker();
        datePicker.setPromptText("Select workout date");

        var bodyweight = new TextField();
        bodyweight.setMaxWidth(60);
        bodyweight.setPromptText("BW");

        workoutInfoBox.getChildren().addAll(infoLabel, datePicker, bodyweight);
        workoutInfoContainer.getChildren().add(workoutInfoBox);
    }

    @FXML
    private void addExerciseSection() {

        var exerciseNumber = exercisesContainer.getChildren().size() + 1;

        var exerciseBox = new VBox(10);
        exerciseBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 2px; -fx-border-radius: 15px;");
        exerciseBox.setAlignment(Pos.CENTER);

        var exerciseLabel = new Label("Exercise " + exerciseNumber);

        List<String> exerciseNamesCopy = allExercises.stream()
                .map(Exercise::name)
                .toList();

        var exerciseCombo = new ComboBox<String>();
        exerciseCombo.setEditable(true);
        exerciseCombo.setPromptText("Enter exercise name");
        exerciseCombo.setMaxWidth(200);

        exerciseCombo.setItems(FXCollections.observableArrayList(exerciseNamesCopy));

        TextField editor = exerciseCombo.getEditor();
        editor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (exerciseCombo.getValue() != null && newVal.equals(exerciseCombo.getValue())) {
                return;
            }

            var items = exerciseCombo.getItems();
            items.clear();

            if (newVal == null || newVal.trim().isEmpty()) {
                items.addAll(exerciseNamesCopy);
            } else {
                var filtered = exerciseNamesCopy.stream()
                        .filter(name -> name.toLowerCase().contains(newVal.toLowerCase()))
                        .limit(10)
                        .toList();
                items.addAll(filtered);
            }

            exerciseCombo.show();
        });

        exerciseCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                editor.setText(newVal);
            }
        });

        var infoBtn = new Button("info");
        infoBtn.setOnAction(e -> showExerciseInfo(exerciseCombo));

        var exerciseInput = new HBox(10, exerciseCombo, infoBtn);
        exerciseInput.setAlignment(Pos.CENTER);

        var setsContainer = new VBox(5);
        setsContainer.setAlignment(Pos.CENTER);

        addSet(setsContainer);

        var addSetBtn = new Button("+");
        addSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: #008000");
        addSetBtn.setOnAction(e -> addSet(setsContainer));

        var removeSetBtn = new Button("-");
        removeSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: #af3321");
        removeSetBtn.setOnAction(e -> {
            if (setsContainer.getChildren().size() > 1) {
                setsContainer.getChildren().remove(setsContainer.getChildren().size() - 1);
                updateSetNumbers(setsContainer);
            }
        });

        var setButtons = new HBox(10, addSetBtn, removeSetBtn);
        setButtons.setAlignment(Pos.CENTER);

        exerciseBox.getChildren().addAll(
                exerciseLabel,
                exerciseInput,
                new Label("Sets"),
                setsContainer,
                setButtons
        );

        exercisesContainer.getChildren().add(exerciseBox);
    }

    private void addSet(VBox setsContainer) {
        var setNumber = setsContainer.getChildren().size() + 1;

        var setBox = new HBox(10);
        setBox.setAlignment(Pos.CENTER);

        var setLabel = new Label(valueOf(setNumber));

        var weightField = new TextField();
        weightField.setPromptText("Weight");
        weightField.setMaxWidth(70);

        var xLabel = new Label("×");

        var repsField = new TextField();
        repsField.setPromptText("Reps");
        repsField.setMaxWidth(70);

        setBox.getChildren().addAll(setLabel, weightField, xLabel, repsField);
        setsContainer.getChildren().add(setBox);

        updateSetNumbers(setsContainer);
    }

    @FXML
    private void removeLastExercise() {
        if (exercisesContainer.getChildren().size() > 1) {
            exercisesContainer.getChildren().remove(exercisesContainer.getChildren().size() - 1);
            updateExerciseNumbers();
        }
    }

    private void updateExerciseNumbers() {
        var i = 1;
        for (var node : exercisesContainer.getChildren()) {
            var box = (VBox) node;
            var lbl = (Label) box.getChildren().get(0);
            lbl.setText("Exercise " + (i++));
        }
    }

    private void updateSetNumbers(VBox setsContainer) {
        var i = 1;
        for (var node : setsContainer.getChildren()) {
            var box = (HBox) node;
            var lbl = (Label) box.getChildren().get(0);
            lbl.setText(String.valueOf((i++)));
        }
    }

    @FXML
    private void showExerciseInfo(ComboBox<String> comboBox) {
        var exerciseName = comboBox.getEditor().getText().trim();
        if (exerciseName.isEmpty()) {
            showAlert("Enter exercise name");
            return;
        }

        var exercise = ExerciseService.findByName(exerciseName);
        if (exercise == null) {
            showAlert("Exercise not found");
            return;
        }

        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exercise information");
        alert.setHeaderText(exercise.name());

        var content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        var musclesLabel = new Label("Targeted muscles: " + exercise.muscles());
        var descLabel = new Label("Description: " + exercise.description());

        descLabel.setWrapText(true);

        content.getChildren().addAll(musclesLabel, descLabel);
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    private void showAlert(String message) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveWorkout() {
        try {
            if (hasInvalidSets()) {
                showAlert("Fill in both weight and reps for each set.");
                return;
            }

            var infoBox = (VBox) workoutInfoContainer.getChildren().get(0);

            var datePicker = (DatePicker)infoBox.getChildren().get(1);
            var bodyweightField = (TextField)infoBox.getChildren().get(2);

            var localDate = datePicker.getValue();
            if (localDate == null) {
                showAlert("Select workout date.");
                return;
            }

            var date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            double bodyweight = Double.parseDouble(bodyweightField.getText());

            for (var node : exercisesContainer.getChildren()) {
                var exerciseBox = (VBox) node;

                var exerciseInput = (HBox) exerciseBox.getChildren().get(1);
                var combo = (ComboBox<String>) exerciseInput.getChildren().get(0);

                var exerciseName = combo.getEditor().getText();

                var setsContainer = (VBox) exerciseBox.getChildren().get(3);
                var setsString = new StringBuilder();

                for (var setNode : setsContainer.getChildren()) {
                    var setBox = (HBox) setNode;

                    var weightFieldSet = (TextField) setBox.getChildren().get(1);
                    var repsFieldSet = (TextField) setBox.getChildren().get(3);

                    if (!weightFieldSet.getText().isBlank() && !repsFieldSet.getText().isBlank()) {
                        if (!setsString.isEmpty())
                            setsString.append("-");
                        setsString
                                .append(weightFieldSet.getText())
                                .append("x")
                                .append(repsFieldSet.getText());
                    }
                }

                var session = new WorkoutSession(date, bodyweight);
                DatabaseHelper.sessionDao.create(session);
                var ex = new WorkoutExercise(exerciseName, setsString.toString(), session);
                DatabaseHelper.exerciseDao.create(ex);
            }

            var ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setContentText("Workout saved");
            ok.showAndWait();
            MainApplication.INSTANCE.showAddWorkout();

        } catch (Exception e) {
            showAlert("Fields are either not filled, or not valid.");
        }
    }


    private boolean hasInvalidSets() {
        for (var node : exercisesContainer.getChildren()) {
            var exerciseBox = (VBox) node;

            var setsContainer = (VBox) exerciseBox.getChildren().get(3);

            for (var setNode : setsContainer.getChildren()) {
                var setBox = (HBox) setNode;

                var weightField = (TextField) setBox.getChildren().get(1);
                var repsField   = (TextField) setBox.getChildren().get(3);

                var weightFilled = !weightField.getText().isBlank();
                var repsFilled   = !repsField.getText().isBlank();

                if (!weightFilled || !repsFilled) {
                    return true;
                }
            }
        }
        return false;
    }

    @FXML
    private void deleteDB() {
        var confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText("Delete all saved data?");
        confirm.setContentText("This will permanently delete all workouts. Continue?");

        var result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        DatabaseHelper.close();

        try {
            var dbUrl = "jdbc:sqlite:workouts.db";
            var pathPart = dbUrl.replace("jdbc:sqlite:", "");
            var dbPath = Paths.get(pathPart).toAbsolutePath();

            if (Files.exists(dbPath)) {
                Files.delete(dbPath);
                var ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Success");
                ok.setHeaderText(null);
                ok.setContentText("All data deleted.");
                ok.showAndWait();
                DatabaseHelper.init();
            } else {
                var warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("File not found");
                warn.setHeaderText("Database file not found.");
                warn.setContentText("Expected path:\n" + dbPath);
                warn.showAndWait();
            }
        } catch (Exception ex) {
            var err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Error");
            err.setHeaderText("Could not delete database file.");
            err.setContentText(ex.getMessage());
            err.showAndWait();
        }
    }

    @FXML private void goToAddWorkout() throws Exception { app.tracker.MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { app.tracker.MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { app.tracker.MainApplication.INSTANCE.showViewGraphs(); }
}
