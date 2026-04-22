package com.expense.split.service;

import com.expense.split.model.Expense;
import com.expense.split.model.Group;
import com.expense.split.model.SplitDetail;
import com.expense.split.model.User;
import java.util.*;

public class DebtSimplifier {

    public static class Settlement {
        public User payer;
        public User payee;
        public double amount;

        public Settlement(User payer, User payee, double amount) {
            this.payer = payer;
            this.payee = payee;
            this.amount = amount;
        }

        @Override
        public String toString() {
            return payer.getName() + " pays " + payee.getName() + ": " + String.format("%.2f", amount);
        }
    }

    public List<Settlement> simplifyDebts(Group group) {
        Map<User, Double> balances = new HashMap<>();

        // 1. Calculate net balance for each user in the group
        for (User member : group.getMembers()) {
            balances.put(member, 0.0);
        }

        for (Expense expense : group.getExpenses()) {
            User payer = expense.getPaidTo();
            double total = expense.getTotalAmount();
            
            // Payer is "owed" the total amount minus their own share (if they are a participant)
            // But usually we just say payer get +total, and everyone else (participants) get -share
            balances.put(payer, balances.getOrDefault(payer, 0.0) + total);

            for (SplitDetail detail : expense.getPaidBy()) {
                if (detail.getStatus()) continue; // skip already-settled splits
                User participant = detail.getUser();
                double share = detail.getAmount();
                balances.put(participant, balances.getOrDefault(participant, 0.0) - share);
            }
        }

        // 2. Separate debtors and creditors
        PriorityQueue<Map.Entry<User, Double>> debtors = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
        PriorityQueue<Map.Entry<User, Double>> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.getValue(), a.getValue()));

        for (Map.Entry<User, Double> entry : balances.entrySet()) {
            if (entry.getValue() < -0.01) {
                debtors.add(entry);
            } else if (entry.getValue() > 0.01) {
                creditors.add(entry);
            }
        }

        // 3. Match debtors and creditors (Greedy Algorithm)
        List<Settlement> settlements = new ArrayList<>();
        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            Map.Entry<User, Double> debtor = debtors.poll();
            Map.Entry<User, Double> creditor = creditors.poll();

            double amount = Math.min(-debtor.getValue(), creditor.getValue());
            settlements.add(new Settlement(debtor.getKey(), creditor.getKey(), amount));

            double remainingDebtor = debtor.getValue() + amount;
            double remainingCreditor = creditor.getValue() - amount;

            if (remainingDebtor < -0.01) {
                debtor.setValue(remainingDebtor);
                debtors.add(debtor);
            }
            if (remainingCreditor > 0.01) {
                creditor.setValue(remainingCreditor);
                creditors.add(creditor);
            }
        }

        return settlements;
    }
}
