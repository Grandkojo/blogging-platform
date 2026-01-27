package com.blogging_platform.service;

import org.mindrot.jbcrypt.BCrypt;

import com.blogging_platform.classes.SessionManager;
import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.dao.interfaces.UserDAO;
import com.blogging_platform.exceptions.AuthenticationException;
import com.blogging_platform.exceptions.DuplicateEmailException;
import com.blogging_platform.model.User;

public class UserService {

    private UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
    }

    public void registerUser(User user) throws DuplicateEmailException {
        if(userDAO.existsByEmail(user.getEmail())){
            throw new DuplicateEmailException("An account with this email already exists");
        }

        String hashed = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashed);
        userDAO.register(user);
    }

    public boolean loginUser(String email, String password) throws AuthenticationException{

        UserRecord user = userDAO.login(email, password);
        if (user == null){
            throw new AuthenticationException("Invalid email or password");
        } else {
            SessionManager.getInstance().login(user);
            return true;
        }
    }

    public void logout(){
        userDAO.logout();
    }
}
