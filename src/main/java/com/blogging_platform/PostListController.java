package com.blogging_platform;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import com.blogging_platform.classes.PostRecord;
import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.exceptions.DatabaseException;
import com.blogging_platform.exceptions.PostNotFoundException;
import com.blogging_platform.service.PostService;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Callback;

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

    private ObservableList<PostRecord> postData = FXCollections.observableArrayList();

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
    
    }

    @Override
    public void setPostService(PostService postService) {
        super.setPostService(postService);
        loadPosts();
    }

    private void loadPosts(){
        try {

            postData.clear();
            postData.addAll(postService.getUserPosts(SessionManager.getInstance().getUserId()));
        } catch (DatabaseException e) {
            showError("Failed to load posts. Please try again.");
        }
    }

    @FXML
    void searchPosts(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()){
            loadPosts();
            return;
        }
        try {
            postData.clear();
            MySQLDriver sqlDriver = new MySQLDriver();
            List<PostRecord> f_posts = sqlDriver.listPostsBySearch(query);
            postData.addAll(f_posts);
        } catch (DatabaseException e) {
            showError("Failed to search posts. Please try again.");
        }
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
}
