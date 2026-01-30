package com.blogging_platform;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.PostNotFoundException;
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
 * FXML controller for Edit Post (EditPost.fxml). Loads an existing post by id (from navigation parameter),
 * allows editing title, content, status, and tag; on save updates the post and tag association.
 */
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

    @FXML
    private ComboBox<String> postTag;

    private String currentPostId;

    private void loadTags() {
        if (tagService == null) return;
        try {
            List<TagRecord> tags = tagService.getAllTags();
            ObservableList<String> tagNames = FXCollections.observableArrayList();
            for (TagRecord tag : tags) {
                tagNames.add(tag.tag());
            }
            postTag.setItems(tagNames);
        } catch (DatabaseQueryException e) {
            postTag.setItems(FXCollections.observableArrayList());
        }
    }

    private void setCurrentPostTag(String postId) {
        if (tagService == null) return;
        try {
            List<TagRecord> tags = tagService.getTagsByPostId(postId);
            if (tags != null && !tags.isEmpty()) {
                postTag.setValue(tags.get(0).tag());
            } else {
                postTag.setValue(null); // no tag – like "added fresh" (prompt shows)
            }
        } catch (DatabaseQueryException e) {
            postTag.setValue(null);
        }
    }

    @Override
    public void setTagService(TagService tagService) {
        super.setTagService(tagService);
        loadTags();
    }

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

            // Set tag dropdown: existing tag if any, otherwise unselected (like create)
            setCurrentPostTag(postId);
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
        postTag.setItems(FXCollections.observableArrayList());
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

                // Update tag: unlink all, then link selected (same behaviour as create when adding fresh)
                if (tagService != null) {
                    try {
                        tagService.unlinkAllTagsFromPost(currentPostId);
                        if (postTag.getValue() != null && !postTag.getValue().trim().isEmpty()) {
                            TagRecord selectedTag = tagService.getTagByTagName(postTag.getValue().trim());
                            if (selectedTag != null) {
                                tagService.linkTagToPost(currentPostId, selectedTag.id());
                            }
                        }
                    } catch (DatabaseQueryException e) {
                        System.err.println("Failed to update tag for post: " + e.getMessage());
                    }
                }

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
