package app.service;

import app.database.DatabaseHelper;
import app.database.WorkoutExerciseRepository;
import app.database.WorkoutSessionRepository;
import app.model.Exercise;
import app.model.ExerciseWithSets;
import app.model.WorkoutExercise;
import app.model.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceTest {

    @Mock
    WorkoutSessionRepository sessionRepo;

    @Mock
    WorkoutExerciseRepository exerciseRepo;

    WorkoutService service;

    @BeforeEach
    void setUp() {
        service = new WorkoutService(sessionRepo, exerciseRepo);
    }


    @Test
    void saveWorkout_savesSessionAndExercises() throws SQLException {
        Date date = new Date();
        List<ExerciseWithSets> exercises = List.of(
                new ExerciseWithSets("Bench Press", "100x5-110x4"),
                new ExerciseWithSets("Deadlift", "160x3")
        );

        service.saveWorkout(date, 80.5, exercises);

        verify(sessionRepo).save(argThat(s ->
                s.getDate().equals(date) && s.getBodyweight() == 80.5
        ));

        verify(exerciseRepo, times(2)).save(any(WorkoutExercise.class));
    }

    @Test
    void saveWorkout_emptyExerciseList_onlySessionSaved() throws SQLException {
        Date date = new Date();

        service.saveWorkout(date, 75.0, List.of());

        verify(sessionRepo).save(any(WorkoutSession.class));
        verify(exerciseRepo, never()).save(any());
    }

    @Test
    void saveWorkout_throwsWhenSessionRepoFails() throws SQLException {
        doThrow(new SQLException("DB error")).when(sessionRepo).save(any(WorkoutSession.class));

        assertThrows(SQLException.class, () ->
                service.saveWorkout(new Date(), 80.0,
                        List.of(new ExerciseWithSets("Test", "100x5")))
        );

        verify(exerciseRepo, never()).save(any());
    }

    @Test
    void saveWorkout_throwsWhenExerciseRepoFails() throws SQLException {
        doThrow(new SQLException("Exercise error")).when(sessionRepo).save(any(WorkoutSession.class));

        assertThrows(SQLException.class, () ->
                service.saveWorkout(new Date(), 80.0,
                        List.of(new ExerciseWithSets("Test", "100x5")))
        );

        verify(sessionRepo).save(any(WorkoutSession.class));
    }

    @Test
    void getAllSessions_delegatesToRepository() throws SQLException {
        when(sessionRepo.findAll()).thenReturn(List.of(
                new WorkoutSession(new Date(), 80.0)
        ));

        List<WorkoutSession> result = service.getAllSessions();

        assertEquals(1, result.size());
        verify(sessionRepo).findAll();
    }

    @Test
    void getAllExercises_delegatesToRepository() throws SQLException {
        when(exerciseRepo.findAll()).thenReturn(List.of(
                new WorkoutExercise("Bench Press", "100x5", null)
        ));

        List<WorkoutExercise> result = service.getAllExercises();

        assertEquals(1, result.size());
        assertEquals("Bench Press", result.get(0).getName());
        verify(exerciseRepo).findAll();
    }

    @Test
    void deleteDatabase_deletesFileAndReinitializes_whenFileExists() throws Exception {
        try (MockedStatic<DatabaseHelper> dbHelperMock = mockStatic(DatabaseHelper.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            boolean result = service.deleteDatabase();

            assertTrue(result);
            dbHelperMock.verify(DatabaseHelper::close);
            filesMock.verify(() -> Files.exists(any(Path.class)));
            filesMock.verify(() -> Files.delete(any(Path.class)));
            dbHelperMock.verify(DatabaseHelper::init);
        }
    }

    @Test
    void deleteDatabase_returnsFalse_whenFileDoesNotExist() throws Exception {
        try (MockedStatic<DatabaseHelper> dbHelperMock = mockStatic(DatabaseHelper.class);
             MockedStatic<Files> filesMock = mockStatic(Files.class)) {

            filesMock.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            boolean result = service.deleteDatabase();

            assertFalse(result);
            dbHelperMock.verify(DatabaseHelper::close);
            filesMock.verify(() -> Files.exists(any(Path.class)));
            filesMock.verifyNoMoreInteractions();
            dbHelperMock.verifyNoMoreInteractions();
        }
    }

    @Test
    void loadExercises_callsParserAndReturnsList() {
        try (MockedStatic<ExerciseCsvParser> parserMock = mockStatic(ExerciseCsvParser.class)) {
            var exercises = List.of(
                    new Exercise("Bench Press", "Chest", "Press"),
                    new Exercise("Squat", "Legs", "Squat down")
            );
            parserMock.when(ExerciseCsvParser::getExercises).thenReturn(exercises);

            List<Exercise> result = service.loadExercises();

            parserMock.verify(ExerciseCsvParser::loadExercises);
            parserMock.verify(ExerciseCsvParser::getExercises);
            assertEquals(2, result.size());
        }
    }

    @Test
    void findExercise_delegatesToParser() {
        try (MockedStatic<ExerciseCsvParser> parserMock = mockStatic(ExerciseCsvParser.class)) {
            Exercise ex = new Exercise("Bench Press", "Chest", "Press");
            parserMock.when(() -> ExerciseCsvParser.findByName("Bench"))
                    .thenReturn(ex);

            Exercise result = service.findExercise("Bench");

            assertSame(ex, result);
            parserMock.verify(() -> ExerciseCsvParser.findByName("Bench"));
        }
    }
}
