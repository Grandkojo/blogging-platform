package com.blogging_platform;

import java.net.URL;
import java.util.ResourceBundle;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Post;

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

     private void loadPostForEditing(String postId, String userId) {
        try {
            PostRecord post = postService.getPost(postId, userId);
    
            postTitle.setText(post.title()); 
            postContent.setText(post.content());

            String status = post.status();
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
        } catch (PostNotFoundException e) {
            showError(e.getUserMessage());
            switchTo("PostList");
        } catch (DatabaseException e) {
            showError("Failed to load post. Please try again.");
        }
    }

    @Override
    public void receiveParameter(Object parameter) {
         if (parameter instanceof String) {
            this.currentPostId = (String) parameter;
            loadPostForEditing(currentPostId, SessionManager.getInstance().getUserId());
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
            try {
                Post post = new Post(currentPostId, SessionManager.getInstance().getUserId(), postTitle.getText().trim(), postContent.getText().trim(), status.toUpperCase());
                postService.updatePost(post);
                showInfo("Post Updated Successfully");
                switchTo("PostList");
            } catch (PostNotFoundException e) {
                showError(e.getUserMessage());
                switchTo("PostList");
            } catch (DatabaseException e) {
                showError("Failed to update post. Please try again.");
            }
        }
    }

    

    
}
