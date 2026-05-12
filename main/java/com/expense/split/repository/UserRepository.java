package com.expense.split.repository;

import com.expense.split.db.DbConnection;
import com.expense.split.exception.*;
import com.expense.split.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class UserRepository {
    private static final List<User> userList = new CopyOnWriteArrayList<>();

    // public UserRepository() {
    //     userList.add(new User("saksham", "saksham@gmail.com", "pass1234"));
    //     userList.add(new User("kirti", "k@gmail.com", "k1234"));
    //     userList.add(new User("rupesh", "r@gmail.com", "r1234"));
    //     userList.add(new User("amruta", "a@gmail.com", "a1234"));
    // }

    // save list (users) in the database.
    public static void upload() throws SQLException, InvalidUserException {
        if (userList.isEmpty()) {
            throw new InvalidUserException("user list is empty. nothing to upload.");
        }

        String query = """
                INSERT INTO users (user_id, name, email, password)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    name = VALUES(name),
                    email = VALUES(email),
                    password = VALUES(password)
                """;

        try (
            Connection connection = DbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(query)
        ) {
            for (User user : userList) {
                ps.setLong(1, user.getId());
                ps.setString(2, user.getName());
                ps.setString(3, user.getEmail());
                ps.setString(4, user.getPassword());

                ps.addBatch();
            }

            ps.executeBatch();

            System.out.println("all users uploaded successfully.");
        }
    }

    // get all entries from database and form a list of users
    public static void download() throws SQLException {
        userList.clear();
        String query = """
                SELECT user_id, name, email, password
                FROM users
                """;
        long maxId = 0;

        try (
                Connection connection = DbConnection.getConnection();
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                long id = rs.getLong("user_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");

                User user = new User(id, name, email, password);
                userList.add(user);

                if (id > maxId) {
                    maxId = id;
                }
            }
        }
        User.setUserCount(maxId);

        System.out.println("all users downloaded successfully.");
    }

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
        if (email == null) {
            return null;
        }
        String needle = email.trim();
        if (needle.isEmpty()) {
            return null;
        }
        for (User current : userList) {
            if (current.getEmail() != null && current.getEmail().equalsIgnoreCase(needle)) {
                return current;
            }
        }
        return null;
    }

    // delete method
    public boolean delete(User user) throws InvalidUserException {
        if(user == null)
            throw new InvalidUserException("user cannot be null.!");

        return userList.remove(user);
    }
}