package app.database;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DatabaseHelper {

    private static final Logger log =
            LoggerFactory.getLogger(DatabaseHelper.class);
    private static final String DB_URL = "jdbc:sqlite:workouts.db";

    private static JdbcPooledConnectionSource connectionSource;

    private static WorkoutSessionRepository workoutSessionRepository;
    private static WorkoutExerciseRepository workoutExerciseRepository;

    private DatabaseHelper() {
    }

    public static void init() {
        try {
            connectionSource = new JdbcPooledConnectionSource(DB_URL);

            workoutSessionRepository =
                    new OrmLiteWorkoutSessionRepository(connectionSource);

            workoutExerciseRepository =
                    new OrmLiteWorkoutExerciseRepository(connectionSource);

        } catch (Exception e) {
            log.error("db init error: {}", e.getMessage(), e);
        }
    }

    public static WorkoutSessionRepository workoutSessionRepo() {
        return workoutSessionRepository;
    }

    public static WorkoutExerciseRepository workoutExerciseRepo() {
        return workoutExerciseRepository;
    }

    public static JdbcPooledConnectionSource connectionSource() {
        return connectionSource;
    }

    public static void close() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
                connectionSource = null;
            }
        } catch (Exception e) {
            log.error("db closing error: {}", e.getMessage(), e);
        }
    }
}
