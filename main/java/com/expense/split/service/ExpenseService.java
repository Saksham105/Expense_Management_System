package com.expense.split.service;

import com.expense.split.design.Color;
import com.expense.split.exception.InvalidSplitException;
import com.expense.split.exception.InvalidUserException;
import com.expense.split.exception.PercentageNot100Exception;
import com.expense.split.model.Expense;
import com.expense.split.model.SplitDetail;
import com.expense.split.model.SplitType;
import com.expense.split.model.User;
import com.expense.split.repository.ExpenseRepository;
import com.expense.split.strategy.EqualSplitStrategy;
import com.expense.split.strategy.ItemLevelSplitStrategy;
import com.expense.split.strategy.PercentageSplitStrategy;
import com.expense.split.strategy.ShareSplitStrategy;
import com.expense.split.strategy.SplitStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public ExpenseService() {
        this.expenseRepository = new ExpenseRepository();
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
            if(participants.contains(paidTo))
                throw new InvalidSplitException("participant list should not contain user who is creating split.!");

            SplitStrategy strategy;

            switch(splitType) {
                case EQUAL -> strategy = new EqualSplitStrategy();
                case PERCENTAGE -> strategy = new PercentageSplitStrategy();
                case SHARE -> strategy = new ShareSplitStrategy();
                case ITEM_LEVEL -> strategy = new ItemLevelSplitStrategy();
                default -> throw new InvalidSplitException("Invalid split type.");
            }

            Map<User, Double> splitResult = strategy.splitExpense(totalAmount, participants, inputData);

            List<SplitDetail> splitDetails = new ArrayList<>();
            for(Map.Entry<User, Double> entry : splitResult.entrySet())
                splitDetails.add(new SplitDetail(entry.getKey(), entry.getValue()));

            Expense expense = new Expense(paidTo, totalAmount, splitType, splitDetails, description, groupId);

            // updating splitHistory of each user who is in participant list
            for(User user : participants) {
                user.getSplitHistory().add(new SplitDetail(paidTo, splitResult.get(user)));
            }

            return expenseRepository.save(expense) != null;
            
        } catch (InvalidUserException | InvalidSplitException | PercentageNot100Exception e) {
            System.err.println(Color.RED + "[ERROR]: " + e.getMessage() + Color.RESET);
        }

        return false;
    }

    public synchronized Expense getExpenseById(long id) {
        return expenseRepository.getExpense(id);
    }

    public synchronized boolean deleteExpense(Expense expense) {
        try {
            return expenseRepository.delete(expense);

        } catch (InvalidSplitException e) {
            System.err.println(Color.RED + "[ERROR]: "
                    + e.getMessage() + Color.RESET);
        }

        return false;
    }
}