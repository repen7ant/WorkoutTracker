package app.database;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper {

    private static final String DB_URL = "jdbc:sqlite:workouts.db";
    private static JdbcPooledConnectionSource connectionSource;

    public static Dao<WorkoutSession, Integer> sessionDao;
    public static Dao<WorkoutExercise, Integer> exerciseDao;

    public static void init() {
        try {
            connectionSource = new JdbcPooledConnectionSource(DB_URL);

            sessionDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, WorkoutSession.class);
            exerciseDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, WorkoutExercise.class);

            TableUtils.createTableIfNotExists(connectionSource, WorkoutSession.class);
            TableUtils.createTableIfNotExists(connectionSource, WorkoutExercise.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
