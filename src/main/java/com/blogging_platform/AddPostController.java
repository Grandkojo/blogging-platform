package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.exceptions.DatabaseException;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class AddPostController extends BaseController implements Initializable {

    
    @FXML
    private TextArea postContent;
    
    @FXML
    private ComboBox<String> postStatus;
    
    @FXML
    private TextField postTile;

    @FXML
    private Label postContentError;

    @FXML
    private Label postStatusError;

    @FXML
    private Label postTitleError;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        postStatus.setItems(FXCollections.observableArrayList("Draft", "Publish"));    
    }

    private boolean validateForm() {
        boolean valid = true;

        if (postTile.getText().trim().isEmpty()) {
            postTitleError.setVisible(true);
            valid = false;
        } else {
            postTitleError.setVisible(false);
        }

        if (postContent.getText().trim().isEmpty()) {
            postContentError.setVisible(true);
            valid = false;
        } else {
            postContentError.setVisible(false);
        }

        if (postStatus.getValue() == null) {
            postStatusError.setVisible(true);
            valid = false;
        } else {
            postStatusError.setVisible(false);
        }

        return valid;
    }


    @FXML
    void submitPost(ActionEvent event) {
        String status = postStatus.getValue().trim();
        switch (status) {
            case "Draft":
                status = "DRAFT";
                break;
            case "Publish":
                status = "PUBLISHED";
                break;
            default:
                status = "DRAFT";
                break;
        }   
        if (validateForm()){
            try {
                MySQLDriver sqlDriver = new MySQLDriver();
                sqlDriver.createPost(SessionManager.getInstance().getUserId(), postTile.getText().trim(), postContent.getText().trim(), status.toUpperCase());
                showInfo("Post Created Successfully");
                switchTo("PostList");
            } catch (DatabaseException e) {
                showError("Failed to create post. Please try again.");
            }
        }
    }


}
