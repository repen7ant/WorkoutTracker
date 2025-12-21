package app.tracker;

import app.model.Exercise;
import app.model.ExerciseWithSets;
import app.service.Navigator;
import app.service.WorkoutService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static java.lang.String.valueOf;

public final class AddWorkoutController {
    private static final int SPACING_SMALL = 5;
    private static final int SPACING_MEDIUM = 10;
    private static final int PROMPT_LIMIT = 10;
    private static final int MAX_COMBO_WIDTH = 200;
    private static final int FIELD_MAX_WIDTH = 70;
    private static final int BODYWEIGHT_FIELD_WIDTH = 60;
    private static final int MIN_SETS = 1;
    private static final int MIN_EXERCISES = 1;
    private static final int SETS_CONTAINER_INDEX = 3;
    private static final int WEIGHT_FIELD_INDEX = 1;
    private static final int REPS_FIELD_INDEX = 3;

    private final Navigator navigator;
    @FXML
    private VBox workoutInfoContainer;
    @FXML
    private VBox exercisesContainer;

    private List<Exercise> allExercises;
    private final WorkoutService workoutService;

    public AddWorkoutController(final WorkoutService workoutService, final Navigator navigator) {
        this.workoutService = workoutService;
        this.navigator = navigator;
    }

    @FXML
    public final void initialize() {
        allExercises = workoutService.loadExercises();

        addWorkoutInfoSection();
        addExerciseSection();
    }

    private void addWorkoutInfoSection() {
        var workoutInfoBox = new VBox(SPACING_MEDIUM);
        workoutInfoBox.setAlignment(Pos.CENTER);

        var infoLabel = new Label("Session information");
        var datePicker = new DatePicker();
        datePicker.setPromptText("Select workout date");

        var bodyweight = new TextField();
        bodyweight.setMaxWidth(BODYWEIGHT_FIELD_WIDTH);
        bodyweight.setPromptText("BW");

        workoutInfoBox.getChildren().addAll(infoLabel, datePicker, bodyweight);
        workoutInfoContainer.getChildren().add(workoutInfoBox);
    }

    @FXML
    private void addExerciseSection() {
        var exerciseNumber = exercisesContainer.getChildren().size() + 1;

        var exerciseBox = new VBox(SPACING_MEDIUM);
        exerciseBox.setStyle("-fx-padding: 10; -fx-border-color: gray; "
                + "-fx-border-width: 2px; -fx-border-radius: 15px;");
        exerciseBox.setAlignment(Pos.CENTER);

        var exerciseLabel = new Label("Exercise " + exerciseNumber);

        List<String> exerciseNamesCopy = allExercises.stream()
                .map(Exercise::name)
                .toList();

        var exerciseCombo = new ComboBox<String>();
        exerciseCombo.setEditable(true);
        exerciseCombo.setPromptText("Enter exercise name");
        exerciseCombo.setMaxWidth(MAX_COMBO_WIDTH);

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
                        .limit(PROMPT_LIMIT)
                        .toList();
                items.addAll(filtered);
            }

