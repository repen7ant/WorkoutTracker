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
    void getBodyweightData_returnsDateAndBodyweight() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));

        List<Pair<Date, Double>> data = graphsService.getBodyweightData();

        assertEquals(2, data.size());

        assertEquals(s1.getDate(), data.get(0).getKey());
        assertEquals(s1.getBodyweight(), data.get(0).getValue());

        assertEquals(s2.getDate(), data.get(1).getKey());
        assertEquals(s2.getBodyweight(), data.get(1).getValue());
    }

    @Test
    void getBodyweightData_emptySessions_returnsEmptyList() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of());

        List<Pair<Date, Double>> data = graphsService.getBodyweightData();

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_returnsBestSetPerSession() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e1_s2));

        List<Pair<Date, Pair<Double, Integer>>> data =
                graphsService.getExerciseData("Bench Press");

        assertEquals(2, data.size());

        Pair<Date, Pair<Double, Integer>> p1 = data.get(0);
        assertEquals(s1.getDate(), p1.getKey());
        assertEquals(110.0, p1.getValue().getKey());
        assertEquals(4, p1.getValue().getValue());

        Pair<Date, Pair<Double, Integer>> p2 = data.get(1);
        assertEquals(s2.getDate(), p2.getKey());
        assertEquals(120.0, p2.getValue().getKey());
        assertEquals(3, p2.getValue().getValue());
    }

    @Test
    void getExerciseData_filtersByName_ignoreCase() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Pair<Date, Pair<Double, Integer>>> data =
                graphsService.getExerciseData("bench");

        assertEquals(1, data.size());
        assertEquals(s1.getDate(), data.get(0).getKey());
    }

    @Test
    void getExerciseData_returnsEmpty_whenNoMatch() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Pair<Date, Pair<Double, Integer>>> data =
                graphsService.getExerciseData("deadlift");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_skipsInvalidSetStrings() throws SQLException {
        WorkoutExercise broken =
                new WorkoutExercise("Bench Press", "INVALID", s1);

        when(workoutService.getAllExercises()).thenReturn(List.of(broken));

        List<Pair<Date, Pair<Double, Integer>>> data =
                graphsService.getExerciseData("bench");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseData_skipsEmptyBestSet() throws SQLException {
        WorkoutExercise empty =
                new WorkoutExercise("Bench Press", "", s1);

        when(workoutService.getAllExercises()).thenReturn(List.of(empty));

        List<Pair<Date, Pair<Double, Integer>>> data =
                graphsService.getExerciseData("bench");

        assertTrue(data.isEmpty());
    }

    @Test
    void getExerciseNames_returnsSortedDistinctNames() throws SQLException {
        when(workoutService.getAllExercises())
                .thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<String> names = graphsService.getExerciseNames();

        assertEquals(List.of("Bench Press", "Squat"), names);
    }

    @Test
    void getExerciseNames_emptyExercises_returnsEmptyList() throws SQLException {
        when(workoutService.getAllExercises()).thenReturn(List.of());

        List<String> names = graphsService.getExerciseNames();

        assertTrue(names.isEmpty());
    }
}
