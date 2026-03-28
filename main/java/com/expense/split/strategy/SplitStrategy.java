package com.expense.split.strategy;

import com.expense.split.exception.InvalidUserException;
import com.expense.split.exception.PercentageNot100Exception;
import com.expense.split.model.User;
import java.util.List;
import java.util.Map;

public interface SplitStrategy {
    Map<User, Double> splitExpense(
        double totalAmount,
        List<User> participants,
        Map<User, Double> inputData // not used in equal split strategy
    ) throws InvalidUserException, PercentageNot100Exception ;
}