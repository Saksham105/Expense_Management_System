package com.expense.split.repository;

import com.expense.split.db.DbConnection;
import com.expense.split.exception.*;
import com.expense.split.model.Expense;
import com.expense.split.model.Group;
import com.expense.split.model.SplitDetail;
import com.expense.split.model.SplitType;
import com.expense.split.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExpenseRepository {
    private static final List<Expense> expenseList = new CopyOnWriteArrayList<>();

    // save expese list in the database
    public static void upload() throws SQLException, InvalidSplitException {
        if (expenseList.isEmpty()) {
            throw new InvalidSplitException("expense list is empty. nothing to upload.");
        }

        String expenseQuery = """
                INSERT INTO expenses (
                    expense_id,
                    description,
                    total_amount,
                    paid_to,
                    split_type,
                    created_at,
                    group_id,
                    currency,
                    conversion_rate,
                    is_recurring
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    description = VALUES(description),
                    total_amount = VALUES(total_amount),
                    paid_to = VALUES(paid_to),
                    split_type = VALUES(split_type),
                    created_at = VALUES(created_at),
                    group_id = VALUES(group_id),
                    currency = VALUES(currency),
                    conversion_rate = VALUES(conversion_rate),
                    is_recurring = VALUES(is_recurring)
                """;

        String splitQuery = """
                INSERT INTO split_details (
                    expense_id,
                    user_id,
                    amount_owed,
                    payment_status
                )
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    amount_owed = VALUES(amount_owed),
                    payment_status = VALUES(payment_status)
                """;

        try (
                Connection connection = DbConnection.getConnection();
                PreparedStatement expensePS = connection.prepareStatement(expenseQuery);
                PreparedStatement splitPS = connection.prepareStatement(splitQuery)
        ) {
            for (Expense expense : expenseList) {

                expensePS.setLong(1, expense.getId());
                expensePS.setString(2, expense.getDescription());
                expensePS.setDouble(3, expense.getTotalAmount());
                expensePS.setLong(4, expense.getPaidTo().getId());
                expensePS.setString(5, expense.getType().name());
                expensePS.setTimestamp(6, Timestamp.valueOf(expense.getCreatedAt()));
                expensePS.setLong(7, expense.getGroupId());
                expensePS.setString(8, expense.getCurrency());
                expensePS.setDouble(9, expense.getConversionRate());
                expensePS.setBoolean(10, expense.isRecurring());

                expensePS.addBatch();

                for (SplitDetail split : expense.getPaidBy()) {
                    splitPS.setLong(1, expense.getId());
                    splitPS.setLong(2, split.getUser().getId());
                    splitPS.setDouble(3, split.getAmount());
                    splitPS.setBoolean(4, split.getStatus());

                    splitPS.addBatch();
                }
            }

            expensePS.executeBatch();
            splitPS.executeBatch();

            System.out.println("all expenses uploaded successfully.");
        }
    }

    // fetch all records from database and save in the list.
    public static void download() throws SQLException {
        expenseList.clear();
        clearLoadedRelationships();

        String expenseQuery = """
                SELECT * FROM expenses
                """;

        String splitQuery = """
                SELECT * FROM split_details
                WHERE expense_id = ?
                """;

        long maxId = 0;

        try (
                Connection connection = DbConnection.getConnection();
                PreparedStatement expensePS = connection.prepareStatement(expenseQuery);
                ResultSet expenseRS = expensePS.executeQuery()
        ) {
            while (expenseRS.next()) {

                long expenseId = expenseRS.getLong("expense_id");
                String description = expenseRS.getString("description");
                double totalAmount = expenseRS.getDouble("total_amount");

                long paidToId = expenseRS.getLong("paid_to");
                User paidTo = new UserRepository().getUserById(paidToId);

                SplitType type = SplitType.valueOf(
                        expenseRS.getString("split_type")
                );

                LocalDateTime createdAt = expenseRS.getTimestamp("created_at").toLocalDateTime();

                long groupId = expenseRS.getLong("group_id");

                String currency = expenseRS.getString("currency");
                double conversionRate = expenseRS.getDouble("conversion_rate");
                boolean recurring = expenseRS.getBoolean("is_recurring");

                List<SplitDetail> splitList = new ArrayList<>();

                try (
                        PreparedStatement splitPS = connection.prepareStatement(splitQuery)
                ) {
                    splitPS.setLong(1, expenseId);

                    try (ResultSet splitRS = splitPS.executeQuery()) {
                        while (splitRS.next()) {
                            long userId = splitRS.getLong("user_id");
                            User user = new UserRepository().getUserById(userId);

                            double amount = splitRS.getDouble("amount_owed");

                            boolean status = splitRS.getBoolean("payment_status");

                            SplitDetail split = new SplitDetail(user, amount);
                            split.setStatus(status);

                            splitList.add(split);

                            if (user != null && paidTo != null) {
                                SplitDetail historyEntry = new SplitDetail(paidTo, amount);
                                historyEntry.setStatus(status);
                                user.getSplitHistory().add(historyEntry);
                            }
                        }
                    }
                }

                Expense expense = new Expense(
                        expenseId,
                        paidTo,
                        totalAmount,
                        type,
                        splitList,
                        description,
                        groupId,
                        createdAt
                );

                expense.setCurrency(currency);
                expense.setConversionRate(conversionRate);
                expense.setRecurring(recurring);

                expenseList.add(expense);
                attachExpenseToGroup(expense);

                if (expenseId > maxId) {
                    maxId = expenseId;
                }
            }
        }

        Expense.setExpenseCount(maxId);

        System.out.println("all expenses downloaded successfully.");
    }

    // Getter
    public static List<Expense> getExpenses() { return Collections.unmodifiableList(expenseList); }

    private static void clearLoadedRelationships() {
        for (Group group : GroupRepository.getGroups()) {
            group.getExpenses().clear();
        }

        for (User user : UserRepository.getUsers()) {
            user.getSplitHistory().clear();
        }
    }

    private static void attachExpenseToGroup(Expense expense) {
        Group group = new GroupRepository().getGroupById(expense.getGroupId());
        if (group != null) {
            group.addExpense(expense);
        }
    }

    // save method
    public Expense save(Expense expense) throws InvalidSplitException {
        if(expense == null)
            throw new InvalidSplitException("split expense cannot be null.!");

        return (expenseList.add(expense) ? expense : null);
    }

    // get methods
    public Expense getExpense(long id) {
        for(Expense current : expenseList) 
            if(current.getId() == id)
                return current;
        return null;
    }

    // delete method
    public boolean delete(Expense user) throws InvalidSplitException {
        if(user == null)
            throw new InvalidSplitException("split expense cannot be null.!");

        return expenseList.remove(user);
    }
}
