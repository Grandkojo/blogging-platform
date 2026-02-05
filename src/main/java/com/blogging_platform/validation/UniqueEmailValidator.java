package com.blogging_platform.validation;

import org.springframework.beans.factory.annotation.Autowired;

import com.blogging_platform.dao.interfaces.implementation.JdbcUserDAO;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    @Autowired
    private JdbcUserDAO jdbcUserDAO;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null) return true;
        return !jdbcUserDAO.existsByEmail(email);
    }

}