            exerciseCombo.show();
        });

        exerciseCombo.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        editor.setText(newVal);
                    }
                });

        var infoBtn = new Button("info");
        infoBtn.setOnAction(e -> showExerciseInfo(exerciseCombo));

        var exerciseInput = new HBox(SPACING_MEDIUM, exerciseCombo, infoBtn);
        exerciseInput.setAlignment(Pos.CENTER);

        var setsContainer = new VBox(SPACING_SMALL);
        setsContainer.setAlignment(Pos.CENTER);

        addSet(setsContainer);

        var addSetBtn = new Button("+");
        addSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: #008000");
        addSetBtn.setOnAction(e -> addSet(setsContainer));

        var removeSetBtn = new Button("-");
        removeSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: #af3321");
        removeSetBtn.setOnAction(e -> {
            if (setsContainer.getChildren().size() > MIN_SETS) {
                setsContainer.getChildren().remove(setsContainer.getChildren().size() - 1);
                updateSetNumbers(setsContainer);
            }
        });

        var setButtons = new HBox(SPACING_MEDIUM, addSetBtn, removeSetBtn);
        setButtons.setAlignment(Pos.CENTER);

        exerciseBox.getChildren().addAll(exerciseLabel, exerciseInput,
                new Label("Sets"), setsContainer, setButtons);

        exercisesContainer.getChildren().add(exerciseBox);
    }

    private void addSet(final VBox setsContainer) {
        var setNumber = setsContainer.getChildren().size() + 1;

        var setBox = new HBox(SPACING_MEDIUM);
        setBox.setAlignment(Pos.CENTER);

        var setLabel = new Label(valueOf(setNumber));

        var weightField = new TextField();
        weightField.setPromptText("Weight");
        weightField.setMaxWidth(FIELD_MAX_WIDTH);

        var xLabel = new Label("×");

        var repsField = new TextField();
        repsField.setPromptText("Reps");
        repsField.setMaxWidth(FIELD_MAX_WIDTH);

        setBox.getChildren().addAll(setLabel, weightField, xLabel, repsField);
        setsContainer.getChildren().add(setBox);

        updateSetNumbers(setsContainer);
    }

    @FXML
    private void removeLastExercise() {
        if (exercisesContainer.getChildren().size() > MIN_EXERCISES) {
            exercisesContainer.getChildren()
                    .remove(exercisesContainer.getChildren().size() - 1);
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

    private void updateSetNumbers(final VBox setsContainer) {
        var i = 1;
        for (var node : setsContainer.getChildren()) {
            var box = (HBox) node;
            var lbl = (Label) box.getChildren().get(0);
            lbl.setText(String.valueOf((i++)));
        }
    }

    @FXML
    private void showExerciseInfo(final ComboBox<String> comboBox) {
        var exerciseName = comboBox.getEditor().getText().trim();
        if (exerciseName.isEmpty()) {
            showAlert("Enter exercise name");
            return;
        }

        var exercise = workoutService.findExercise(exerciseName);
        if (exercise == null) {
            showAlert("Exercise not found");
            return;
        }

        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exercise information");
        alert.setHeaderText(exercise.name());

        var content = new VBox(SPACING_MEDIUM);
        content.setStyle("-fx-padding: 20;");

        var musclesLabel = new Label("Targeted muscles: " + exercise.muscles());
        var descLabel = new Label("Description: " + exercise.description());

        descLabel.setWrapText(true);

        content.getChildren().addAll(musclesLabel, descLabel);
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    @FXML
    private void saveWorkout() {
        try {
            List<VBox> exerciseBoxes = new ArrayList<>();
            exercisesContainer.getChildren()
                    .forEach(node -> exerciseBoxes.add((VBox) node));

            if (hasInvalidSets(exerciseBoxes)) {
                showAlert("Fill in both weight and reps for each set.");
                return;
            }

            var infoBox = (VBox) workoutInfoContainer.getChildren().get(0);
            var datePicker = (DatePicker) infoBox.getChildren().get(1);
            var bodyweightField = (TextField) infoBox.getChildren().get(2);

            var localDate = datePicker.getValue();
            if (localDate == null) {
                showAlert("Select workout date.");
                return;
            }

            var date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            double bodyweight = Double.parseDouble(bodyweightField.getText());

            List<ExerciseWithSets> exList = new ArrayList<>();

            for (var node : exercisesContainer.getChildren()) {
                var exerciseBox = (VBox) node;

                var exerciseInput = (HBox) exerciseBox.getChildren().get(1);
                var combo = (ComboBox<String>) exerciseInput.getChildren().get(0);

                var exerciseName = combo.getEditor().getText();

                var setsContainer = (VBox) exerciseBox.getChildren().get(SETS_CONTAINER_INDEX);
                String setsString = buildSetsString(setsContainer);

                exList.add(new ExerciseWithSets(exerciseName, setsString));
            }

            workoutService.saveWorkout(date, bodyweight, exList);

            var ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Success");
            ok.setContentText("Workout saved");
            ok.showAndWait();
            navigator.showAddWorkout();

        } catch (Exception e) {
            showAlert("Fields are either not filled, or not valid.");
        }
    }

    private void showAlert(final String message) {
        var alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public boolean hasInvalidSets(final List<VBox> exerciseBoxes) {
        for (var node : exerciseBoxes) {
            var setsContainer = (VBox) node.getChildren().get(SETS_CONTAINER_INDEX);

            for (var setNode : setsContainer.getChildren()) {
                var setBox = (HBox) setNode;

                var weightField = (TextField) setBox.getChildren().get(WEIGHT_FIELD_INDEX);
                var repsField = (TextField) setBox.getChildren().get(REPS_FIELD_INDEX);

                var weightFilled = !weightField.getText().isBlank();
                var repsFilled = !repsField.getText().isBlank();

                if (!weightFilled || !repsFilled) {
                    return true;
                }
            }
        }
        return false;
    }

    public String buildSetsString(final VBox setsContainer) {
        var setsString = new StringBuilder();

        for (var setNode : setsContainer.getChildren()) {
            var setBox = (HBox) setNode;

            var weightFieldSet = (TextField) setBox.getChildren().get(WEIGHT_FIELD_INDEX);
            var repsFieldSet = (TextField) setBox.getChildren().get(REPS_FIELD_INDEX);

            if (!weightFieldSet.getText().isBlank() && !repsFieldSet.getText().isBlank()) {
                if (!setsString.isEmpty()) {
                    setsString.append("-");
                }
                setsString.append(weightFieldSet.getText())
                        .append("x")
                        .append(repsFieldSet.getText());
            }
        }
        return setsString.toString();
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

        try {
            boolean deleted = workoutService.deleteDatabase();
            if (deleted) {
                var ok = new Alert(Alert.AlertType.INFORMATION);
                ok.setTitle("Success");
                ok.setHeaderText(null);
                ok.setContentText("All data deleted.");
                ok.showAndWait();
            } else {
                var warn = new Alert(Alert.AlertType.WARNING);
                warn.setTitle("File not found");
                warn.setHeaderText("Database file not found.");
                warn.setContentText("Expected path:\n"
                        + Paths.get("workouts.db").toAbsolutePath());
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

    @FXML
    private void goToViewStatistics() throws Exception {
        navigator.showViewStatistics();
    }

    @FXML
    private void goToViewGraphs() throws Exception {
        navigator.showViewGraphs();
    }
}