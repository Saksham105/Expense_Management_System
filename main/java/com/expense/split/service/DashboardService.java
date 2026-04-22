package com.expense.split.service;

import com.expense.split.model.Expense;
import com.expense.split.model.Group;
import com.expense.split.model.SplitDetail;
import com.expense.split.model.User;
import java.util.*;

public class DashboardService {

    public static class UserBalance {
        public double totalOwe = 0;
        public double totalOwedTo = 0;
        public Map<User, Double> perPersonBalance = new HashMap<>();
    }

    public UserBalance getUserBalanceInGroup(User user, Group group) {
        UserBalance balance = new UserBalance();

        for (Expense expense : group.getExpenses()) {
            User payer = expense.getPaidTo();
            
            if (payer.equals(user)) {
                // User paid, others owe them (only count UNPAID splits)
                for (SplitDetail detail : expense.getPaidBy()) {
                    if (!detail.getUser().equals(user) && !detail.getStatus()) {
                        balance.totalOwedTo += detail.getAmount();
                        User other = detail.getUser();
                        balance.perPersonBalance.put(other, balance.perPersonBalance.getOrDefault(other, 0.0) + detail.getAmount());
                    }
                }
            } else {
                // Someone else paid, check if user is a participant (only count UNPAID splits)
                for (SplitDetail detail : expense.getPaidBy()) {
                    if (detail.getUser().equals(user) && !detail.getStatus()) {
                        balance.totalOwe += detail.getAmount();
                        balance.perPersonBalance.put(payer, balance.perPersonBalance.getOrDefault(payer, 0.0) - detail.getAmount());
                    }
                }
            }
        }

        return balance;
    }
}
