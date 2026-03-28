package com.expense.split.service;

import com.expense.split.design.Color;
import com.expense.split.dto.Input;
import com.expense.split.exception.IllegalNameException;
import com.expense.split.exception.InvalidUserException;
import com.expense.split.model.User;
import com.expense.split.repository.UserRepository;

public class UserService {
    private final UserRepository userRepository;

    public UserService() {
        userRepository = new UserRepository();
    }

    public User createUser(String name, String email, String password) {
        return new User(name, email, password);
    }

    public synchronized boolean saveUser(User user) {
        try {
            if(userRepository.save(user) != null)
                return true;

        } catch (InvalidUserException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized boolean updateUserName(User user, String name) {
        try {
            if(userRepository.updateName(user, name) != null)
                return true;

        } catch (InvalidUserException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized boolean updateUserEmail(User user, String email) {
        try {
            if(userRepository.updateEmail(user, email) != null)
                return true;

        } catch (InvalidUserException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized boolean updateUserPassword(User user, String password) {
        try {
            if(!UserRepository.getUsers().contains(user))
                throw new InvalidUserException("user not found.!");

            String pass = Input.password("\nEnter password: ");
            if(!user.getPassword().equals(pass))
                throw new InvalidUserException("incorrect password.!");

            userRepository.updatePassword(user, password);
            return true;

        } catch (InvalidUserException | IllegalNameException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized User getUserById(long id) {
        return userRepository.getUserById(id);
    }

    public synchronized User getUserByName(String name) {
        return userRepository.getUserByName(name);
    }

    public synchronized User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    public synchronized boolean clearSplitHistory(User user) {
        try {
            if(!UserRepository.getUsers().contains(user))
                throw new InvalidUserException("user not found.!");

            user.getSplitHistory().clear();
            return true;

        } catch (InvalidUserException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized boolean deleteUser(User user) {
        try {
            return userRepository.delete(user);

        } catch (InvalidUserException e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }
}