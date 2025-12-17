package app.service;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class StatisticsService {
    private final WorkoutService workoutService;

    public StatisticsService(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    public List<Map<String, Object>> getBodyweightByDate() throws SQLException {
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        List<Map<String, Object>> rows = new ArrayList<>();

        for (WorkoutSession s : sessions) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", s.getId());
            row.put("date", s.getDate());
            row.put("bw", s.getBodyweight());
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllExercisesAllSets(String query) throws SQLException {
        List<WorkoutExercise> filtered = filterExercisesByName(workoutService.getAllExercises(), query);
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
                String d = df.format(ex.getSession().getDate());
                row.put(d, ex.getSetsString());
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllExercisesBestSet(String query) throws SQLException {
        List<WorkoutExercise> filtered = filterExercisesByName(workoutService.getAllExercises(), query);
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
                String d = df.format(ex.getSession().getDate());
                row.put(d, pickBestSet(ex.getSetsString()));
            }
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> getAllSessionsSummary(String query) throws SQLException {
        List<WorkoutSession> sessions = workoutService.getAllSessions();
        List<WorkoutExercise> exercises = workoutService.getAllExercises();

        List<Map<String, Object>> rows = new ArrayList<>();
        for (WorkoutSession s : sessions) {
            for (WorkoutExercise ex : exercises) {
                if (ex.getSession() == null || ex.getSession().getId() != s.getId()) continue;
                if (query != null && !query.isBlank() &&
                        !ex.getName().toLowerCase().contains(query.toLowerCase())) continue;

                Map<String, Object> row = new HashMap<>();
                row.put("sid", s.getId());
                row.put("date", s.getDate());
                row.put("name", ex.getName());
                row.put("sets", ex.getSetsString());
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

    private List<WorkoutExercise> filterExercisesByName(List<WorkoutExercise> exercises, String query) {
        if (query == null || query.isBlank()) return exercises;
        String lower = query.toLowerCase();
        return exercises.stream()
                .filter(ex -> ex.getName().toLowerCase().contains(lower))
                .toList();
    }

    public String pickBestSet(String sets) {
        if (sets == null || sets.isBlank()) return "";
        String best = "";
        int bestW = -1, bestR = -1;
        for (String part : sets.split("-")) {
            String[] wr = part.split("x");
            if (wr.length != 2) continue;
            try {
                int w = Integer.parseInt(wr[0]);
                int r = Integer.parseInt(wr[1]);
                if (w > bestW || (w == bestW && r > bestR)) {
                    bestW = w;
                    bestR = r;
                    best = w + "x" + r;
                }
            } catch (NumberFormatException ignore) {}
        }
        return best;
    }
}
