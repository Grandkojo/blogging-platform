package com.blogging_platform;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.dao.interfaces.CommentDAO;
import com.blogging_platform.dao.interfaces.PostDAO;
import com.blogging_platform.dao.interfaces.ReviewDAO;
import com.blogging_platform.dao.interfaces.TagDAO;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcCommentDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcPostDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcReviewDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcTagDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcUserDAO;
import com.blogging_platform.service.CommentService;
import com.blogging_platform.service.PostService;
import com.blogging_platform.service.ReviewService;
import com.blogging_platform.service.TagService;
import com.blogging_platform.service.UserService;

/**
 * Main JavaFX application for the Blogging Platform.
 * Initializes DAOs and services, manages the primary scene, and injects dependencies
 * into FXML controllers. Also handles scene transitions (e.g. Login, PostHome, SinglePostView).
 */
public class App extends Application {

    private static App instance;
    private static Scene scene;

    private UserService userService;
    private PostService postService;
    private CommentService commentService;
    private TagService tagService;
    private ReviewService reviewService;


    /** Returns the shared user service. */
    public UserService getUserService() { return userService; }
    /** Returns the shared post service. */
    public PostService getPostService() { return postService; }
    /** Returns the shared comment service. */
    public CommentService getCommentService() { return commentService; }
    /** Returns the shared tag service. */
    public TagService getTagService() { return tagService; }
    /** Returns the shared review service. */
    public ReviewService getReviewService() { return reviewService; }

    public App() { instance = this; }

    /** Returns the singleton application instance. */
    public static App getInstance() { return instance; }

    @Override
    public void start(Stage stage) throws IOException {

        //initialize the daos
        UserDAO userDAO = new JdbcUserDAO();
        PostDAO postDAO = new JdbcPostDAO();
        CommentDAO commentDAO = new JdbcCommentDAO();
        TagDAO tagDAO = new JdbcTagDAO();
        ReviewDAO reviewDAO = new JdbcReviewDAO();

        //initialize services
        this.userService = new UserService(userDAO);
        this.postService = new PostService(postDAO);
        this.commentService = new CommentService(commentDAO);
        this.tagService = new TagService(tagDAO);
        this.reviewService = new ReviewService(reviewDAO);

        scene = new Scene(loadFXML("Login"));
        stage.setResizable(true);
        stage.setFullScreen(true);  
        stage.setTitle("Blogging Platform");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Replaces the scene root with the given FXML view and passes an optional parameter to the controller.
     *
     * @param fxml      base name of the FXML file (e.g. "Login", "SinglePostView")
     * @param parameter optional data passed to the controller if it implements {@link ParameterReceiver}
     * @throws IOException if the FXML cannot be loaded
     */
    static void setRoot(String fxml, Object parameter) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = loader.load();

        // Inject App instance into controller
        Object controller = loader.getController();
        if (controller instanceof BaseController baseController) {
            baseController.setApp(App.getInstance());
            baseController.setUserService(getInstance().getUserService()); 
            baseController.setPostService(getInstance().getPostService());
            baseController.setCommentSerivce(getInstance().getCommentService());
            baseController.setTagService(getInstance().getTagService());
            baseController.setReviewService(getInstance().getReviewService());

        }

        if (controller instanceof ParameterReceiver receiver){
            receiver.receiveParameter(parameter);
        }
        
        scene.setRoot(root);
    }

    /**
     * Replaces the scene root with the given FXML view (no parameter).
     *
     * @param fxml base name of the FXML file
     * @throws IOException if the FXML cannot be loaded
     */
    static void setRoot(String fxml) throws IOException {
        setRoot(fxml, null);
    }

    /**
     * Loads an FXML file and injects App and services into its controller.
     *
     * @param fxml base name of the FXML file (without .fxml)
     * @return the root node of the loaded FXML
     * @throws IOException if the FXML cannot be loaded
     */
    private static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    Parent parent = fxmlLoader.load();

    // Inject the App instance into the controller if it extends BaseController
    Object controller = fxmlLoader.getController();
    if (controller instanceof BaseController baseController) {
        baseController.setApp(getInstance()); 

        // Pass the services from the App instance to the Controller
        baseController.setUserService(getInstance().getUserService());
        baseController.setPostService(getInstance().getPostService());
        baseController.setCommentSerivce(getInstance().getCommentService());
        baseController.setTagService(getInstance().getTagService());
        baseController.setReviewService(getInstance().getReviewService());
    }

    return parent;
}

    /**
     * Launches the JavaFX application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
