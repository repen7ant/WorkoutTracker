package app.database;

import app.model.WorkoutExercise;
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
class OrmLiteWorkoutExerciseRepositoryTest {

    @Mock
    JdbcPooledConnectionSource connectionSource;

    @Mock
    Dao<WorkoutExercise, Integer> dao;

    @Test
    void save_shouldCallDaoCreate() throws Exception {
        try (
                MockedStatic<DaoManager> daoManagerMock = mockStatic(DaoManager.class);
                MockedStatic<TableUtils> tableUtilsMock = mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(connectionSource, WorkoutExercise.class))
                    .thenReturn(dao);

            OrmLiteWorkoutExerciseRepository repo =
                    new OrmLiteWorkoutExerciseRepository(connectionSource);

            WorkoutExercise exercise = new WorkoutExercise();

            repo.save(exercise);

            verify(dao).create(exercise);
        }
    }

    @Test
    void findAll_shouldReturnDaoResult() throws Exception {
        List<WorkoutExercise> expected = List.of(new WorkoutExercise());

        when(dao.queryForAll()).thenReturn(expected);

        try (
                MockedStatic<DaoManager> daoManagerMock = mockStatic(DaoManager.class);
                MockedStatic<TableUtils> tableUtilsMock = mockStatic(TableUtils.class)
        ) {
            daoManagerMock
                    .when(() -> DaoManager.createDao(connectionSource, WorkoutExercise.class))
                    .thenReturn(dao);

            OrmLiteWorkoutExerciseRepository repo =
                    new OrmLiteWorkoutExerciseRepository(connectionSource);

            List<WorkoutExercise> result = repo.findAll();

            assertEquals(expected, result);
            verify(dao).queryForAll();
        }
    }
}
