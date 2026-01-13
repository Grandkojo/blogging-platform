package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DuplicateEmailException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterUserController extends BaseController implements Initializable {

    @FXML
    private TextField userEmail;

    @FXML
    private TextField userName;

    @FXML
    private ComboBox<String> userRole;

    @FXML
    private PasswordField userPassword;

    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;


    @FXML
    private Label nameError;

    @FXML
    private Label roleError;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        userRole.setItems(FXCollections.observableArrayList("Admin", "Regular"));
        userRole.setPromptText("Select Role");
    }

        private boolean validateForm() {
        boolean valid = true;

        if (userName.getText().trim().isEmpty()) {
            nameError.setVisible(true);
            valid = false;
        } else {
            nameError.setVisible(false);
        }

        if (userEmail.getText().trim().isEmpty()) {
            emailError.setVisible(true);
            valid = false;
        } else if (!userEmail.getText().trim().contains("@") || !userEmail.getText().trim().contains(".")){
            emailError.setText("Valid email is required");
            emailError.setVisible(true);
            valid = false;
        }else {
            emailError.setVisible(false);
        }

        if (userPassword.getText().trim().isEmpty()) {
            passwordError.setVisible(true);
            valid = false;
        } else {
            passwordError.setVisible(false);
        }

        if (userRole.getValue() == null) {
            roleError.setVisible(true);
            valid = false;
        } else {
            roleError.setVisible(false);
        }

        return valid;
    }

    @FXML
    void submitUser(ActionEvent event) {
        if(validateForm()){
            try {
                MySQLDriver sqlDriver = new MySQLDriver();
                sqlDriver.createUser(userName.getText().trim(), userEmail.getText().trim(), userRole.getValue(), userPassword.getText().trim());
                showInfo("User Registered Successfully");
                switchTo("Login", null);
            } catch (DuplicateEmailException e) {
                emailError.setText(e.getUserMessage());
                emailError.setVisible(true);
            } catch (DatabaseException e) {
                showError("Database error. Please try again later.");
            }
        }
    }

    @FXML
    void switchToLogin(ActionEvent event) {
        switchTo("Login", null);
    }


    
}
