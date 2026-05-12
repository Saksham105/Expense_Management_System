package com.expense.split;

import com.expense.split.design.Color;
import com.expense.split.gui.ExpenseSplitterGUI;
import com.expense.split.repository.ExpenseRepository;
import com.expense.split.repository.GroupRepository;
import com.expense.split.repository.UserRepository;

public class ExpenseSplitterApplication {
    public static void main(String[] args) {
        try {
            System.out.println(Color.BLUE + "STARTING APPLICATION..." + Color.RESET);
            UserRepository.download();
            GroupRepository.download();
            ExpenseRepository.download();
            
            ExpenseSplitterGUI.launch();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}