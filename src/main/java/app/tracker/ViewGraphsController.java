package app.tracker;

import app.service.GraphsService;
import app.service.Navigator;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ViewGraphsController {
    @FXML private ComboBox<String> chartCombo;
    @FXML private Pane chartPane;
    @FXML private Label statusLabel;

    private final Navigator navigator;
    private final GraphsService graphsService;
    private LineChart<String, Number> lineChart;
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    public ViewGraphsController(GraphsService graphsService, Navigator navigator) {
        this.navigator = navigator;
        this.graphsService = graphsService;
    }

    @FXML
    public void initialize() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Weight (kg)");

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setPrefSize(800, 500);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);
        chartPane.getChildren().add(lineChart);

        try {
            chartCombo.getItems().add("Bodyweight");
            chartCombo.getItems().addAll(graphsService.getExerciseNames());
            chartCombo.getSelectionModel().selectFirst();
            chartCombo.getSelectionModel().selectedItemProperty()
                    .addListener((obs, o, n) -> reloadChart());
            reloadChart();
        } catch (SQLException e) {
            statusLabel.setText("Error loading data");
        }
    }

    private void reloadChart() {
        String selected = chartCombo.getValue();
        if (selected == null) return;

        try {
            lineChart.getData().clear();

            if ("Bodyweight".equals(selected)) {
                loadBodyweightChart();
            } else {
                loadExerciseChart(selected);
            }
        } catch (SQLException e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    private void loadBodyweightChart() throws SQLException {
        var data = graphsService.getBodyweightData();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Bodyweight");

        List<Double> values = new ArrayList<>();
        data.stream()
                .sorted(Comparator.comparing(Pair::getKey))
                .forEach(p -> {
                    String dateStr = DATE_FMT.format(p.getKey());
                    series.getData().add(new XYChart.Data<>(dateStr, p.getValue()));
                    values.add(p.getValue());
                });

        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        adjustYAxis(yAxis, values, 0.5);

        lineChart.getData().add(series);
    }

    private void loadExerciseChart(String exerciseName) throws SQLException {
        var data = graphsService.getExerciseData(exerciseName);

        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Weight (kg)");

        List<Double> weightValues = new ArrayList<>();

        data.stream()
                .sorted(Comparator.comparing(Pair::getKey))
                .forEach(p -> {
                    String dateStr = DATE_FMT.format(p.getKey());
                    double weight = p.getValue().getKey();
                    weightValues.add(weight);
                    weightSeries.getData().add(new XYChart.Data<>(dateStr, weight));
                });

        NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
        adjustYAxis(yAxis, weightValues, 2.5);
        yAxis.setLabel("Weight (kg)");

        lineChart.getData().add(weightSeries);
    }


    private void adjustYAxis(NumberAxis axis, List<? extends Number> values, double padding) {
        if (values.isEmpty()) return;

        double min = values.stream().mapToDouble(Number::doubleValue).min().orElse(0);
        double max = values.stream().mapToDouble(Number::doubleValue).max().orElse(0);

        if (min == max) {
            min -= 1;
            max += 1;
        }

        axis.setAutoRanging(false);
        axis.setLowerBound(min - padding);
        axis.setUpperBound(max + padding);

        double range = (max - min) + padding * 2;
        axis.setTickUnit(calculateNiceTick(range));
    }

    private double calculateNiceTick(double range) {
        if (range <= 1) return 0.1;
        if (range <= 2) return 0.2;
        if (range <= 5) return 0.5;
        if (range <= 10) return 1;
        if (range <= 20) return 2;
        return 5;
    }

    @FXML private void goToAddWorkout() throws Exception { navigator.showAddWorkout(); }
    @FXML private void goToViewStatistics() throws Exception { navigator.showViewStatistics(); }
}

