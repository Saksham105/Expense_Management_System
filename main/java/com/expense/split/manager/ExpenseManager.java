package com.expense.split.manager;

import com.expense.split.design.Color;
import com.expense.split.model.Expense;
import com.expense.split.model.SplitType;
import com.expense.split.model.User;
import com.expense.split.repository.ExpenseRepository;
import com.expense.split.service.ExpenseService;
import java.util.List;
import java.util.Map;

public class ExpenseManager {
    private final ExpenseService expenseService;

    public ExpenseManager() {
        this.expenseService = new ExpenseService();
    }

    public synchronized boolean createExpense(
            double totalAmount,
            User paidTo,
            List<User> participants,
            SplitType splitType,
            Map<User, Double> inputData,
            String description,
            long groupId
    ) {
        try {
            return expenseService.createExpense(totalAmount, paidTo, participants, splitType, inputData, description, groupId);
        } catch (Exception e) {
            System.err.println(Color.RED + "[UNKNOWN ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized Expense findExpenseById(long id) {
        return expenseService.getExpenseById(id);
    }

    public synchronized boolean deleteExpense(Expense expense) {
        return expenseService.deleteExpense(expense);
    }

    public synchronized List<Expense> listAllExpenses() {
        return ExpenseRepository.getExpenses();
    }
}
