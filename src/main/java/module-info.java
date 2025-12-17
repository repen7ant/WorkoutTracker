module app.tracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires ormlite.jdbc;
    requires org.slf4j;

    opens app.tracker to javafx.fxml;
    opens app.model to ormlite.jdbc;

    opens app.service to org.mockito;
    opens app.database to org.mockito;

    exports app.tracker;
    exports app.service;
    exports app.model;
    exports app.database;
}