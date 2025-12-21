package app.service;

import app.model.Exercise;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Answers.CALLS_REAL_METHODS;

class ExerciseCsvParserTest {

    private MockedStatic<ExerciseCsvParser> mockedStatic;

    @BeforeEach
    void setUp() {
        mockedStatic = Mockito.mockStatic(ExerciseCsvParser.class, CALLS_REAL_METHODS);
        ExerciseCsvParser.clear();
    }

    @AfterEach
    void tearDown() {
        mockedStatic.close();
    }

    @Test
    void loadExercises_parsesCsvSuccessfully() throws IOException {
        String csvContent = "name,muscles,description\n" +
                "Bench Press,Chest,Press the bar\n" +
                "Squat,Legs,Squat down\n" +
                "Deadlift,Back,Deadlift";
        setupMockCsv(csvContent);

        ExerciseCsvParser.loadExercises();
        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertEquals(3, exercises.size());
        assertEquals("Bench Press", exercises.get(0).name());
        assertEquals("Chest", exercises.get(0).muscles());
        assertEquals("Press the bar", exercises.get(0).description());
    }

    @Test
    void getExercises_lazyLoadsWhenEmpty() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);

        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertEquals(1, exercises.size());
        assertEquals("Bench Press", exercises.get(0).name());
    }

    @Test
    void findByName_returnsMatchingExercise_ignoreCase() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                Squat,Legs,Test
                """;
        setupMockCsv(csvContent);
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("bEnCh");

        assertNotNull(result);
        assertEquals("Bench Press", result.name());
    }

    @Test
    void findByName_returnsNull_whenNoMatch() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("___NO_SUCH_EXERCISE___");

        assertNull(result);
    }

    @Test
    void loadExercises_handlesMissingResourceGracefully() {
        mockedStatic.when(ExerciseCsvParser::openCsv).thenReturn(null);

        assertDoesNotThrow(ExerciseCsvParser::loadExercises);
        List<Exercise> exercises = ExerciseCsvParser.getExercises();
        assertTrue(exercises.isEmpty());
    }

    @Test
    void findByName_returnsNull_whenNullName() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName(null);
        assertNull(result);
    }

    @Test
    void findByName_returnsNull_whenEmptyName() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);
        ExerciseCsvParser.loadExercises();

        Exercise result = ExerciseCsvParser.findByName("");
        assertNull(result);
    }

    @Test
    void loadExercises_doesNotDuplicateOnMultipleCalls() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);

        ExerciseCsvParser.loadExercises();
        int sizeAfterFirst = ExerciseCsvParser.getExercises().size();

        ExerciseCsvParser.loadExercises();
        int sizeAfterSecond = ExerciseCsvParser.getExercises().size();

        assertEquals(1, sizeAfterFirst);
        assertEquals(1, sizeAfterSecond);
    }

    @Test
    void getExercises_returnsDefensiveCopy() throws IOException {
        String csvContent = """
                name,muscles,description
                Bench Press,Chest,Test
                """;
        setupMockCsv(csvContent);
        ExerciseCsvParser.loadExercises();

        List<Exercise> first = ExerciseCsvParser.getExercises();
        List<Exercise> second = ExerciseCsvParser.getExercises();

        assertNotSame(first, second);
        assertEquals(1, first.size());
        assertEquals(1, second.size());

        first.clear();
        List<Exercise> afterClear = ExerciseCsvParser.getExercises();
        assertEquals(1, afterClear.size()); // defensive copy работает
    }

    @Test
    void loadExercises_ignoresLinesWithLessThanThreeColumns() throws IOException {
        String csvContent = """
                name,muscles,description
                OnlyName
                Name,Muscles
                Bench Press,Chest,Press the bar
                """;
        setupMockCsv(csvContent);

        ExerciseCsvParser.loadExercises();
        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertEquals(1, exercises.size());
        assertEquals("Bench Press", exercises.get(0).name());
    }

    @Test
    void loadExercises_ignoresEmptyFirstColumn() throws IOException {
        String csvContent = """
                name,muscles,description
                ,Muscles,Desc
                Bench Press,Chest,Press
                """;
        setupMockCsv(csvContent);

        ExerciseCsvParser.loadExercises();
        List<Exercise> exercises = ExerciseCsvParser.getExercises();

        assertEquals(1, exercises.size());
        assertEquals("Bench Press", exercises.get(0).name());
    }

    private void setupMockCsv(String csvContent) {
        InputStream mockStream = new ByteArrayInputStream(
                csvContent.getBytes(StandardCharsets.UTF_8));
        mockedStatic.when(ExerciseCsvParser::openCsv).thenReturn(mockStream);
    }

    @Test
    void loadExercises_logsError_whenExceptionThrown() {
        ExerciseCsvParser.clear();

        InputStream brokenStream = new InputStream() {
            @Override
            public int read() {
                throw new RuntimeException("boom");
            }
        };

        mockedStatic.when(ExerciseCsvParser::openCsv).thenReturn(brokenStream);

        assertDoesNotThrow(ExerciseCsvParser::loadExercises);
        assertTrue(ExerciseCsvParser.getExercises().isEmpty());
    }

    @Test
    void findByName_skipsExercisesWithNullName() {
        ExerciseCsvParser.clear();

        Exercise bad = new Exercise(null, "Chest", "Desc");
        Exercise good = new Exercise("Bench Press", "Chest", "Desc");

        try {
            var field = ExerciseCsvParser.class.getDeclaredField("EXERCISES");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<Exercise> list = (List<Exercise>) field.get(null);
            list.add(bad);
            list.add(good);
        } catch (Exception e) {
            fail(e);
        }

        Exercise result = ExerciseCsvParser.findByName("bench");

        assertNotNull(result);
        assertEquals("Bench Press", result.name());
    }
}
