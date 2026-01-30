package com.blogging_platform.classes;

/**
 * Interface for FXML controllers that accept a parameter when navigated to
 * (e.g. post id for SinglePostView or ReviewPage).
 */
public interface ParameterReceiver {

    /**
     * Receives the parameter passed by {@link App#setRoot(String, Object)}.
     * Typically called with a String (e.g. post id) after the controller is loaded.
     *
     * @param parameter the navigation parameter (often a post id)
     */
    void receiveParameter(Object parameter);
}
