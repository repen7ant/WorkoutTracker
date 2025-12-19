package app.tracker;

import app.database.DatabaseHelper;
import app.database.OrmLiteWorkoutExerciseRepository;
import app.database.OrmLiteWorkoutSessionRepository;
import app.service.GraphsService;
import app.service.Navigator;
import app.service.StatisticsService;
import app.service.WorkoutService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class MainApplication extends Application implements Navigator {
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        primaryStage = stage;
        DatabaseHelper.init();

        showAddWorkout();

        stage.getIcons().add(new Image("/icon.jpg"));
        stage.setMinWidth(900);
        stage.setMinHeight(900);
        stage.setTitle("Workout Tracker");
        stage.show();
    }

    public void showAddWorkout() throws IOException, SQLException {
        var cs = DatabaseHelper.connectionSource();
        var sessionRepo = new OrmLiteWorkoutSessionRepository(cs);
        var exerciseRepo = new OrmLiteWorkoutExerciseRepository(cs);
        var workoutService = new WorkoutService(sessionRepo, exerciseRepo);

        var loader = new FXMLLoader(MainApplication.class.getResource("add-workout-view.fxml"));
        loader.setControllerFactory(c -> {
            if (c == AddWorkoutController.class) {
                return new AddWorkoutController(workoutService, this);
            }
            throw new IllegalStateException("Unknown controller class: " + c);
        });

        var scene = new Scene(loader.load(), 900, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add workout");
    }


    public void showViewStatistics() throws IOException, SQLException {
        var cs = DatabaseHelper.connectionSource();
        var sessionRepo = new OrmLiteWorkoutSessionRepository(cs);
        var exerciseRepo = new OrmLiteWorkoutExerciseRepository(cs);
        var workoutService = new WorkoutService(sessionRepo, exerciseRepo);
        var statisticsService = new StatisticsService(workoutService);

        var loader = new FXMLLoader(MainApplication.class.getResource("view-statistics-view.fxml"));
        loader.setControllerFactory(c -> {
            if (c == ViewStatisticsController.class) {
                return new ViewStatisticsController(statisticsService, this);
            }
            throw new IllegalStateException("Unknown controller class: " + c);
        });

        var scene = new Scene(loader.load(), 900, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Statistics");
    }

    public void showViewGraphs() throws IOException, SQLException {
        var cs = DatabaseHelper.connectionSource();
        var sessionRepo = new OrmLiteWorkoutSessionRepository(cs);
        var exerciseRepo = new OrmLiteWorkoutExerciseRepository(cs);
        var workoutService = new WorkoutService(sessionRepo, exerciseRepo);
        var graphsService = new GraphsService(workoutService);

        var loader = new FXMLLoader(MainApplication.class.getResource("view-graphs-view.fxml"));

        loader.setControllerFactory(c -> {
            if (c == ViewGraphsController.class) {
                return new ViewGraphsController(graphsService, this);
            }
            throw new IllegalStateException("Unknown controller class: " + c);
        });

        var scene = new Scene(loader.load(), 900, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
