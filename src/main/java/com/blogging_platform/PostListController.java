package com.blogging_platform;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import com.blogging_platform.classes.PostRecord;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

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
    private TableColumn<PostRecord, LocalDateTime> dateColumn;

    @FXML
    private TableView<PostRecord> postsTable;

    @FXML
    private TextField searchField;

    private ObservableList<PostRecord> postData = FXCollections.observableArrayList();

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
        
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleObjectProperty<LocalDateTime>(cellData.getValue().publishedDate()));
    

        postsTable.setItems(postData);

        loadPosts();
    }

    private void loadPosts(){
        postData.clear();
        MySQLDriver sqlDriver = new MySQLDriver();
        List<PostRecord> posts = sqlDriver.listPosts();
        postData.addAll(posts);

    }

    @FXML
    void searchPosts(ActionEvent event) {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()){
            loadPosts();
            return;
        }
        postData.clear();
        MySQLDriver sqlDriver = new MySQLDriver();
        List<PostRecord> f_posts = sqlDriver.listPostsBySearch(query);
        postData.addAll(f_posts);

    }

    @FXML
    void deletePost(ActionEvent event) {
        PostRecord selected = postsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Delete: " + selected.title());
        }
    }

    @FXML
    void editPost(ActionEvent event) {
        PostRecord selected = postsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            System.out.println("Edit: " + selected.title());
        }
    }

    @FXML
    void openAddPost(ActionEvent event) {
        switchTo("AddPost");
    }
}
