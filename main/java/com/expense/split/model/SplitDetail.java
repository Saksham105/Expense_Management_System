package com.expense.split.model;

public class SplitDetail {
    private User user;
    private double amountOwed;
    private boolean status;

    public SplitDetail(User user, double amount) {
        this.user = user;
        this.amountOwed = amount;
        this.status = false;
    }

    // Setters
    public void setUser(User user) { this.user = user; }
    public void setAmount(double amount) { this.amountOwed = amount; }
    public void setStatus(boolean status) { this.status = status; }

    // Getters
    public User getUser() { return user; }
    public double getAmount() { return amountOwed; }
    public boolean getStatus() { return status; }

    @Override
    public String toString() {
        return "[" + 
            (user != null ? user.getName() : "UNKNOWN") +
            ", " + amountOwed +
            ", " + (status ? "PAID" : "UNPAID") +
            "]";
    }
}