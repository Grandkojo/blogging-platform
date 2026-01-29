package com.blogging_platform;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.blogging_platform.classes.CacheManager;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.exceptions.DatabaseQueryException;

public class PostHomeController extends BaseController {

    @FXML private FlowPane postsFlowPane;

    @FXML
    private Hyperlink blogLink;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    private static final String SORT_DATE_DESC = "Newest first";
    private static final String SORT_DATE_ASC = "Oldest first";
    private static final String SORT_TITLE_ASC = "Title A–Z";
    private static final String SORT_TITLE_DESC = "Title Z–A";
    private static final String SORT_AUTHOR_ASC = "Author A–Z";
    private static final String SORT_AUTHOR_DESC = "Author Z–A";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    @FXML
    private void initialize() {
        String userRole = SessionManager.getInstance().getUserRole();
        if (userRole != null && userRole.equals("Admin")){
            blogLink.setDisable(false);
            blogLink.setVisible(true);
        }
        if (sortComboBox != null) {
            sortComboBox.getItems().setAll(SORT_DATE_DESC, SORT_DATE_ASC, SORT_TITLE_ASC, SORT_TITLE_DESC, SORT_AUTHOR_ASC, SORT_AUTHOR_DESC);
            sortComboBox.setValue(SORT_DATE_DESC);
            sortComboBox.valueProperty().addListener((o, oldVal, newVal) -> {
                if (reviewService != null && tagService != null) loadAllPosts();
            });
        }
    }

    @Override
    public void setTagService(com.blogging_platform.service.TagService tagService) {
        super.setTagService(tagService);
        com.blogging_platform.classes.CacheManager.getInstance().setTagService(tagService);
    }

    @Override
    public void setReviewService(com.blogging_platform.service.ReviewService reviewService) {
        super.setReviewService(reviewService);
        // Load posts after both tagService and reviewService are injected (so ratings show on cards)
        if (reviewService != null && tagService != null) {
            loadAllPosts();
        }
    }

    @FXML
    private void searchPosts() {
        loadAllPosts();
    }

    /** Map UI sort label to CacheManager sortBy key. */
    private String getSortByKey() {
        String v = sortComboBox != null ? sortComboBox.getValue() : null;
        if (SORT_DATE_ASC.equals(v)) return "date_asc";
        if (SORT_TITLE_ASC.equals(v)) return "title_asc";
        if (SORT_TITLE_DESC.equals(v)) return "title_desc";
        if (SORT_AUTHOR_ASC.equals(v)) return "author_asc";
        if (SORT_AUTHOR_DESC.equals(v)) return "author_desc";
        return "date_desc";
    }

    private void loadAllPosts() {
        CacheManager cache = CacheManager.getInstance();
        String query = searchField != null ? searchField.getText().trim() : "";
        String sortBy = getSortByKey();
        List<PostRecord> posts = cache.getPublishedPostsSearchAndSort(query, sortBy);
        postsFlowPane.getChildren().clear();

        for (PostRecord post : posts) {
            List<TagRecord> tags = getTagsForPost(post.id());
            double avgRating = getAverageRating(post.id());
            Node card = createPostCard(post.title(), post.content(), post.status(), post.author(), post.publishedDate(), post.id(), post.commentCount() != null ? post.commentCount() : 0, tags, avgRating);
            postsFlowPane.getChildren().add(card);
        }
    }

    private List<TagRecord> getTagsForPost(String postId) {
        if (tagService == null) {
            return new ArrayList<>();
        }
        try {
            return tagService.getTagsByPostId(postId);
        } catch (DatabaseQueryException e) {
            return new ArrayList<>();
        }
    }

    private double getAverageRating(String postId) {
        if (reviewService == null) {
            return 0.0;
        }
        try {
            return reviewService.getAverageRating(postId);
        } catch (DatabaseQueryException e) {
            return 0.0;
        }
    }

    private Node createPostCard(String title, String content, String status,
                                String author, LocalDateTime publishedDate, String id, int commentCount, List<TagRecord> tags, double avgRating) {

        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 12;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);
            """);
        card.setMaxWidth(350);
        card.setPrefWidth(350);

        // Title and Tags container
        VBox titleContainer = new VBox(8);
        
        // Title row with tags beside it
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        titleRow.setPrefWidth(310); // Match card width minus padding
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        
        titleRow.getChildren().add(titleLabel);
        
        // Add tags beside the title (on the same row)
        if (tags != null && !tags.isEmpty()) {
            HBox tagsBox = new HBox(5);
            tagsBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            for (TagRecord tag : tags) {
                Label tagLabel = new Label(tag.tag());
                tagLabel.setStyle("""
                    -fx-background-color: #e8f5e9;
                    -fx-background-radius: 12;
                    -fx-padding: 4 10;
                    -fx-font-size: 11px;
                    -fx-text-fill: #2e7d32;
                    -fx-font-weight: bold;
                    """);
                tagsBox.getChildren().add(tagLabel);
            }
            titleRow.getChildren().add(tagsBox);
        }
        
        titleContainer.getChildren().add(titleRow);

        // Author & Date
        String metaText = "by " + author + " • " + publishedDate.format(dateFormatter);
        Label metaLabel = new Label(metaText);
        metaLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        Label commentLabel = new Label(commentCount + (commentCount == 1 ? " comment" : " comments"));
        commentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #3498db;");

        // Rating stars (clickable → Review page)
        HBox ratingBox = new HBox(6);
        ratingBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        int fullStars = (int) Math.round(avgRating);
        fullStars = Math.max(0, Math.min(5, fullStars));
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) stars.append("★");
        for (int i = fullStars; i < 5; i++) stars.append("☆");
        Label starsLabel = new Label(stars.toString());
        starsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f1c40f;");
        Label ratingText = new Label(avgRating > 0 ? String.format("%.1f", avgRating) : "No reviews");
        ratingText.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");
        ratingBox.getChildren().addAll(starsLabel, ratingText);
        ratingBox.setStyle("-fx-cursor: hand;");
        ratingBox.setOnMouseClicked(e -> {
            e.consume();
            switchTo("ReviewPage", id);
        });

        // Short excerpt
        String excerpt = content.length() > 150
                ? content.substring(0, 150) + "..."
                : content;
        Text preview = new Text(excerpt);
        preview.setStyle("-fx-font-size: 15px; -fx-text-fill: #555;");
        preview.setWrappingWidth(310);

        card.getChildren().addAll(titleContainer, metaLabel, commentLabel, ratingBox, preview);

        // Click anywhere on the card → open full postz
        card.setOnMouseClicked(e -> openSinglePost(id));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() +
                "-fx-cursor: hand; -fx-background-color: #f8f9fa;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle()
                .replace("-fx-cursor: hand; -fx-background-color: #f8f9fa;", "")));

        return card;
    }

    private void openSinglePost(String id) {
        switchTo("SinglePostView", id);  
    }

    @FXML
    void goToPostList(ActionEvent event) {
        switchTo("PostList");
    }

    @FXML
    void logout(ActionEvent event) {
        Optional<ButtonType> result = confirmDialog("Do you want to logout?");
        if (result.isPresent() && result.get() == ButtonType.OK) {
            userService.logout();
            switchTo("Login");
        } else {
            System.out.println("Logout cancelled");
        }
    }
}