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

public class WorkoutService {

    private final WorkoutSessionRepository sessionRepo;
    private final WorkoutExerciseRepository exerciseRepo;

    public WorkoutService(WorkoutSessionRepository sessionRepo,
                          WorkoutExerciseRepository exerciseRepo) {
        this.sessionRepo = sessionRepo;
        this.exerciseRepo = exerciseRepo;
    }

    public void saveWorkout(Date date,
                            double bodyweight,
                            List<ExerciseWithSets> exercises) throws SQLException {

        WorkoutSession session = new WorkoutSession(date, bodyweight);
        sessionRepo.save(session);

        for (ExerciseWithSets ex : exercises) {
            WorkoutExercise we = new WorkoutExercise(
                    ex.name(),
                    ex.setsString(),
                    session
            );
            exerciseRepo.save(we);
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

        String dbUrl = "jdbc:sqlite:workouts.db";
        String pathPart = dbUrl.replace("jdbc:sqlite:", "");
        Path dbPath = Paths.get(pathPart).toAbsolutePath();

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

    public Exercise findExercise(String exerciseName) {
        return ExerciseCsvParser.findByName(exerciseName);
    }
}
