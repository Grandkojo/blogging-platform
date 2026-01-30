package com.blogging_platform;
import com.blogging_platform.exceptions.AuthenticationException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * FXML controller for Login (Login.fxml). Validates email and password, authenticates via
 * {@link com.blogging_platform.service.UserService}, and navigates to PostHome on success.
 */
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
                if (userService.loginUser(userEmail.getText().trim(), userPassword.getText().trim())){
                    switchTo("PostHome");
                }
            } catch (AuthenticationException e) {
                showError(e.getUserMessage());
            }
        }
    }

    @FXML
    void switchToRegister(ActionEvent event) {
        switchTo("RegisterUser");
    }

}
