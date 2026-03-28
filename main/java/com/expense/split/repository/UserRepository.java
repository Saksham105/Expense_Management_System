package com.expense.split.repository;

import com.expense.split.exception.*;
import com.expense.split.model.User;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserRepository {
    private static final List<User> userList = new CopyOnWriteArrayList<>();

    // Getter
    public static List<User> getUsers() { return Collections.unmodifiableList(userList); }

    // save method
    public User save(User user) throws InvalidUserException {
        if(user == null)
            throw new InvalidUserException("user cannot be null.!");

        if(userList.contains(user))
            throw new InvalidUserException("duplicate user not allowed.!");

        return (userList.add(user) ? user : null);
    }

    // update methods
    public User updateName(User user, String name) throws InvalidUserException {
        if(!userList.contains(user))
            throw new InvalidUserException("user not found in repository.!");

        user.setName(name);
        return user;
    }

    public User updateEmail(User user, String email) throws InvalidUserException {
        if(!userList.contains(user))
            throw new InvalidUserException("user not found in repository.!");

        user.setEmail(email);
        return user;
    }

    public User updatePassword(User user, String password) throws InvalidUserException {
        if(!userList.contains(user))
            throw new InvalidUserException("user not found in repository.!");

        user.setPassword(password);
        return user;
    }

    // get methods
    public User getUserById(long id) {
        for(User current : userList) 
            if(current.getId() == id)
                return current;
        return null;
    }

    public User getUserByName(String name) {
        for(User current : userList)
            if(name != null && current.getName().equals(name))
                return current;
        return null;
    }

    public User getUserByEmail(String email) {
        for(User current : userList)
            if(email != null && current.getEmail().equals(email))
                return current;
        return null;
    }

    // delete method
    public boolean delete(User user) throws InvalidUserException {
        if(user == null)
            throw new InvalidUserException("user cannot be null.!");

        return userList.remove(user);
    }
}