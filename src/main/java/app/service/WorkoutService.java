package app.service;

import app.database.WorkoutExerciseRepository;
import app.database.WorkoutSessionRepository;
import app.model.ExerciseWithSets;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class WorkoutService {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutExerciseRepository exerciseRepo;

    public WorkoutService(WorkoutSessionRepository sessionRepo,
                          WorkoutExerciseRepository exerciseRepo) {
        this.sessionRepo = sessionRepo;
        this.exerciseRepo = exerciseRepo;
    }

    public void saveWorkout(Date date,
                            double bodyweight,
                            List<ExerciseWithSets> exercises) throws SQLException {

        WorkoutSession session = new WorkoutSession(date, bodyweight);
        sessionRepo.save(session);

        for (ExerciseWithSets ex : exercises) {
            WorkoutExercise we = new WorkoutExercise(
                    ex.name(),
                    ex.setsString(),
                    session
            );
            exerciseRepo.save(we);
        }
    }

    public boolean hasInvalidSets(List<VBox> exerciseBoxes) {
        for (var node : exerciseBoxes) {

            var setsContainer = (VBox) node.getChildren().get(3);

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

    public String buildSetsString(VBox setsContainer) {
        var setsString = new StringBuilder();

        for (var setNode : setsContainer.getChildren()) {
            var setBox = (HBox) setNode;

            var weightFieldSet = (TextField) setBox.getChildren().get(1);
            var repsFieldSet   = (TextField) setBox.getChildren().get(3);

            if (!weightFieldSet.getText().isBlank() && !repsFieldSet.getText().isBlank()) {
                if (!setsString.isEmpty())
                    setsString.append("-");
                setsString
                        .append(weightFieldSet.getText())
                        .append("x")
                        .append(repsFieldSet.getText());
            }
        }
        return setsString.toString();
    }

    public List<WorkoutSession> getAllSessions() throws SQLException {
        return sessionRepo.findAll();
    }

    public List<WorkoutExercise> getAllExercises() throws SQLException {
        return exerciseRepo.findAll();
    }
}
