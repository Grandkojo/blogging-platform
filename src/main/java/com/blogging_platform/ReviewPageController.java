package com.blogging_platform;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.ReviewRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Review;

/**
 * FXML controller for the review page (ReviewPage.fxml). Receives post id via navigation parameter;
 * displays post, average rating, and list of reviews; allows adding/editing/deleting reviews (author or admin post-owner).
 */
public class ReviewPageController extends BaseController implements ParameterReceiver {

    @FXML private Label postTitleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private HBox averageRatingContainer;
    @FXML private VBox reviewsContainer;
    @FXML private VBox addReviewForm;
    @FXML private TextArea reviewMessageInput;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private Label noReviewsLabel;

    private String currentPostId;
    private String postUserId;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @Override
    public void receiveParameter(Object parameter) {
        if (parameter instanceof String) {
            this.currentPostId = (String) parameter;
            displayPost(currentPostId);
        }
    }

    public void displayPost(String id) {
        try {
            PostRecord post = postService.getPost(id);
            this.postUserId = post.userId();
            postTitleLabel.setText(post.title());
            authorLabel.setText("by " + post.author());
            dateLabel.setText(post.publishedDate() != null ? post.publishedDate().format(formatter) : "");

            loadAverageRating(id);
            loadReviews(id);
            setupAddReviewForm();
        } catch (PostNotFoundException e) {
            showError(e.getUserMessage());
            switchTo("PostHome");
        } catch (DatabaseException e) {
            showError("Failed to load post. Please try again.");
        }
    }

    private void setupAddReviewForm() {
        ratingCombo.getItems().clear();
        ratingCombo.getItems().addAll(1, 2, 3, 4, 5);
        addReviewForm.setVisible(SessionManager.getInstance().isLoggedIn());
    }

    private void loadAverageRating(String postId) {
        averageRatingContainer.getChildren().clear();
        if (reviewService == null) return;
        try {
            double avg = reviewService.getAverageRating(postId);
            int fullStars = (int) Math.round(avg);
            fullStars = Math.max(0, Math.min(5, fullStars));
            StringBuilder stars = new StringBuilder();
            for (int i = 0; i < fullStars; i++) stars.append("★");
            for (int i = fullStars; i < 5; i++) stars.append("☆");
            Label starsLabel = new Label(stars.toString());
            starsLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #f1c40f;");
            Label ratingLabel = new Label(avg > 0 ? String.format("%.1f", avg) + " average" : "No reviews yet");
            ratingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            averageRatingContainer.getChildren().addAll(starsLabel, ratingLabel);
        } catch (DatabaseQueryException e) {
            Label noRating = new Label("No reviews yet");
            noRating.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            averageRatingContainer.getChildren().add(noRating);
        }
    }

    private void loadReviews(String postId) {
        reviewsContainer.getChildren().clear();
        if (reviewService == null) return;
        try {
            List<ReviewRecord> reviews = reviewService.getReviewsByPostId(postId);
            if (reviews == null || reviews.isEmpty()) {
                noReviewsLabel.setVisible(true);
                addReviewForm.setVisible(SessionManager.getInstance().isLoggedIn());
            } else {
                noReviewsLabel.setVisible(false);
                addReviewForm.setVisible(SessionManager.getInstance().isLoggedIn());
                String currentUserId = SessionManager.getInstance().getUserId();
                String userRole = SessionManager.getInstance().getUserRole();
                boolean isPostOwner = currentUserId != null && currentUserId.equals(postUserId);

                for (ReviewRecord review : reviews) {
                    VBox reviewBox = new VBox(10);
                    reviewBox.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 20; -fx-background-radius: 12;");

                    HBox headerBox = new HBox(10);
                    headerBox.setAlignment(Pos.CENTER_LEFT);

                    String authorName = review.authorName() != null ? review.authorName() : "Unknown";
                    Label author = new Label(authorName);
                    author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");

                    Hyperlink editButton = new Hyperlink();
                    Hyperlink deleteButton = new Hyperlink();

                    boolean isOwnReview = currentUserId != null && currentUserId.equals(review.userId());
                    boolean canEdit = isOwnReview;
                    boolean canDelete = isOwnReview || ("Admin".equals(userRole) && isPostOwner);

                    if (canEdit) {
                        editButton = new Hyperlink("Edit");
                        editButton.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #3498db;");
                        editButton.setOnAction(event -> handleEditReview(review.id()));
                    }
                    if (canDelete) {
                        deleteButton = new Hyperlink("Delete");
                        deleteButton.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #e74c3c;");
                        deleteButton.setOnAction(event -> handleDeleteReview(review.id()));
                    }

                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    headerBox.getChildren().addAll(author, spacer, editButton, deleteButton);

                    Label date = new Label(review.createdAt() != null
                            ? review.createdAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm"))
                            : "");
                    date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

                    int r = review.rating() != null ? review.rating() : 0;
                    StringBuilder starStr = new StringBuilder();
                    for (int i = 0; i < r; i++) starStr.append("★");
                    for (int i = r; i < 5; i++) starStr.append("☆");
                    Label starsLabel = new Label(starStr.toString());
                    starsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1c40f;");

                    Text message = new Text(review.message() != null ? review.message() : "");
                    message.setStyle("-fx-font-size: 16px; -fx-text-fill: #34495e;");
                    message.setWrappingWidth(800);

                    reviewBox.setUserData(review.id());
                    reviewBox.getChildren().addAll(headerBox, date, starsLabel, message);
                    reviewsContainer.getChildren().add(reviewBox);
                }
            }
        } catch (DatabaseException e) {
            showError("Failed to load reviews. Please try again.");
        }
    }

