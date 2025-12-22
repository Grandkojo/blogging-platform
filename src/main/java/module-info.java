module com.blogging_platform {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.blogging_platform to javafx.fxml;
    exports com.blogging_platform;
}
