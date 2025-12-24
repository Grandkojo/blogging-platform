module com.blogging_platform {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.blogging_platform to javafx.fxml;
    exports com.blogging_platform;
}
