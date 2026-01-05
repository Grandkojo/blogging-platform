module com.blogging_platform {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;
    requires javafx.graphics;
    

    opens com.blogging_platform to javafx.fxml;
    exports com.blogging_platform;
}
