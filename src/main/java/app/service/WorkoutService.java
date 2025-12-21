package app.service;

import app.database.DatabaseHelper;
import app.database.WorkoutExerciseRepository;
import app.database.WorkoutSessionRepository;
import app.model.Exercise;
import app.model.ExerciseWithSets;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public final class WorkoutService {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutExerciseRepository exerciseRepo;

    public WorkoutService(
            final WorkoutSessionRepository sessionRepo,
            final WorkoutExerciseRepository exerciseRepo) {
        this.sessionRepo = sessionRepo;
        this.exerciseRepo = exerciseRepo;
    }

    public void saveWorkout(
            final Date date,
            final double bodyweight,
            final List<ExerciseWithSets> exercises) throws SQLException {

        var session = new WorkoutSession(date, bodyweight);
        sessionRepo.save(session);

        for (ExerciseWithSets exercise : exercises) {
            WorkoutExercise workoutExercise = new WorkoutExercise(
                    exercise.name(),
                    exercise.setsString(),
                    session);
            exerciseRepo.save(workoutExercise);
        }
    }

    public List<WorkoutSession> getAllSessions() throws SQLException {
        return sessionRepo.findAll();
    }

    public List<WorkoutExercise> getAllExercises() throws SQLException {
        return exerciseRepo.findAll();
    }

    public boolean deleteDatabase() throws Exception {
        DatabaseHelper.close();

        var dbUrl = "jdbc:sqlite:workouts.db";
        var pathPart = dbUrl.replace("jdbc:sqlite:", "");
        var dbPath = Paths.get(pathPart).toAbsolutePath();

        if (Files.exists(dbPath)) {
            Files.delete(dbPath);
            DatabaseHelper.init();
            return true;
        }
        return false;
    }

    public List<Exercise> loadExercises() {
        ExerciseCsvParser.loadExercises();
        return ExerciseCsvParser.getExercises();
    }

    public Exercise findExercise(final String exerciseName) {
        return ExerciseCsvParser.findByName(exerciseName);
    }
}
