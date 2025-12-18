package app.database;

import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseHelperTest {

    @Test
    void init_shouldCreateRepositories() {
        Dao<WorkoutSession, Integer> sessionDao = mock(Dao.class);
        Dao<WorkoutExercise, Integer> exerciseDao = mock(Dao.class);

        try (
                MockedConstruction<JdbcPooledConnectionSource> csMock =
                        mockConstruction(JdbcPooledConnectionSource.class);

                MockedStatic<DaoManager> daoManagerMock =
                        mockStatic(DaoManager.class);

                MockedStatic<TableUtils> tableUtilsMock =
                        mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(any(), eq(WorkoutSession.class)))
                    .thenReturn(sessionDao);

            daoManagerMock
                    .when(() -> DaoManager.createDao(any(), eq(WorkoutExercise.class)))
                    .thenReturn(exerciseDao);

            DatabaseHelper.init();

            assertNotNull(DatabaseHelper.connectionSource());
            assertNotNull(DatabaseHelper.workoutSessionRepo());
            assertNotNull(DatabaseHelper.workoutExerciseRepo());
        }
    }

    @Test
    void close_shouldCloseConnection() throws Exception {
        try (
                MockedConstruction<JdbcPooledConnectionSource> csMock =
                        mockConstruction(JdbcPooledConnectionSource.class)
        ) {
            DatabaseHelper.init();
            JdbcPooledConnectionSource cs = DatabaseHelper.connectionSource();

            DatabaseHelper.close();

            verify(cs).close();
            assertNull(DatabaseHelper.connectionSource());
        }
    }

    @Test
    void close_shouldDoNothing_whenConnectionSourceIsNull() {
        DatabaseHelper.close();

        assertNull(DatabaseHelper.connectionSource());
    }

    @Test
    void close_shouldCatchException_whenCloseFails() throws Exception {
        Dao<WorkoutSession, Integer> sessionDao = mock(Dao.class);
        Dao<WorkoutExercise, Integer> exerciseDao = mock(Dao.class);

        try (
                MockedConstruction<JdbcPooledConnectionSource> csMock =
                        mockConstruction(JdbcPooledConnectionSource.class,
                                (mock, context) ->
                                        doThrow(new RuntimeException("boom"))
                                                .when(mock).close());

                MockedStatic<DaoManager> daoManagerMock =
                        mockStatic(DaoManager.class);

                MockedStatic<TableUtils> tableUtilsMock =
                        mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(any(), eq(WorkoutSession.class)))
                    .thenReturn(sessionDao);

            daoManagerMock
                    .when(() -> DaoManager.createDao(any(), eq(WorkoutExercise.class)))
                    .thenReturn(exerciseDao);

            DatabaseHelper.init();
            JdbcPooledConnectionSource cs = DatabaseHelper.connectionSource();

            assertDoesNotThrow(DatabaseHelper::close);

            verify(cs).close();

            assertNotNull(DatabaseHelper.connectionSource());
        }
    }
}
