package com.expense.split.manager;

import com.expense.split.design.Color;
import com.expense.split.dto.Input;
import com.expense.split.model.User;
import com.expense.split.repository.UserRepository;
import com.expense.split.service.UserService;
import java.util.List;

public class UserManager {
    private final UserService userService;

    public UserManager() {
        this.userService = new UserService();
    }

    public synchronized User registerUser() {
        try {
            String name = Input.name("Enter name: ");
            String email = Input.email("Enter email: ");
            String password = Input.password("Enter password: ");

            User user = userService.createUser(name, email, password);
            if(userService.saveUser(user))
                return user;

        } catch (Exception e) {
            System.err.println(Color.RED + "[UNKNOWN ERROR]: " + e.getMessage() + Color.RESET);
        }

        return null;
    }

    /**
     * Registers a user from non-CLI contexts (e.g. Swing). Persists via {@link com.expense.split.service.UserService}.
     */
    public synchronized User registerUser(String name, String email, String password) {
        if (name == null || name.isBlank() || email == null || email.isBlank() || password == null) {
            return null;
        }
        String trimmedEmail = email.trim();
        if (listAllUsers().stream().anyMatch(u ->
                u.getEmail() != null && u.getEmail().equalsIgnoreCase(trimmedEmail))) {
            return null;
        }
        User user = userService.createUser(name.trim(), trimmedEmail, password);
        return userService.saveUser(user) ? user : null;
    }

    public synchronized boolean updateName(User user, String name) {
        return userService.updateUserName(user, name);
    }

    public synchronized boolean updateEmail(User user, String email) {
        return userService.updateUserEmail(user, email);
    }

    public synchronized boolean changePassword(User user, String newPassword) {
        return userService.updateUserPassword(user, newPassword);
    }

    public synchronized User findById(long id) {
        return userService.getUserById(id);
    }

    public synchronized User findByName(String name) {
        return userService.getUserByName(name);
    }

    public synchronized User findByEmail(String email) {
        return userService.getUserByEmail(email);
    }

    public synchronized List<User> listAllUsers() {
        return UserRepository.getUsers();
    }

    public synchronized boolean clearHistory(User user) {
        return userService.clearSplitHistory(user);
    }

    public synchronized boolean deleteUser(User user) {
        return userService.deleteUser(user);
    }
}