    private void handleDeleteReview(String reviewId) {
        Optional<ButtonType> result = confirmDialog("Do you want to delete this review?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                reviewService.deleteReview(reviewId);
                com.blogging_platform.classes.CacheManager.getInstance().invalidateCache();
                showInfo("Review deleted successfully");
                loadReviews(currentPostId);
                loadAverageRating(currentPostId);
            } catch (DatabaseException e) {
                showError("Failed to delete review. Please try again.");
            }
        }
    }

    private void handleEditReview(String reviewId) {
        for (Node node : reviewsContainer.getChildren()) {
            if (node instanceof VBox reviewBox && node.getUserData() != null && node.getUserData().equals(reviewId)) {
                ReviewRecord review;
                try {
                    review = reviewService.getReviewById(reviewId);
                } catch (DatabaseQueryException e) {
                    showError("Failed to load review.");
                    return;
                }
                if (review == null) return;

                reviewBox.getChildren().clear();

                Label author = new Label(review.authorName() != null ? review.authorName() : "Unknown");
                author.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2c3e50;");
                Label date = new Label(review.createdAt() != null
                        ? review.createdAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' HH:mm"))
                        : "");
                date.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 14px;");

                ComboBox<Integer> ratingEdit = new ComboBox<>();
                ratingEdit.getItems().addAll(1, 2, 3, 4, 5);
                ratingEdit.setValue(review.rating() != null ? review.rating() : 5);
                ratingEdit.setPrefWidth(80);
                HBox ratingRow = new HBox(8);
                ratingRow.getChildren().addAll(new Label("Rating:"), ratingEdit);

                TextArea editArea = new TextArea(review.message() != null ? review.message() : "");
                editArea.setWrapText(true);
                editArea.setPrefRowCount(4);
                editArea.setStyle("-fx-font-size: 16px; -fx-background-radius: 8;");

                Button saveBtn = new Button("Save");
                saveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 6;");
                Button cancelBtn = new Button("Cancel");
                cancelBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16; -fx-background-radius: 6;");
                HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
                buttonBox.setAlignment(Pos.CENTER_RIGHT);

                saveBtn.setOnAction(e -> {
                    Integer newRating = ratingEdit.getValue();
                    String newMessage = editArea.getText().trim();
                    if (newMessage.isEmpty()) {
                        showError("Review message cannot be empty.");
                        return;
                    }
                    try {
                        Review updated = new Review(reviewId, review.postId(), review.userId(), newRating != null ? newRating : 5, newMessage);
                        reviewService.updateReview(updated);
                        com.blogging_platform.classes.CacheManager.getInstance().invalidateCache();
                        showInfo("Review updated!");
                        loadReviews(currentPostId);
                        loadAverageRating(currentPostId);
                    } catch (DatabaseException ex) {
                        showError("Failed to update review. Please try again.");
                    }
                });
                cancelBtn.setOnAction(e -> {
                    loadReviews(currentPostId);
                });

                reviewBox.getChildren().addAll(author, date, ratingRow, editArea, buttonBox);
                reviewBox.setStyle("-fx-background-color: #f0f8ff; -fx-padding: 20; -fx-background-radius: 12; -fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 12;");
                break;
            }
        }
    }

    @FXML
    private void postReview() {
        Integer rating = ratingCombo.getValue();
        String message = reviewMessageInput.getText().trim();
        if (rating == null) {
            showError("Please select a rating (1-5).");
            return;
        }
        if (message.isEmpty()) {
            showError("Review message cannot be empty.");
            return;
        }
        try {
            Review review = new Review(currentPostId, SessionManager.getInstance().getUserId(), rating, message);
            reviewService.createReview(review);
            com.blogging_platform.classes.CacheManager.getInstance().invalidateCache();
            reviewMessageInput.clear();
            ratingCombo.setValue(null);
            loadReviews(currentPostId);
            loadAverageRating(currentPostId);
            showInfo("Review added!");
        } catch (DuplicateResourceException e) {
            showError("You have already reviewed this post.");
        } catch (DatabaseException e) {
            showError("Failed to add review. Please try again.");
        }
    }

    @FXML
    private void goBack() {
        switchTo("SinglePostView", currentPostId);
    }
}
