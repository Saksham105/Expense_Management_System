package com.expense.split.strategy;

import com.expense.split.exception.InvalidUserException;
import com.expense.split.exception.PercentageNot100Exception;
import com.expense.split.model.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PercentageSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, Double> splitExpense(
        double totalAmount,
        List<User> participants,
        Map<User, Double> inputData
    ) throws InvalidUserException, PercentageNot100Exception {
        
        if(participants == null || participants.isEmpty())
            throw new InvalidUserException("participant list cannot be empty.!");

        double totalPercentage = 0.0;

        for(double percent : inputData.values())
            totalPercentage += percent;

        if(totalPercentage != 100)
            throw new PercentageNot100Exception("total percentage must be 100.!");

        Map<User, Double> result = new HashMap<>();

        for(User user : participants) {
            double percent = inputData.get(user);

            double share = totalAmount * (percent / 100);

            result.put(user, share);
        }

        return result;
    }
}