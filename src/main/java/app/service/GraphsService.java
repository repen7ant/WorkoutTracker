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
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        List<Pair<Date, Double>> data = new ArrayList<>();
        for (WorkoutSession s : sessions) {
            data.add(new Pair<>(s.getDate(), s.getBodyweight()));
        }
        return data;
    }

    public List<Pair<Date, Pair<Double, Integer>>> getExerciseData(String exerciseName) throws SQLException {
        List<WorkoutExercise> exercises = workoutService.getAllExercises();
        List<Pair<Date, Pair<Double, Integer>>> data = new ArrayList<>();

        for (WorkoutExercise ex : exercises) {
            if (exerciseName.equalsIgnoreCase(ex.getName())) {
                String bestSet = SetParser.pickBestSet(ex.getSetsString());
                if (!bestSet.isEmpty()) {
                    String[] wr = bestSet.split("x");
                    if (wr.length == 2) {
                        try {
                            double weight = Double.parseDouble(wr[0]);
                            int reps = Integer.parseInt(wr[1]);
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
