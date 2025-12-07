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

import java.io.File;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

public class AddWorkoutController {
    public VBox workoutInfoContainer;
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
        VBox workoutInfoBox = new VBox(10);
        workoutInfoBox.setAlignment(Pos.CENTER);

        var dateLabel = new Label("Session information");
        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Select workout date");

        var bodyweight = new TextField();
        bodyweight.setMaxWidth(60);
        bodyweight.setPromptText("BW");

        workoutInfoBox.getChildren().addAll(dateLabel, datePicker, bodyweight);
        workoutInfoContainer.getChildren().add(workoutInfoBox);
    }

    @FXML
    private void addExerciseSection() {

        int exerciseNumber = exercisesContainer.getChildren().size() + 1;

        VBox exerciseBox = new VBox(10);
        exerciseBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-radius: 15px;");
        exerciseBox.setAlignment(Pos.CENTER);

        Label exerciseLabel = new Label("Exercise " + exerciseNumber);

        ComboBox<String> exerciseCombo = new ComboBox<>();
        exerciseCombo.setEditable(true);
        exerciseCombo.setPromptText("Enter exercise name");
        exerciseCombo.setMaxWidth(200);

        ObservableList<String> allItems =
                FXCollections.observableArrayList(
                        allExercises.stream().map(Exercise::name).collect(Collectors.toList())
                );

        FilteredList<String> filteredItems = new FilteredList<>(allItems, s -> true);
        exerciseCombo.setItems(filteredItems);
        exerciseCombo.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {

            if (exerciseCombo.getValue() != null && newVal.equals(exerciseCombo.getValue()))
                return;

            filteredItems.setPredicate(item ->
                    newVal == null || newVal.isEmpty() ||
                            item.toLowerCase().contains(newVal.toLowerCase())
            );
            exerciseCombo.show();
        });

        exerciseCombo.setOnAction(e -> {
            String selected = exerciseCombo.getSelectionModel().getSelectedItem();
            if (selected != null)
                exerciseCombo.getEditor().setText(selected);
        });

        Button infoBtn = new Button("info");
        infoBtn.setOnAction(e -> showExerciseInfo(exerciseCombo));

        HBox exerciseInput = new HBox(10, exerciseCombo, infoBtn);
        exerciseInput.setAlignment(Pos.CENTER);

        VBox setsContainer = new VBox(5);
        setsContainer.setAlignment(Pos.CENTER);

        addSet(setsContainer);

        Button addSetBtn = new Button("+");
        addSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: rgba(0, 255, 0, 0.6)");
        addSetBtn.setOnAction(e -> addSet(setsContainer));

        Button removeSetBtn = new Button("-");
        removeSetBtn.setStyle("-fx-min-width: 50; -fx-background-color: rgba(255, 0, 0, 0.6)");
        removeSetBtn.setOnAction(e -> {
            if (setsContainer.getChildren().size() > 1) {
                setsContainer.getChildren().remove(setsContainer.getChildren().size() - 1);
                updateSetNumbers(setsContainer);
            }
        });

        HBox setButtons = new HBox(10, addSetBtn, removeSetBtn);
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

    // --- Добавление подхода с нумерацией ---
    private void addSet(VBox setsContainer) {
        int setNumber = setsContainer.getChildren().size() + 1;

        HBox setBox = new HBox(10);
        setBox.setAlignment(Pos.CENTER);

        Label setLabel = new Label(valueOf(setNumber));

        TextField weightField = new TextField();
        weightField.setPromptText("Weight");
        weightField.setMaxWidth(70);

        Label xLabel = new Label("×");

        TextField repsField = new TextField();
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
        int i = 1;
        for (var node : exercisesContainer.getChildren()) {
            VBox box = (VBox) node;
            Label lbl = (Label) box.getChildren().get(0);
            lbl.setText("Exercise " + (i++));
        }
    }

    private void updateSetNumbers(VBox setsContainer) {
        int i = 1;
        for (var node : setsContainer.getChildren()) {
            HBox box = (HBox) node;
            Label lbl = (Label) box.getChildren().get(0);
            lbl.setText(String.valueOf((i++)));
        }
    }

    @FXML
    private void showExerciseInfo(ComboBox<String> comboBox) {
        String exerciseName = comboBox.getEditor().getText().trim();
        if (exerciseName.isEmpty()) {
            showAlert("Enter exercise name");
            return;
        }

        Exercise exercise = ExerciseService.findByName(exerciseName);
        if (exercise == null) {
            showAlert("Exercise not found");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Exercise information");
        alert.setHeaderText(exercise.name());

        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 20;");

        Label musclesLabel = new Label("Targeted muscles: " + exercise.muscles());
        Label descLabel = new Label("Description: " + exercise.description());

        descLabel.setWrapText(true);

        content.getChildren().addAll(musclesLabel, descLabel);
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void saveWorkout() {
        try {
            // 1) проверяем сет‑данные ДО сохранения в БД
            if (hasInvalidSets()) {
                showAlert("Fill in both weight and reps for each set.");
                return;
            }

            // 2) читаем дату и вес
            VBox infoBox = (VBox) workoutInfoContainer.getChildren().get(0);

            DatePicker datePicker      = (DatePicker) infoBox.getChildren().get(1);
            TextField bodyweightField  = (TextField) infoBox.getChildren().get(2);

            var localDate = datePicker.getValue();
            if (localDate == null) {
                showAlert("Select workout date.");
                return;
            }

            Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            double bodyweight = Double.parseDouble(bodyweightField.getText());


            // 4) собираем упражнения
            for (var node : exercisesContainer.getChildren()) {
                VBox exerciseBox = (VBox) node;

                HBox exerciseInput = (HBox) exerciseBox.getChildren().get(1);
                ComboBox<String> combo = (ComboBox<String>) exerciseInput.getChildren().get(0);

                String exerciseName = combo.getEditor().getText();

                VBox setsContainer = (VBox) exerciseBox.getChildren().get(3);
                StringBuilder setsString = new StringBuilder();

                for (var setNode : setsContainer.getChildren()) {
                    HBox setBox = (HBox) setNode;

                    TextField weightFieldSet = (TextField) setBox.getChildren().get(1);
                    TextField repsFieldSet   = (TextField) setBox.getChildren().get(3);

                    if (!weightFieldSet.getText().isBlank() && !repsFieldSet.getText().isBlank()) {
                        if (!setsString.isEmpty()) setsString.append("-");
                        setsString
                                .append(weightFieldSet.getText())
                                .append("x")
                                .append(repsFieldSet.getText());
                    }
                }

                WorkoutSession session = new WorkoutSession(date, bodyweight);
                DatabaseHelper.sessionDao.create(session);
                WorkoutExercise ex = new WorkoutExercise(exerciseName, setsString.toString(), session);
                DatabaseHelper.exerciseDao.create(ex);

            }

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
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
            VBox exerciseBox = (VBox) node;

            VBox setsContainer = (VBox) exerciseBox.getChildren().get(3);

            for (var setNode : setsContainer.getChildren()) {
                HBox setBox = (HBox) setNode;

                TextField weightField = (TextField) setBox.getChildren().get(1);
                TextField repsField   = (TextField) setBox.getChildren().get(3);

                boolean weightFilled = !weightField.getText().isBlank();
                boolean repsFilled   = !repsField.getText().isBlank();

                if (!weightFilled || !repsFilled) {
                    return true;
                }
            }
        }
        return false;
    }

    @FXML
    private void deleteDB() {
        // 1. Модальное окно подтверждения
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText("Delete all saved data?");
        confirm.setContentText("This will permanently delete the workouts.db file. Continue?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return; // пользователь отменил
        }

        // 2. Удаляем файл
        File dbFile = new File("workouts.db");
        if (dbFile.exists() && dbFile.delete()) {
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Success");
            info.setHeaderText(null);
            info.setContentText("All data deleted successfully.");
            info.showAndWait();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setTitle("Error");
            error.setHeaderText("Could not delete database file.");
            error.setContentText("Make sure the application has access and the file is not locked.");
            error.showAndWait();
        }
    }

    @FXML private void goToAddWorkout() throws Exception { app.tracker.MainApplication.INSTANCE.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { app.tracker.MainApplication.INSTANCE.showViewStatistics(); }
    @FXML private void goToViewGraphs() throws Exception { app.tracker.MainApplication.INSTANCE.showViewGraphs(); }
}
