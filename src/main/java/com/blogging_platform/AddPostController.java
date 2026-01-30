package com.blogging_platform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.model.Post;
import com.blogging_platform.service.TagService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML controller for Add Post (AddPost.fxml). Handles creating a new post with title,
 * content, status (Draft/Publish), and optional tag; links the selected tag to the post after creation.
 */
public class AddPostController extends BaseController implements Initializable {
    
    @FXML
    private TextArea postContent;
    
    @FXML
    private ComboBox<String> postStatus;
    
    @FXML
    private ComboBox<String> postTag;
    
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
        // Don't load tags here - wait for service injection
        postTag.setItems(FXCollections.observableArrayList());
    }

    @Override
    public void setTagService(TagService tagService) {
        super.setTagService(tagService);
        loadTags(); // Load tags after service is injected
    }

    private void loadTags() {
        if (tagService == null) {
            // Service not yet injected, skip loading
            return;
        }
        try {
            List<TagRecord> tags = tagService.getAllTags();
            ObservableList<String> tagNames = FXCollections.observableArrayList();
            for (TagRecord tag : tags) {
                tagNames.add(tag.tag());
            }
            postTag.setItems(tagNames);
        } catch (DatabaseQueryException e) {
            // If tags can't be loaded, just continue without tags
            postTag.setItems(FXCollections.observableArrayList());
        }
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
                Post post = new Post(SessionManager.getInstance().getUserId(), postTile.getText().trim(), postContent.getText().trim(), status.toUpperCase());
                String postId = postService.createPost(post);
                
                // Link tag to post if selected
                if (postTag.getValue() != null && !postTag.getValue().trim().isEmpty()) {
                    try {
                        TagRecord selectedTag = tagService.getTagByTagName(postTag.getValue().trim());
                        if (selectedTag != null) {
                            tagService.linkTagToPost(postId, selectedTag.id());
                        }
                    } catch (DatabaseQueryException e) {
                        // Tag linking failed, but post was created, so just log and continue
                        System.err.println("Failed to link tag to post: " + e.getMessage());
                    }
                }
                
                showInfo("Post Created Successfully");
                switchTo("PostList");
            } catch (DatabaseQueryException e) {
                showError("Failed to create post. Please try again.");
            }
        }
    }


}
