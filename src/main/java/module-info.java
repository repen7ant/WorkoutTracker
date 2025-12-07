module app.tracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires ormlite.jdbc;

    opens app.tracker to javafx.fxml;
    opens app.model to ormlite.jdbc;
    exports app.tracker;
    exports app.model to ormlite.jdbc;
}