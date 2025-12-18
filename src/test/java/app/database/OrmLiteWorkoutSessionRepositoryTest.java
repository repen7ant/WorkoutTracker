package app.database;

import app.model.WorkoutSession;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrmLiteWorkoutSessionRepositoryTest {

    @Mock
    JdbcPooledConnectionSource connectionSource;

    @Mock
    Dao<WorkoutSession, Integer> dao;

    @Test
    void save_shouldCallDaoCreate() throws Exception {
        try (
                MockedStatic<DaoManager> daoManagerMock = mockStatic(DaoManager.class);
                MockedStatic<TableUtils> tableUtilsMock = mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(connectionSource, WorkoutSession.class))
                    .thenReturn(dao);

            OrmLiteWorkoutSessionRepository repo =
                    new OrmLiteWorkoutSessionRepository(connectionSource);

            WorkoutSession session = new WorkoutSession();

            repo.save(session);

            verify(dao).create(session);
        }
    }

    @Test
    void findAll_shouldReturnDaoResult() throws Exception {
        List<WorkoutSession> expected = List.of(new WorkoutSession());

        when(dao.queryForAll()).thenReturn(expected);

        try (
                MockedStatic<DaoManager> daoManagerMock = mockStatic(DaoManager.class);
                MockedStatic<TableUtils> tableUtilsMock = mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(connectionSource, WorkoutSession.class))
                    .thenReturn(dao);

            OrmLiteWorkoutSessionRepository repo =
                    new OrmLiteWorkoutSessionRepository(connectionSource);

            List<WorkoutSession> result = repo.findAll();

            assertEquals(expected, result);
        }
    }
}
