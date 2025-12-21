package app.service;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;


public final class StatisticsService {

    private final WorkoutService workoutService;

    public StatisticsService(final WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    public List<Map<String, Object>> getBodyweightByDate() throws SQLException {
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        List<Map<String, Object>> rows = new ArrayList<>();

        for (WorkoutSession session : sessions) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", session.getId());
            row.put("date", session.getDate());
            row.put("bw", session.getBodyweight());
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllExercisesAllSets(
            final String query) throws SQLException {
        List<WorkoutExercise> filtered = filterExercisesByName(
                workoutService.getAllExercises(), query);
        List<WorkoutSession> sessions = workoutService.getAllSessions();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> dates = sessions.stream()
                .map(s -> df.format(s.getDate()))
                .collect(Collectors.toCollection(TreeSet::new));

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (var entry : byName.entrySet()) {
            List<WorkoutExercise> exList = entry.getValue();
            Map<String, Object> row = new HashMap<>();
            row.put("id", exList.get(0).getId());
            row.put("name", entry.getKey());

            for (WorkoutExercise ex : exList) {
                String dateStr = df.format(ex.getSession().getDate());
                row.put(dateStr, ex.getSetsString());
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllExercisesBestSet(
            final String query) throws SQLException {
        List<WorkoutExercise> filtered = filterExercisesByName(
                workoutService.getAllExercises(), query);
        List<WorkoutSession> sessions = workoutService.getAllSessions();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Set<String> dates = sessions.stream()
                .map(s -> df.format(s.getDate()))
                .collect(Collectors.toCollection(TreeSet::new));

        Map<String, List<WorkoutExercise>> byName = filtered.stream()
                .collect(Collectors.groupingBy(WorkoutExercise::getName));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (var entry : byName.entrySet()) {
            List<WorkoutExercise> exList = entry.getValue();
            Map<String, Object> row = new HashMap<>();
            row.put("id", exList.get(0).getId());
            row.put("name", entry.getKey());

            for (WorkoutExercise ex : exList) {
                String dateStr = df.format(ex.getSession().getDate());
                row.put(dateStr, SetParser.pickBestSet(ex.getSetsString()));
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllSessionsSummary(
            final String query) throws SQLException {
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        List<WorkoutExercise> exercises = workoutService.getAllExercises();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (WorkoutSession session : sessions) {
            for (WorkoutExercise exercise : exercises) {
                if (exercise.getSession() == null
                        || exercise.getSession().getId() != session.getId()) {
                    continue;
                }
                if (query != null && !query.isBlank()
                        && !exercise.getName()
                        .toLowerCase()
                        .contains(query.toLowerCase())) {
                    continue;
                }

                Map<String, Object> row = new HashMap<>();
                row.put("sid", session.getId());
                row.put("date", session.getDate());
                row.put("name", exercise.getName());
                row.put("sets", exercise.getSetsString());
                rows.add(row);
            }
        }
        return rows;
    }

    public Set<String> getAllDates() throws SQLException {
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return sessions.stream()
                .map(s -> df.format(s.getDate()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private List<WorkoutExercise> filterExercisesByName(
            final List<WorkoutExercise> exercises,
            final String query) {
        if (query == null || query.isBlank()) {
            return exercises;
        }
        String lowerQuery = query.toLowerCase();
        return exercises.stream()
                .filter(ex -> ex.getName()
                        .toLowerCase()
                        .contains(lowerQuery))
                .toList();
    }
}
