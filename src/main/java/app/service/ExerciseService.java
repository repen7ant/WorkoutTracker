package app.service;

import app.database.DatabaseHelper;
import app.model.Exercise;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ExerciseService {
    private static final List<Exercise> exercises = new ArrayList<>();
    private static final Logger log = Logger.getLogger(ExerciseService.class.getName());

    public static void loadExercises() {
        try (InputStream is = ExerciseService.class.getResourceAsStream("/exercises.csv")) {
            try (var reader = new BufferedReader(new InputStreamReader(is))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", 3);
                    if (parts.length == 3) {
                        exercises.add(new Exercise(parts[0].trim(), parts[1].trim(), parts[2].trim()));
                    }
                }
            }
        } catch (Exception e) {
            log.severe("error parsing exercises: " + e.getMessage());
        }
    }

    public static List<Exercise> getExercises() {
        if (exercises.isEmpty()) loadExercises();
        return new ArrayList<>(exercises);
    }

    public static Exercise findByName(String name) {
        return exercises.stream()
                .filter(e -> e.name().toLowerCase().contains(name.toLowerCase()))
                .findFirst().orElse(null);
    }
}
