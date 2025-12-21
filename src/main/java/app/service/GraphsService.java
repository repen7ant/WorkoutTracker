package app.service;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import javafx.util.Pair;

import java.sql.SQLException;
import java.util.*;

public class GraphsService {
    private final WorkoutService workoutService;

    public GraphsService(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    public List<Pair<Date, Double>> getBodyweightData() throws SQLException {
        var sessions = workoutService.getAllSessions();
        List<Pair<Date, Double>> data = new ArrayList<>();
        for (WorkoutSession s : sessions) {
            data.add(new Pair<>(s.getDate(), s.getBodyweight()));
        }
        return data;
    }

    public List<Pair<Date, Pair<Double, Integer>>> getExerciseData(String exerciseName) throws SQLException {
        var exercises = workoutService.getAllExercises();
        List<Pair<Date, Pair<Double, Integer>>> data = new ArrayList<>();

        for (WorkoutExercise ex : exercises) {
            if (exerciseName == null
                    || exerciseName.isBlank()
                    || ex.getName().toLowerCase().contains(exerciseName.toLowerCase())) {
                var bestSet = SetParser.pickBestSet(ex.getSetsString());
                if (!bestSet.isEmpty()) {
                    var wr = bestSet.split("x");
                    if (wr.length == 2) {
                        try {
                            var weight = Double.parseDouble(wr[0]);
                            var reps = Integer.parseInt(wr[1]);
                            data.add(new Pair<>(ex.getSession().getDate(), new Pair<>(weight, reps)));
                        } catch (NumberFormatException ignore) {}
                    }
                }
            }
        }
        return data;
    }

    public List<String> getExerciseNames() throws SQLException {
        return workoutService.getAllExercises().stream()
                .map(WorkoutExercise::getName)
                .distinct()
                .sorted()
                .toList();
    }
}
