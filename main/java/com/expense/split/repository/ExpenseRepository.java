package com.expense.split.repository;

import com.expense.split.exception.*;
import com.expense.split.model.Expense;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ExpenseRepository {
    private static final List<Expense> expenseList = new CopyOnWriteArrayList<>();

    // Getter
    public static List<Expense> getExpenses() { return Collections.unmodifiableList(expenseList); }

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