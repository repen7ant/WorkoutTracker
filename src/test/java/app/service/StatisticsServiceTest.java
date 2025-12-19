package app.service;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock
    WorkoutService workoutService;

    StatisticsService statisticsService;

    WorkoutSession s1;
    WorkoutSession s2;
    WorkoutExercise e1_s1;
    WorkoutExercise e2_s1;
    WorkoutExercise e1_s2;

    @BeforeEach
    void setUp() {
        statisticsService = new StatisticsService(workoutService);

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
    void getBodyweightByDate_returnsRowsWithDateAndBw() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));

        List<Map<String, Object>> rows = statisticsService.getBodyweightByDate();

        assertEquals(2, rows.size());

        Map<String, Object> r1 = rows.get(0);
        assertEquals(s1.getDate(), r1.get("date"));
        assertEquals(s1.getBodyweight(), r1.get("bw"));

        Map<String, Object> r2 = rows.get(1);
        assertEquals(s2.getDate(), r2.get("date"));
        assertEquals(s2.getBodyweight(), r2.get("bw"));
    }

    @Test
    void getAllExercisesAllSets_groupsByName_andKeepsAllSets() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesAllSets("bench");

        assertEquals(1, rows.size());
        Map<String, Object> row = rows.get(0);
        assertEquals("Bench Press", row.get("name"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String d1 = df.format(s1.getDate());
        String d2 = df.format(s2.getDate());

        assertEquals("100x5-110x4", row.get(d1));
        assertEquals("120x3", row.get(d2));
    }

    @Test
    void getAllExercisesAllSets_returnsEmpty_whenNoMatch() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesAllSets("deadlift");

        assertTrue(rows.isEmpty());
    }

    @Test
    void getAllExercisesBestSet_usesBestSetPerDate() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesBestSet("bench");

        assertEquals(1, rows.size());
        Map<String, Object> row = rows.get(0);

        assertEquals("Bench Press", row.get("name"));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String d1 = df.format(s1.getDate());
        String d2 = df.format(s2.getDate());

        assertEquals("110x4", row.get(d1));
        assertEquals("120x3", row.get(d2));
    }

    @Test
    void getAllExercisesBestSet_emptyWhenNoExercises() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of());

        List<Map<String, Object>> rows = statisticsService.getAllExercisesBestSet(null);

        assertTrue(rows.isEmpty());
    }

    @Test
    void getAllSessionsSummary_returnsRowsPerSessionAndExercise() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary("");

        assertEquals(6, rows.size());

        Map<String, Object> r0 = rows.get(0);
        assertEquals("Bench Press", r0.get("name"));
        assertEquals("100x5-110x4", r0.get("sets"));
    }

    @Test
    void getAllSessionsSummary_filtersByQuery() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary("bench");

        assertEquals(4, rows.size());

        assertTrue(rows.stream()
                .allMatch(r -> ((String) r.get("name")).toLowerCase().contains("bench")));

        assertFalse(rows.stream()
                .anyMatch(r -> ((String) r.get("name")).toLowerCase().contains("squat")));
    }

    @Test
    void getAllExercisesAllSets_nullQuery_doesNotFilter() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesAllSets(null);

        assertEquals(2, rows.size());
    }

    @Test
    void getAllExercisesBestSet_filtersByQuery() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesBestSet("squat");

        assertEquals(1, rows.size());
        assertEquals("Squat", rows.get(0).get("name"));
    }

    @Test
    void getAllSessionsSummary_queryFiltersOutAll() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary("nonexistent");

        assertTrue(rows.isEmpty());
    }

    @Test
    void getAllDates_emptySessions_returnsEmptySet() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of());

        Set<String> dates = statisticsService.getAllDates();

        assertTrue(dates.isEmpty());
    }

    @Test
    void getAllSessionsSummary_skipsExercisesWithNullSession() throws SQLException {
        WorkoutExercise orphan = new WorkoutExercise("Bench Press", "100x5", null);

        when(workoutService.getAllSessions()).thenReturn(List.of(s1));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, orphan));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary(null);

        assertEquals(1, rows.size());
        assertEquals("Bench Press", rows.get(0).get("name"));
    }

    @Test
    void getAllSessionsSummary_skipsExercisesFromOtherSession() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary(null);

        assertEquals(2, rows.size());
        assertEquals("Bench Press", rows.get(0).get("name"));
        assertEquals(s1.getDate(), rows.get(0).get("date"));
    }

    @Test
    void getAllSessionsSummary_filtersOutByQueryWhenNotMatching() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary("deadlift");

        assertTrue(rows.isEmpty());
    }

    @Test
    void getAllSessionsSummary_queryBlank_doesNotFilter() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1, s2));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1, e1_s2));

        List<Map<String, Object>> rows = statisticsService.getAllSessionsSummary("   ");

        assertEquals(6, rows.size());
    }

    @Test
    void getAllDates_formatsDatesAndReturnsSortedUnique() throws SQLException {
        WorkoutSession s1copy = new WorkoutSession(s1.getDate(), 81.0);
        when(workoutService.getAllSessions()).thenReturn(List.of(s2, s1, s1copy));

        Set<String> dates = statisticsService.getAllDates();

        assertEquals(2, dates.size());
        String any = dates.iterator().next();
        assertEquals(10, any.length());
    }
    @Test
    void getAllExercisesAllSets_nullQuery_returnsAllByNameGroups() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesAllSets(null);

        assertEquals(2, rows.size());
    }

    @Test
    void getAllExercisesAllSets_blankQuery_doesNotFilter() throws SQLException {
        when(workoutService.getAllSessions()).thenReturn(List.of(s1));
        when(workoutService.getAllExercises()).thenReturn(List.of(e1_s1, e2_s1));

        List<Map<String, Object>> rows = statisticsService.getAllExercisesAllSets("   ");

        assertEquals(2, rows.size());
    }
}