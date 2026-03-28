package com.expense.split.strategy;

import com.expense.split.exception.InvalidUserException;
import com.expense.split.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShareSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, Double> splitExpense(
        double totalAmount,
        List<User> participants,
        Map<User, Double> inputData
    ) throws InvalidUserException {
        
        if (participants == null || participants.isEmpty())
            throw new InvalidUserException("participant list cannot be empty.!");

        double totalShares = 0.0;
        for (double share : inputData.values())
            totalShares += share;

        if (totalShares == 0)
            throw new InvalidUserException("total shares cannot be zero.!");

        Map<User, Double> result = new HashMap<>();

        for (User user : participants) {
            double shares = inputData.getOrDefault(user, 0.0);
            double amount = (totalAmount * shares) / totalShares;
            result.put(user, amount);
        }

        return result;
    }
}
