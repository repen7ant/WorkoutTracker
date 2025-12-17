package app.database;

import app.model.WorkoutExercise;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class OrmLiteWorkoutExerciseRepository implements WorkoutExerciseRepository {
    private final Dao<WorkoutExercise, Integer> dao;

    public OrmLiteWorkoutExerciseRepository(JdbcPooledConnectionSource cs) throws SQLException {
        this.dao = DaoManager.createDao(cs, WorkoutExercise.class);
        TableUtils.createTableIfNotExists(cs, WorkoutExercise.class);
    }

    @Override
    public void save(WorkoutExercise exercise) throws SQLException {
        dao.create(exercise);
    }

    @Override
    public List<WorkoutExercise> findAll() throws SQLException {
        return dao.queryForAll();
    }
}
