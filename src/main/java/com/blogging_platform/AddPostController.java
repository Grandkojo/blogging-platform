package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddPostController implements Initializable {

    
    @FXML
    private TextArea postContent;
    
    @FXML
    private ComboBox<String> postStatus;
    
    @FXML
    private TextField postTile;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        postStatus.setItems(FXCollections.observableArrayList("Draft", "Publish"));    
    }

    @FXML
    void submitPost(ActionEvent event) {

    }


}
