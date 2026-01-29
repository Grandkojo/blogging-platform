package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.model.User;

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
        } else if (!userEmail.getText().trim().contains("@") || !userEmail.getText().trim().contains(".")) {
            emailError.setText("Valid email is required");
            emailError.setVisible(true);
            valid = false;
        } else {
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
        if (validateForm()) {
            try {
                System.out.println(userRole.getValue());
                User user = new User(userName.getText().trim(), userEmail.getText().trim(), userPassword.getText().trim(), userRole.getValue().trim());
                userService.registerUser(user);
                showInfo("User Registered Successfully");
                switchTo("Login", null);

            } catch (DuplicateEmailException e) {
                emailError.setText(e.getUserMessage());
                emailError.setVisible(true);
            }
        }
    }

    @FXML
    void switchToLogin(ActionEvent event) {
        switchTo("Login", null);
    }

}
