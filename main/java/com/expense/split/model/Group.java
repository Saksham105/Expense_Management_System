package com.expense.split.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Group {

    private final long id;
    private String name;
    private final List<User> members; // group members list
    private final List<Expense> expenses; // group expenses list

    private static long groupCount = 0;

    public Group(String name) {
        this.id = ++groupCount;
        this.name = name;
        this.members = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<User> getMembers() {
        return members;
    }

    public List<Expense> getExpenses() {
        return expenses;
    }

    public void addMember(User user) {
        if (!members.contains(user)) {
            members.add(user);
        }
    }

    public void addExpense(Expense expense) {
        expenses.add(expense);
    }

    // overriding Object class methods
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group other)) {
            return false;
        }
        return id == other.id;
    }

    @Override
    public String toString() {
        return "[GROUP]: #" + id + ", name: " + name + ", members: " + members.size();
    }
}
