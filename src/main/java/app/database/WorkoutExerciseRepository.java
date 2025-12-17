package app.database;

import app.model.WorkoutExercise;

import java.sql.SQLException;
import java.util.List;

public interface WorkoutExerciseRepository {
    void save(WorkoutExercise exercise) throws SQLException;
    List<WorkoutExercise> findAll() throws SQLException;
}