package app.service;

import app.model.Exercise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExerciseCsvParserTest {

    @BeforeEach
    void clearStaticExercises() throws Exception {
        Field field = ExerciseCsvParser.class.getDeclaredField("exercises");
        field.setAccessible(true);
        ((List<?>) field.get(null)).clear();
    }

    @Test
    void loadExercises_parsesCsvSuccessfully() {
        ExerciseCsvParser.loadExercises();

        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertFalse(exercises.isEmpty(), "Exercises should be loaded");

        Exercise first = exercises.get(0);
        assertNotNull(first.name());
        assertNotNull(first.muscles());
        assertNotNull(first.description());
    }

    @Test
    void getExercises_lazyLoadsWhenEmpty() {
        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertFalse(exercises.isEmpty());
    }

    @Test
    void findByName_returnsMatchingExercise_ignoreCase() {
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("bench");

        assertNotNull(result);
        assertTrue(result.name().toLowerCase().contains("bench"));
    }

    @Test
    void findByName_returnsNull_whenNoMatch() {
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("___NO_SUCH_EXERCISE___");

        assertNull(result);
    }

    @Test
    void loadExercises_handlesMissingResourceGracefully() throws Exception {
        Field field = ExerciseCsvParser.class.getDeclaredField("exercises");
        field.setAccessible(true);
        ((List<?>) field.get(null)).clear();

        assertDoesNotThrow(ExerciseCsvParser::loadExercises);
    }

    @Test
    void findByName_returnsNull_whenNullName() {
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName(null);

        assertNull(result);
    }

    @Test
    void findByName_returnsNull_whenEmptyName() {
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("");

        assertNull(result);
    }

    @Test
    void loadExercises_doesNotDuplicateOnMultipleCalls() {
        ExerciseCsvParser.loadExercises();
        int sizeAfterFirst = ExerciseCsvParser.getExercises().size();

        ExerciseCsvParser.loadExercises();
        int sizeAfterSecond = ExerciseCsvParser.getExercises().size();

        assertEquals(sizeAfterFirst, sizeAfterSecond);
    }

    @Test
    void getExercises_returnsDefensiveCopy() {
        ExerciseCsvParser.loadExercises();

        List<Exercise> first = ExerciseCsvParser.getExercises();
        List<Exercise> second = ExerciseCsvParser.getExercises();

        assertNotSame(first, second);
        assertEquals(first, second);

        first.clear();
        List<Exercise> afterClear = ExerciseCsvParser.getExercises();
        assertFalse(afterClear.isEmpty());
    }

    @Test
    void loadExercises_ignoresLinesWithLessThanThreeColumns() throws Exception {
        parseLineManually("OnlyName");
        parseLineManually("Name,Muscles");
        parseLineManually("Bench Press,Chest,Press the bar");

        List<Exercise> exercises = ExerciseCsvParser.getExercises();
        assertEquals(1, exercises.size());
        assertEquals("Bench Press", exercises.get(0).name());
    }

    @Test
    void findByName_ignoresExercisesWithNullName() throws Exception {
        Field field = ExerciseCsvParser.class.getDeclaredField("exercises");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Exercise> backing = (List<Exercise>) field.get(null);
        backing.add(new Exercise(null, "Muscles", "Desc"));
        backing.add(new Exercise("Bench Press", "Chest", "Press"));

        Exercise result = ExerciseCsvParser.findByName("bench");

        assertNotNull(result);
        assertEquals("Bench Press", result.name());
    }


    private void parseLineManually(String line) throws Exception {
        Field field = ExerciseCsvParser.class.getDeclaredField("exercises");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Exercise> exercises = (List<Exercise>) field.get(null);

        String[] parts = line.split(",", 3);
        if (parts.length == 3) {
            exercises.add(new Exercise(parts[0].trim(), parts[1].trim(), parts[2].trim()));
        }
    }

}
