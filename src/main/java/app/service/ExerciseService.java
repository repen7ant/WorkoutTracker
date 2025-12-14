package app.service;

import app.model.Exercise;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExerciseService {
    private static final List<Exercise> exercises = new ArrayList<>();
    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

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
            log.error("error parsing exercises: {}", e.getMessage(), e);
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
