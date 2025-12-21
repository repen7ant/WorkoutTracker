package app.service;

import app.model.Exercise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ExerciseCsvParser {

    private static final List<Exercise> EXERCISES = new ArrayList<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(ExerciseCsvParser.class);
    private static final int CSV_PARTS_COUNT = 3;

    private ExerciseCsvParser() {
    }

    public static void loadExercises() {
        if (!EXERCISES.isEmpty()) {
            return;
        }
        try (InputStream is = openCsv()) {
            if (is == null) {
                LOGGER.warn("exercises.csv not found");
                return;
            }
            try (var reader = new BufferedReader(
                    new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", CSV_PARTS_COUNT);
                    if (parts.length == CSV_PARTS_COUNT) {
                        EXERCISES.add(new Exercise(
                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim()
                        ));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("error parsing exercises: {}", e.getMessage(), e);
        }
    }

    public static List<Exercise> getExercises() {
        if (EXERCISES.isEmpty()) {
            loadExercises();
        }
        return new ArrayList<>(EXERCISES);
    }

    public static Exercise findByName(final String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return EXERCISES.stream()
                .filter(e -> e.name() != null)
                .filter(e -> e.name()
                        .toLowerCase()
                        .contains(name.toLowerCase()))
                .findFirst()
                .orElse(null);
    }

    static InputStream openCsv() {
        return ExerciseCsvParser.class
                .getResourceAsStream("/exercises.csv");
    }
}
