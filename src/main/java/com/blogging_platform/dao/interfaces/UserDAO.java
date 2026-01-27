package com.blogging_platform.dao.interfaces;

import com.blogging_platform.classes.UserRecord;
import com.blogging_platform.model.User;

public interface UserDAO {

    boolean existsByEmail(String email);

    void register(User user);

    UserRecord login(String email, String password);

    void logout();

    
}
