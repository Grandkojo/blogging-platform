package com.blogging_platform;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.SessionManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EditPostController extends BaseController implements ParameterReceiver, Initializable {

    @FXML
    private TextArea postContent;

    @FXML
    private Label postContentError;

    @FXML
    private ComboBox<String> postStatus;

    @FXML
    private Label postStatusError;

    @FXML
    private TextField postTitle;

    @FXML
    private Label postTitleError;

    private String currentPostId;

     private void loadPostForEditing(String id) {
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> post = sqlDriver.getPostById(id);

        if (post != null){
            postTitle.setText(post.get(0)); 
            postContent.setText(post.get(1));

            String status = post.get(2);
            switch (status) {
            case "DRAFT":
                postStatus.setValue("Draft");
                break;
            case "PUBLISHED":
                 postStatus.setValue("Publish");
                break;
            default:
                postStatus.setValue("Draft");
                break;
        }   
        }
    }

    @Override
    public void receiveParameter(Object parameter) {
         if (parameter instanceof String) {
            this.currentPostId = (String) parameter;
            loadPostForEditing(currentPostId);
        }
    }

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        postStatus.getItems().setAll("Draft", "Publish");
    }

    private boolean validateForm() {
        boolean valid = true;

        if (postTitle.getText().trim().isEmpty()) {
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
            MySQLDriver sqlDriver = new MySQLDriver();
            boolean created = sqlDriver.updatePost(currentPostId, postTitle.getText().trim(), postContent.getText().trim(), status.toUpperCase());
            if (created){
                showInfo("Post Updated Successfully");
                switchTo("PostList");
            } else {
                showError("Failed to update post, try again");
            }

        }
    }

    

    
}
