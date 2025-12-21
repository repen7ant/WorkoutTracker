package app.service;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import javafx.util.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphsServiceTest {

    @Mock
    WorkoutService workoutService;

    GraphsService graphsService;

    WorkoutSession s1;
    WorkoutSession s2;
    WorkoutExercise e1_s1;
    WorkoutExercise e2_s1;
    WorkoutExercise e1_s2;

    @BeforeEach
    void setUp() {
        graphsService = new GraphsService(workoutService);

        Calendar cal = Calendar.getInstance();
        cal.set(2024, Calendar.JANUARY, 1, 0, 0, 0);
        s1 = new WorkoutSession(cal.getTime(), 80.0);

        cal.set(2024, Calendar.JANUARY, 2, 0, 0, 0);
        s2 = new WorkoutSession(cal.getTime(), 82.0);

        e1_s1 = new WorkoutExercise("Bench Press", "100x5-110x4", s1);
        e2_s1 = new WorkoutExercise("Squat", "140x5", s1);
        e1_s2 = new WorkoutExercise("Bench Press", "120x3", s2);
    }

    @Test
    void getExerciseData_returnsBestSetPerSession() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e1_s2));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("Bench Press");

        assertEquals(2, data.size());

        Pair<Date, Double> p1 = data.get(0);
        assertEquals(s1.getDate(), p1.getKey());
        assertEquals(110.0, p1.getValue());

        Pair<Date, Double> p2 = data.get(1);
        assertEquals(s2.getDate(), p2.getKey());
        assertEquals(120.0, p2.getValue());
    }

    @Test
    void getExerciseData_filtersByName_ignoreCase() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("bench press");

        assertEquals(1, data.size());
        assertEquals(s1.getDate(), data.get(0).getKey());
    }

    @Test
    void getExerciseData_returnsEmpty_whenNoMatch() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("deadlift");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_skipsInvalidSetStrings() throws SQLException {
        WorkoutExercise broken = new WorkoutExercise("Bench Press", "INVALID", s1);
        when(workoutService.getAllExercises()).thenReturn(List.of(broken));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("bench press");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_skipsEmptyBestSet() throws SQLException {
        WorkoutExercise empty = new WorkoutExercise("Bench Press", "", s1);
        when(workoutService.getAllExercises()).thenReturn(List.of(empty));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("bench press");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_nullExerciseName_returnsAllExercises() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1));

        List<Pair<Date, Double>> data = graphsService.getExerciseData(null);

        assertEquals(1, data.size());
    }

    @Test
    void getExerciseData_blankExerciseName_returnsAllExercises() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("   ");

        assertEquals(1, data.size());
    }

    @Test
    void getExerciseData_bestSetHasWrongFormat_skipsParsing() throws SQLException {
        WorkoutExercise wrongFormat = new WorkoutExercise("Bench Press", "110x4xextra", s1);
        when(workoutService.getAllExercises()).thenReturn(List.of(wrongFormat));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("Bench Press");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_numberFormatException_ignoresAndContinues() throws SQLException {
        WorkoutExercise invalidWeight = new WorkoutExercise("Bench Press", "abcx5", s1);
        WorkoutExercise validExercise = new WorkoutExercise("Squat", "140x5", s1);

        when(workoutService.getAllExercises()).thenReturn(List.of(invalidWeight, validExercise));

        List<Pair<Date, Double>> data = graphsService.getExerciseData(null); // null = все упражнения

        assertEquals(1, data.size());
        assertEquals(140.0, data.get(0).getValue()); // ← Только вес
    }

    @Test
    void getExerciseData_invalidReps_ignoresAndContinues() throws SQLException {
        WorkoutExercise invalidReps = new WorkoutExercise("Bench Press", "110xabc", s1);
        when(workoutService.getAllExercises()).thenReturn(List.of(invalidReps));

        List<Pair<Date, Double>> data = graphsService.getExerciseData("Bench Press");

        assertTrue(data.isEmpty());
    }
}
