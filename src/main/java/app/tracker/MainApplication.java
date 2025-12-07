package app.tracker;

import app.database.DatabaseHelper;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;
import java.util.Objects;

public class MainApplication extends Application {
    public static MainApplication INSTANCE;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        INSTANCE = this;
        primaryStage = stage;
        DatabaseHelper.init();
        showAddWorkout();

        stage.getIcons().add(new Image("/icon.jpg"));
        stage.setMinWidth(800);
        stage.setMinHeight(900);
        stage.setTitle("Workout Tracker");
        stage.show();
    }

    public void showAddWorkout() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("add-workout-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Add workout");
    }

    public void showViewStatistics() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("view-statistics-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Statistics");
    }

    public void showViewGraphs() throws IOException {
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("view-graphs-view.fxml"));
        Scene scene = new Scene(loader.load(), 800, 900);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Progress");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
