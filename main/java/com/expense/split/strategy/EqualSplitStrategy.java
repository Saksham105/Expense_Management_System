package com.expense.split.strategy;

import com.expense.split.exception.InvalidUserException;
import com.expense.split.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, Double> splitExpense(
        double totalAmount,
        List<User> participants,
        Map<User, Double> inputData // not used in equal split strategy
    ) throws InvalidUserException {
        
        if(participants == null || participants.isEmpty())
            throw new InvalidUserException("participant list cannot be empty.!");

        double share = (double) (totalAmount / participants.size());

        Map<User, Double> result = new HashMap<>();

        for(User user : participants)
            result.put(user, share);

        return result;
    }
}