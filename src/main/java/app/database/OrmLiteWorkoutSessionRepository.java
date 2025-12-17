package app.database;

import app.model.WorkoutSession;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class OrmLiteWorkoutSessionRepository implements WorkoutSessionRepository {
    private final Dao<WorkoutSession, Integer> dao;

    public OrmLiteWorkoutSessionRepository(JdbcPooledConnectionSource cs) throws SQLException {
        this.dao = DaoManager.createDao(cs, WorkoutSession.class);
        TableUtils.createTableIfNotExists(cs, WorkoutSession.class);
    }

    @Override
    public void save(WorkoutSession session) throws SQLException {
        dao.create(session);
    }

    @Override
    public List<WorkoutSession> findAll() throws SQLException {
        return dao.queryForAll();
    }
}
