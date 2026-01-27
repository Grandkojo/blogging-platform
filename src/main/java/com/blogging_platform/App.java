package com.blogging_platform;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

import com.blogging_platform.classes.ParameterReceiver;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.dao.interfaces.implementation.JdbcUserDAO;
import com.blogging_platform.service.UserService;

/**
 * JavaFX App
 */
public class App extends Application {

    private static App instance;
    private static Scene scene;

    //services
    private UserService userService;


    public UserService getUserService() { return userService; }

    public App() { instance = this; }

    public static App getInstance() { return instance; }

    
    
    @Override
    public void start(Stage stage) throws IOException {

        //initialize the daos
        UserDAO userDAO = new JdbcUserDAO();

        //initialize services
        this.userService = new UserService(userDAO);

        scene = new Scene(loadFXML("Login"));
        stage.setResizable(true);
        stage.setFullScreen(true);  
        stage.setTitle("Blogging Platform");
        stage.setScene(scene);
        stage.show();
    }

    static void setRoot(String fxml, Object parameter) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        Parent root = loader.load();

        // Inject App instance into controller
        Object controller = loader.getController();
        if (controller instanceof BaseController baseController) {
            baseController.setApp(App.getInstance());
            baseController.setUserService(getInstance().getUserService()); 

        }

        if (controller instanceof ParameterReceiver receiver){
            receiver.receiveParameter(parameter);
        }
        
        scene.setRoot(root);
    }

    static void setRoot(String fxml) throws IOException {
        setRoot(fxml, null);
    }

    private static Parent loadFXML(String fxml) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
    Parent parent = fxmlLoader.load();

    // Inject the App instance into the controller if it extends BaseController
    Object controller = fxmlLoader.getController();
    if (controller instanceof BaseController baseController) {
        baseController.setApp(getInstance()); 

        // Pass the services from the App instance to the Controller
        baseController.setUserService(getInstance().getUserService());
    }

    return parent;
}

    public static void main(String[] args) {
        launch();
    }

}