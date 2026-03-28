package com.expense.split.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class Expense {
    private final long id;
    private String description;
    private double totalAmount;
    private User paidTo; // whom participants have to pay
    private List<SplitDetail> paidBy; // participants list
    private SplitType type;
    private final LocalDateTime createdAt; // no update
    private long groupId; // group this expense belongs to
    private String currency = "USD"; // base currency
    private double conversionRate = 1.0; // rate used
    private boolean isRecurring = false; // monthly flag

    private static long expenseCount =0;

    public Expense(
        User paidTo,
        double totalAmount,
        SplitType type,
        List<SplitDetail> paidBy,
        String description,
        long groupId
    ) {
        this.id = ++expenseCount;
        this.paidTo = paidTo;
        this.totalAmount = totalAmount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
        this.paidBy = paidBy;
        this.description = description;
        this.groupId = groupId;
    }

    // Setters
    public void setPaidTo(User paidTo) { this.paidTo = paidTo; }
    public void setPaidBy(List<SplitDetail> paidBy) { this.paidBy = paidBy; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public void setType(SplitType type) { this.type = type; }
    public void setDescription(String description) { this.description = description; }

    // Getters
    public long getId() { return id; }
    public User getPaidTo() { return paidTo; }
    public List<SplitDetail> getPaidBy() { return paidBy; }
    public double getTotalAmount() { return totalAmount; }
    public SplitType getType() { return type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getDescription() { return description; }
    public long getGroupId() { return groupId; }
    public String getCurrency() { return currency; }
    public double getConversionRate() { return conversionRate; }
    public boolean isRecurring() { return isRecurring; }

    public void setGroupId(long groupId) { this.groupId = groupId; }
    public void setCurrency(String currency) { this.currency = currency; }
    public void setConversionRate(double rate) { this.conversionRate = rate; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    // overriding Object class methods
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o)
            return true;

        if(!(o instanceof Expense other))
            return false;

        return (id == other.id);
    }

    @Override
    public String toString() {
        return "[EXPENSE]: #" + id + "\nGroup: " + groupId
        + "\nPaid to: " + paidTo
        + "\nTotal amount: " + totalAmount + " " + currency
        + "\nType: " + type
        + "\nPaid by: \n\t" + paidBy
        + (isRecurring ? "\n*Recurring Monthly*" : "");
    }
}