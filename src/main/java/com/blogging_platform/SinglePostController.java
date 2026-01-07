package com.blogging_platform;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.User;

public class SinglePostController extends BaseController implements ParameterReceiver {

    @FXML private Label postTitleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label postContentText;
    @FXML private VBox addCommentForm;
    @FXML private TextArea commentInput;
    @FXML private Label noCommentsLabel;

    @FXML private VBox commentsContainer;

    private String currentPostId;

    private String postUserId;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public void displayPost(String id) {
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> post = sqlDriver.getFullPostById(id);
        this.postUserId = post.get(6);
        postTitleLabel.setText(post.get(1));
        postContentText.setText(post.get(2));
        authorLabel.setText("by " + post.get(5));
        dateLabel.setText(post.get(4).formatted(formatter));

        loadComments(id);
    }

    @Override
    public void receiveParameter(Object parameter) {
         if (parameter instanceof String) {
            this.currentPostId = (String) parameter;
            displayPost(currentPostId);
        }
    }
    private void loadComments(String postId) {
        commentsContainer.getChildren().clear();

        MySQLDriver sqlDriver = new MySQLDriver();
        List<CommentRecord> comments = sqlDriver.getCommentsByPostId(postId);

        if (comments.isEmpty()) {
            noCommentsLabel.setVisible(true);
            addCommentForm.setVisible(SessionManager.getInstance().isLoggedIn());
        } else {
            noCommentsLabel.setVisible(false);
            addCommentForm.setVisible(SessionManager.getInstance().isLoggedIn());

            for (CommentRecord comment : comments) {
                VBox commentBox = new VBox(10);
                commentBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 12;");

                // Label author = new Label(comment.authorName());
                // author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                // Hyperlink editButton = new Hyperlink("Edit");
                // editButton.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                // Hyperlink deleteButton = new Hyperlink("Delete");
                // deleteButton.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");


                // Label date = new Label(comment.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")));
                // date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

                // Text content = new Text(comment.content());
                // content.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
                // content.setWrappingWidth(800);

                // commentBox.getChildren().addAll(author, date, content, editButton, deleteButton);
                // commentsContainer.getChildren().add(commentBox);

                HBox headerBox = new HBox(10); // HBox with spacing of 10
                headerBox.setAlignment(Pos.CENTER_LEFT); // Align items vertically in the center

                Label author = new Label(comment.authorName());
                author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                Hyperlink editButton = new Hyperlink();
                Hyperlink deleteButton = new Hyperlink();

                //if comment belongs to logged in user
                if (comment.userId().equals(SessionManager.getInstance().getUserId())){
                    editButton = new Hyperlink("Edit");
                    editButton.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3498db;"); // Adjusted font size slightly
                    editButton.setOnAction(event -> handleEdit(comment.id())); // Add your event handler method
                }
                //if comment belongs to the logged in user or logged in user is the owner of the post
                if (comment.userId().equals(SessionManager.getInstance().getUserId()) || SessionManager.getInstance().getUserId().equals(this.postUserId)){
                    deleteButton = new Hyperlink("Delete");
                    deleteButton.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;"); // Add a delete color
                    deleteButton.setOnAction(event -> handleDelete(comment.id())); // Add your event handler method
                }

                // Use Region as a spacer that grows to push buttons to the right
                javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                // Add elements to the HBox: Author, then a growing spacer, then buttons
                headerBox.getChildren().addAll(author, spacer, editButton, deleteButton);
                // --- End of new HBox structure ---

                // The rest of your existing elements
                Label date = new Label(comment.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")));
                date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

                Text content = new Text(comment.content());
                content.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
                content.setWrappingWidth(800);

                commentBox.setUserData(comment.id());

                // Add the new headerBox to the main VBox instead of the 'author' label alone
                commentBox.getChildren().addAll(headerBox, date, content);
                commentsContainer.getChildren().add(commentBox);
            }
        }
    }

    private void handleDelete(String commentId) {
        Optional<ButtonType> result = confirmDialog("Do you want to delete this comment?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            MySQLDriver sqlDriver = new MySQLDriver();
            sqlDriver.deleteComment(commentId);
            loadComments(currentPostId);
        } else {
            System.out.println("Delete cancelled");
        }

    }

    private void handleEdit(String commentId) {
    // Find the comment box that contains this commentId
    for (Node node : commentsContainer.getChildren()) {
        if (node instanceof VBox commentBox && node.getUserData() != null 
            && node.getUserData().equals(commentId)) {

            // Clear current content
            commentBox.getChildren().clear();

            // Get current comment data (we'll reuse the existing CommentRecord if possible)
            // But for simplicity, fetch fresh from DB
            MySQLDriver sqlDriver = new MySQLDriver();
            CommentRecord comment = sqlDriver.getCommentById(commentId, this.currentPostId);

            if (comment == null) return;

            // Author + Date (unchanged)
            Label author = new Label(comment.authorName());
            author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

            Label date = new Label(comment.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")));
            date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

            // Editable TextArea
            TextArea editArea = new TextArea(comment.content());
            editArea.setWrapText(true);
            editArea.setPrefRowCount(4);
            editArea.setStyle("-fx-font-size: 16px; -fx-background-radius: 8;");

            // Save + Cancel buttons
            Button saveBtn = new Button("Save");
            saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 6;");

            Button cancelBtn = new Button("Cancel");
            cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 6;");

            HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);

            // Save action
            saveBtn.setOnAction(e -> {
                String newContent = editArea.getText().trim();
                if (newContent.isEmpty()) {
                    showError("Comment cannot be empty");
                    return;
                }

                boolean updated = sqlDriver.updateComment(commentId, newContent);
                if (updated) {
                    showInfo("Comment updated!");
                    loadComments(currentPostId); // refresh all comments
                } else {
                    showError("Failed to update comment");
                }
            });

            // Cancel action — reload comments to reset
            cancelBtn.setOnAction(e -> loadComments(currentPostId));

            // Rebuild the comment box in edit mode
            commentBox.getChildren().addAll(author, date, editArea, buttonBox);
            commentBox.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 12;");

            break;
        }
    }
}

    @FXML
    private void postComment() {
        String content = commentInput.getText().trim();
        if (content.isEmpty()) {
            showError("Comment cannot be empty");
            return;
        }

        MySQLDriver sqlDriver = new MySQLDriver();
        String currentUserId = SessionManager.getInstance().getUserId();

        boolean success = sqlDriver.addComment(currentPostId, currentUserId, content);

        if (success) {
            commentInput.clear();
            loadComments(currentPostId);  // refresh
            showInfo("Comment added!");
        } else {
            showError("Failed to add comment");
        }
    }

    @FXML
    private void goBack() {
        switchTo("PostHome");
    }

}
