package com.expense.split.strategy;

import com.expense.split.exception.InvalidUserException;
import com.expense.split.exception.PercentageNot100Exception;
import com.expense.split.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemLevelSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, Double> splitExpense(
        double totalAmount,
        List<User> participants,
        Map<User, Double> inputData
    ) throws InvalidUserException, PercentageNot100Exception {
        
        if (participants == null || participants.isEmpty())
            throw new InvalidUserException("participant list cannot be empty.!");

        double sumOfItems = 0.0;
        for (double amount : inputData.values())
            sumOfItems += amount;

        // Allowing a small delta for floating point errors
        if (Math.abs(sumOfItems - totalAmount) > 0.01)
            throw new PercentageNot100Exception("sum of item amounts must equal total expense amount.!");

        Map<User, Double> result = new HashMap<>();

        for (User user : participants) {
            result.put(user, inputData.getOrDefault(user, 0.0));
        }

        return result;
    }
}
