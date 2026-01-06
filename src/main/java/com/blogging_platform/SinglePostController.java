package com.blogging_platform;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import com.blogging_platform.classes.CommentRecord;
import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.SessionManager;

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

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public void displayPost(String id) {
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> post = sqlDriver.getFullPostById(id);
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

                Label author = new Label(comment.authorName());
                author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                Label date = new Label(comment.date().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm")));
                date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

                Text content = new Text(comment.content());
                content.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
                content.setWrappingWidth(800);

                commentBox.getChildren().addAll(author, date, content);
                commentsContainer.getChildren().add(commentBox);
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