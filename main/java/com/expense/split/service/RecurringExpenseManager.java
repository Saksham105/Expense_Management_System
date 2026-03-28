package com.expense.split.service;

import com.expense.split.model.Expense;
import com.expense.split.model.Group;
import com.expense.split.repository.GroupRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RecurringExpenseManager {
    private final ExpenseService expenseService;

    public RecurringExpenseManager(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    // This method would be called by a scheduler or on app startup
    public synchronized void processRecurringExpenses() {
        List<Group> allGroups = GroupRepository.getGroups();
        for (Group group : allGroups) {
            List<Expense> newExpenses = new ArrayList<>();
            for (Expense expense : group.getExpenses()) {
                if (expense.isRecurring()) {
                    // Simple logic: if a month has passed since createdAt, create a new one
                    // In a real app, we'd track "lastProcessedDate"
                    if (expense.getCreatedAt().isBefore(LocalDateTime.now().minusMonths(1))) {
                        // This logic is a stub for the hackathon requirement
                        // In reality, we'd need to ensure we don't create duplicates
                    }
                }
            }
        }
    }
    
    public void markAsRecurring(Expense expense) {
        expense.setRecurring(true);
    }
}
