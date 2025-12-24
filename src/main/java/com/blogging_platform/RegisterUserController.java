package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class RegisterUserController implements Initializable {

    @FXML
    private TextField userEmail;

    @FXML
    private TextField userName;

    @FXML
    private ComboBox<String> userRole;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        userRole.setItems(FXCollections.observableArrayList("Admin", "Regular"));
    }

    @FXML
    void submitUser(ActionEvent event) {

    }


    
}
