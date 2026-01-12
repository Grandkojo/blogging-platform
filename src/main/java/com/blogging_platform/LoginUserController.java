package com.blogging_platform;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.User;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.UserNotFoundException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginUserController extends BaseController {

    @FXML
    private Label emailError;

    @FXML
    private Label passwordError;

    @FXML
    private TextField userEmail;

    @FXML
    private PasswordField userPassword;

    @FXML
    private Hyperlink registerLink;

    

    private boolean validateForm() {
        boolean valid = true;

        if (userPassword.getText().trim().isEmpty()) {
            passwordError.setVisible(true);
            valid = false;
        } else {
            passwordError.setVisible(false);
        }

        if (userEmail.getText().trim().isEmpty()) {
            emailError.setVisible(true);
            valid = false;
        } else if (!userEmail.getText().trim().contains("@") || !userEmail.getText().trim().contains(".")){
            emailError.setText("Valid email is required");
            emailError.setVisible(true);
            valid = false;
        } else {
            emailError.setVisible(false);
        }

        return valid;
    }

    @FXML
    void loginUser(ActionEvent event) {
        if(validateForm()){
            try {
                MySQLDriver sqlDriver = new MySQLDriver();
                boolean userExists = sqlDriver.validateLogin(userEmail.getText().trim(), userPassword.getText().trim());
                if (userExists){
                    User user = sqlDriver.getCurrentUser(userEmail.getText().trim());
                    SessionManager.getInstance().login(user);
                    switchTo("PostHome");
                } else {
                    throw new AuthenticationException("Invalid email or password");
                }
            } catch (AuthenticationException e) {
                showError(e.getUserMessage());
            } catch (UserNotFoundException e) {
                showError(e.getUserMessage());
            } catch (DatabaseException e) {
                showError("Database error. Please try again later.");
            }
        }
    }

    @FXML
    void switchToRegister(ActionEvent event) {
        switchTo("RegisterUser");
    }

}
