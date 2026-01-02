package com.blogging_platform;


import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;

/**
 * Base controller that all other controllers should extend.
 * Provides easy access to the main App instance for scene switching.
 */
public abstract class BaseController {

    // Reference to the main application
    private App app;

    /**
     * Called by the App class after loading the FXML.
     * Do not call this manually.
     */
    public void setApp(App app) {
        this.app = app;
    }

    /**
     * Helper to switch to another FXML scene.
     * Usage: switchTo("Login") → loads Login.fxml
     */
    protected void switchTo(String fxmlName) {
        if (app == null) {
            System.err.println("App reference is null! Cannot switch scene.");
            return;
        }
        try {
            app.setRoot(fxmlName);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not load " + fxmlName + ".fxml");
        }
    }

    /**
     * Optional: simple error alert
     */
    protected void showError(String message) {
        Alert alert = new Alert(
            Alert.AlertType.ERROR
        );
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Optional: simple info alert
     */
    protected void showInfo(String message) {
        javafx.scene.control.Alert alert = new Alert(
            Alert.AlertType.INFORMATION
        );
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // You can add more common methods here (e.g., logout, getCurrentUser, etc.)
}
