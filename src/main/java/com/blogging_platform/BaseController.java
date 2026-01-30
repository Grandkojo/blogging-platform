package com.blogging_platform;


import java.io.IOException;
import java.util.Optional;

import com.blogging_platform.service.CommentService;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.ReviewService;
import com.blogging_platform.service.TagService;
import com.blogging_platform.service.UserService;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

/**
 * Base controller that all FXML controllers extend.
 * Holds references to the main {@link App} and injected services (user, post, comment, tag, review),
 * and provides helpers for scene switching and dialogs (error, info, confirm).
 */
public abstract class BaseController {

    private App app;
    private String currentId;

    protected UserService userService;
    protected PostService postService;
    protected CommentService commentService;
    protected TagService tagService;
    protected ReviewService reviewService;

    /**
     * Sets the application reference. Called by {@link App} after loading FXML; do not call manually.
     *
     * @param app the application instance
     */
    public void setApp(App app) {
        this.app = app;
    }

    /** Sets the user service (injected by App). */
    public void setUserService(UserService userService) { this.userService = userService; }

    /** Sets the post service (injected by App). */
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    /** Sets the comment service (injected by App). */
    public void setCommentSerivce(CommentService commentService) {
        this.commentService = commentService;
    }

    /** Sets the tag service (injected by App). */
    public void setTagService(TagService tagService) {
        this.tagService = tagService;
    }

    /** Sets the review service (injected by App). */
    public void setReviewService(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Switches to another FXML scene and passes a string parameter (e.g. post id) to its controller.
     *
     * @param fxmlName base name of the FXML file (e.g. "SinglePostView")
     * @param id       optional parameter passed to the controller if it implements {@link com.blogging_platform.classes.ParameterReceiver}
     */
    protected void switchTo(String fxmlName, String id) {
        if (app == null) {
            System.err.println("App reference is null! Cannot switch scene.");
            return;
        }
        try {
            App.setRoot(fxmlName, id);
        } catch (IOException e) {
            showError("Could not load " + fxmlName + ".fxml");
        }
    }

    /**
     * Switches to another FXML scene without passing a parameter.
     *
     * @param fxmlName base name of the FXML file (e.g. "Login", "PostHome")
     */
    protected void switchTo(String fxmlName) {
        if (app == null) {
            System.err.println("App reference is null! Cannot switch scene.");
            return;
        }
        try {
            App.setRoot(fxmlName, null);
        } catch (IOException e) {
            showError("Could not load " + fxmlName + ".fxml");
        }
    }

    /**
     * Shows an error alert with the given message.
     *
     * @param message the error text to display
     */
    protected void showError(String message) {
        Alert alert = new Alert(
            Alert.AlertType.ERROR
        );
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert with the given message.
     *
     * @param message the text to display
     */
    protected void showInfo(String message) {
        javafx.scene.control.Alert alert = new Alert(
            Alert.AlertType.INFORMATION
        );
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns the user's choice.
     *
     * @param message the confirmation question
     * @return the selected button (e.g. OK, Cancel), or empty if dismissed
     */
    protected Optional<ButtonType> confirmDialog(String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Confirm");
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    /**
     * Stores an identifier for the current context (e.g. post id).
     *
     * @param id the identifier to store
     */
    protected void setCurrentId(String id) {
        this.currentId = id;
    }

    /**
     * Returns the stored context identifier.
     *
     * @return the current id, or null if not set
     */
    protected String getCurrentId() {
        return this.currentId;
    }
}
