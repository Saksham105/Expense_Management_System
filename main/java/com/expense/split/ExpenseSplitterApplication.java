package com.expense.split;

import com.expense.split.design.Color;
import com.expense.split.gui.ExpenseSplitterGUI;

public class ExpenseSplitterApplication {
    public static void main(String[] args) {
        System.out.println(Color.BLUE + "STARTING APPLICATION..." + Color.RESET);
        ExpenseSplitterGUI.launch();
    }
}