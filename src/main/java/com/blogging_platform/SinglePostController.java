package com.blogging_platform;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import com.blogging_platform.classes.ParameterReceiver;

public class SinglePostController extends BaseController implements ParameterReceiver {

    @FXML private Label postTitleLabel;
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label postContentText;

    private String currentPostId;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public void displayPost(String id) {
        MySQLDriver sqlDriver = new MySQLDriver();
        ArrayList<String> post = sqlDriver.getFullPostById(id);
        postTitleLabel.setText(post.get(1));
        postContentText.setText(post.get(2));
        authorLabel.setText("by " + post.get(5));
        dateLabel.setText(post.get(4).formatted(formatter));
    }

    @Override
    public void receiveParameter(Object parameter) {
         if (parameter instanceof String) {
            this.currentPostId = (String) parameter;
            displayPost(currentPostId);
        }
    }

    @FXML
    private void goBack() {
        switchTo("PostHome");
    }

}