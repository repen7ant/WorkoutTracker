package app.database;

import app.model.WorkoutSession;

import java.sql.SQLException;
import java.util.List;

public interface WorkoutSessionRepository {
    void save(WorkoutSession session) throws SQLException;
    List<WorkoutSession> findAll() throws SQLException;
}
