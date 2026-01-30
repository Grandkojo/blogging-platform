package com.blogging_platform;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import com.blogging_platform.classes.CacheManager;
import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.TagRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.DatabaseQueryException;
import com.blogging_platform.exceptions.DuplicateResourceException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.model.Tag;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.TagService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.util.Callback;

/**
 * FXML controller for My Blog Posts (PostList.fxml). Displays the current user's posts in a table
 * with in-memory search (title, author, tag) and sort; supports edit, delete, add post, and (Admin) create tag.
 */
public class PostListController extends BaseController implements Initializable {

    @FXML
    private TableColumn<PostRecord, String> idColumn;

    @FXML
    private TableColumn<PostRecord, String> titleColumn;

    @FXML
    private TableColumn<PostRecord, String> statusColumn;

    @FXML
    private TableColumn<PostRecord, String> authorColumn;

    @FXML
    private TableColumn<PostRecord, LocalDateTime> createdAtColumn;

    @FXML
    private TableColumn<PostRecord, LocalDateTime> dateColumn;

    @FXML
    private TableView<PostRecord> postsTable;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private Button createTagButton;

    private ObservableList<PostRecord> postData = FXCollections.observableArrayList();
    /** Full list from DB for in-memory filter/sort (no DB on search). */
    private List<PostRecord> fullPostList = new ArrayList<>();
    /** Post id -> tag names for search by tag. */
    private Map<String, List<String>> postIdToTagNames = new ConcurrentHashMap<>();

    private static final String SORT_DATE_DESC = "Newest first";
    private static final String SORT_DATE_ASC = "Oldest first";
    private static final String SORT_TITLE_ASC = "Title A–Z";
    private static final String SORT_TITLE_DESC = "Title Z–A";
    private static final String SORT_AUTHOR_ASC = "Author A–Z";
    private static final String SORT_AUTHOR_DESC = "Author Z–A";

    public static <S> Callback<TableColumn<S, LocalDateTime>, TableCell<S, LocalDateTime>> getDateCellFactory() {
        return column -> { 
            return new TableCell<S, LocalDateTime>() { 
                private final DateTimeFormatter formatter = 
                    DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

                @Override
                protected void updateItem(LocalDateTime item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(formatter.format(item));
                    }
                }
            };
        }; 
    }



    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        idColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().id()));
        
        titleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().title()));
        
        authorColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().author()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().status()));

        createdAtColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().createdAt()));
        createdAtColumn.setCellFactory(getDateCellFactory());


        dateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<>(cellData.getValue().publishedDate()));
        dateColumn.setCellFactory(getDateCellFactory());
    
        postsTable.setItems(postData);

        if (sortComboBox != null) {
            sortComboBox.getItems().setAll(SORT_DATE_DESC, SORT_DATE_ASC, SORT_TITLE_ASC, SORT_TITLE_DESC, SORT_AUTHOR_ASC, SORT_AUTHOR_DESC);
            sortComboBox.setValue(SORT_DATE_DESC);
            sortComboBox.valueProperty().addListener((o, oldVal, newVal) -> applyFilterAndSort());
        }
        
        String userRole = SessionManager.getInstance().getUserRole();
        if (userRole != null && userRole.equals("Admin")) {
            createTagButton.setVisible(true);
        } else {
            createTagButton.setVisible(false);
        }
    }

    @Override
    public void setPostService(PostService postService) {
        super.setPostService(postService);
        loadPosts();
    }

    @Override
    public void setTagService(TagService tagService) {
        super.setTagService(tagService);
        if (postService != null) loadPosts();
    }

    private void loadPosts() {
        try {
            fullPostList = postService.getUserPosts(SessionManager.getInstance().getUserId());
            postIdToTagNames.clear();
            if (tagService != null) {
                for (PostRecord p : fullPostList) {
                    try {
                        List<TagRecord> tags = tagService.getTagsByPostId(p.id());
                        List<String> names = new ArrayList<>();
                        if (tags != null) for (TagRecord t : tags) names.add(t.tag());
                        postIdToTagNames.put(p.id(), names);
                    } catch (DatabaseQueryException e) {
                        postIdToTagNames.put(p.id(), List.of());
                    }
                }
            }
            applyFilterAndSort();
        } catch (DatabaseException e) {
            showError("Failed to load posts. Please try again.");
        }
    }

    /** In-memory filter by title/author/tag then QuickSort via CacheManager. */
    private void applyFilterAndSort() {
        String query = searchField != null ? searchField.getText().trim() : "";
        String q = query.isEmpty() ? null : query.toLowerCase();
        List<PostRecord> filtered = new ArrayList<>();
        for (PostRecord p : fullPostList) {
            if (q == null || matchesSearch(p, q)) filtered.add(p);
        }
        String sortBy = getSortByKey();
        CacheManager.getInstance().sortPosts(filtered, sortBy);
        postData.clear();
        postData.addAll(filtered);
    }

    private boolean matchesSearch(PostRecord p, String q) {
        if (p.title() != null && p.title().toLowerCase().contains(q)) return true;
        if (p.author() != null && p.author().toLowerCase().contains(q)) return true;
        List<String> tags = postIdToTagNames.get(p.id());
        if (tags != null) for (String t : tags) if (t != null && t.toLowerCase().contains(q)) return true;
        return false;
    }

    private String getSortByKey() {
        String v = sortComboBox != null ? sortComboBox.getValue() : null;
        if (SORT_DATE_ASC.equals(v)) return "date_asc";
        if (SORT_TITLE_ASC.equals(v)) return "title_asc";
        if (SORT_TITLE_DESC.equals(v)) return "title_desc";
        if (SORT_AUTHOR_ASC.equals(v)) return "author_asc";
        if (SORT_AUTHOR_DESC.equals(v)) return "author_desc";
        return "date_desc";
    }

    @FXML
    void searchPosts(ActionEvent event) {
        applyFilterAndSort();
    }

    @FXML
    void deletePost(ActionEvent event) {
        PostRecord selected = postsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            return;
        }
        String postId = selected.id();
        Optional<ButtonType> result =  confirmDialog("Delete post \"" + selected.title() + "\"?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                postService.deletePost(postId, SessionManager.getInstance().getUserId());
                showInfo("Post deleted successfully");
                loadPosts();
            } catch (PostNotFoundException e) {
                showError(e.getUserMessage());
                loadPosts();
            } catch (DatabaseException e) {
                showError("Failed to delete post. Please try again.");
            }
        }
    }

    @FXML
    void editPost(ActionEvent event) {
        PostRecord selected = postsTable.getSelectionModel().getSelectedItem();
        String postId = selected.id();
        switchTo("EditPost", postId);
        setCurrentId(postId);
    }


    @FXML
    void gotHome(ActionEvent event) {
        switchTo("PostHome");
    }

    @FXML
    void openAddPost(ActionEvent event) {
        switchTo("AddPost");
    }

    @FXML
    void openCreateTagDialog(ActionEvent event) {
        if (tagService == null) {
            showError("Tag service is not available. Please try again.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create New Tag");
        dialog.setHeaderText("Enter tag name");
        dialog.setContentText("Tag name:");

        Optional<String> result = dialog.showAndWait();
        
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            String tagName = result.get().trim();
            try {
                Tag tag = new Tag(tagName);
                tagService.createTag(tag);
                showInfo("Tag '" + tagName + "' created successfully!");
            } catch (DuplicateResourceException e) {
                showError("Tag '" + tagName + "' already exists!");
            } catch (DatabaseQueryException e) {
                showError("Failed to create tag. Please try again.");
            }
        }
    }
}
