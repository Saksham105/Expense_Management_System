package com.expense.split.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final long id;
    private String name;
    private String email;
    private String password;
    private final List<SplitDetail> splitHistory;

    private static long userCount =0;

    public User(String name, String email, String password) {
        this.id = ++userCount;
        this.name = name;
        this.email = email;
        this.password = password;
        this.splitHistory = new ArrayList<>();
    }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }

    // Getters
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public static long getUserCount() { return userCount; }
    public List<SplitDetail> getSplitHistory() { return splitHistory; }

    // overriding Object class methods
    @Override
    public int hashCode() {
        return Objects.hash(id, email, password);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;

        if(!(o instanceof User other))
            return false;

        return (id == other.id) && Objects.equals(email, other.email) && Objects.equals(password, other.password);
    }

    @Override
    public String toString() {
        return "[USER]: #" + id + ", name: " + name + ", email: " + email + "\nOwed Expenses:\n\t" + splitHistory;
    }
}