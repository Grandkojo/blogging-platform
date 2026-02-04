
package com.blogging_platform.service;


import java.util.List;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.model.User;

/**
 * Application service for user registration and authentication. Hashes passwords
 * and delegates persistence to {@link UserDAO}; updates {@link SessionManager} on login.
 */
@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    /** Creates a user service with the given DAO. */
    // public UserService(UserDAO userDAO) {
    //     this.userDAO = userDAO;
    // }
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Registers a new user. Password is hashed before storage.
     *
     * @param user the user (name, email, password, role)
     * @throws DuplicateEmailException if the email is already registered
     */
    public void registerUser(User user) throws DuplicateEmailException {
         if(userDAO.existsByEmail(user.getEmail())){
            throw new DuplicateEmailException("An account with this email already exists");
        }

        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        userDAO.register(user);
    }

    /**
     * Authenticates the user and sets the session on success.
     *
     * @param email    user email
     * @param password plain-text password
     * @return true if login succeeded
     * @throws AuthenticationException if credentials are invalid
     */
    public UserRecord loginUser(String email, String password) throws AuthenticationException {

        UserRecord user = userDAO.login(email, password);
        if (user == null){
            throw new AuthenticationException("Invalid email or password");
        } else {
            SessionManager.getInstance().login(user);
            return user;
        }
    }

    public boolean existsById(String userId){
        return userDAO.existsById(userId);
    }

    public List<UserRecord> getUsers(){
        return userDAO.findAll();
    }
}
