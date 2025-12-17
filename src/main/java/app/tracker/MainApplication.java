package app.tracker;

import app.database.*;

import app.service.StatisticsService;
import app.service.WorkoutService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

public class MainApplication extends Application {
    public static MainApplication INSTANCE;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        INSTANCE = this;
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
                return new AddWorkoutController(workoutService);
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
                return new ViewStatisticsController(statisticsService);
            }
            throw new IllegalStateException("Unknown controller class: " + c);
        });

        var scene = new Scene(loader.load(), 900, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Statistics");
    }

    public void showViewGraphs() throws IOException {
        var loader = new FXMLLoader(MainApplication.class.getResource("view-graphs-view.fxml"));
        var scene = new Scene(loader.load(), 900, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
