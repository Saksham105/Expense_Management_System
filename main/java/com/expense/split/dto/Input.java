package com.expense.split.dto;

import com.expense.split.design.Color;
import com.expense.split.exception.*;
import com.expense.split.model.SplitType;
import java.util.Scanner;

public final class Input {
    private static final Scanner sc = new Scanner(System.in);

    private Input() {} // prevents object creation, uses only static methods

    public static String name(String msg) throws IllegalNameException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        String user_name = sc.nextLine().trim();
        System.out.print(Color.RESET);

        // matches with valid name constraints
        if(!user_name.matches("^[a-zA-Z\\s]+$"))
            throw new IllegalNameException("invalid name.!");

        return user_name;
    }

    public static String email(String msg) throws IllegalNameException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        String user_email = sc.nextLine().trim();
        System.out.print(Color.RESET);

        // matches with valid name constraints
        if(!user_email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
            throw new IllegalNameException("invalid email.!");

        return user_email;
    }

    public static String password(String msg) throws IllegalNameException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        String user_password = sc.nextLine().trim();
        System.out.print(Color.RESET);

        // matches with valid name constraints
        if(!user_password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$"))
            throw new IllegalNameException(
                "invalid password:\nPassword must contain:\n\t1. at least one Uppercase letter.\n\t2. at least one lowercase letter.\n\t3. at least one apecial character.\n\t4. at least 8 characters long"
            );

        return user_password;
    }

    public static String description(String msg) {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        String expense_description = sc.nextLine().trim();
        System.out.print(Color.RESET);

        return expense_description;
    }

    public static double amount(String msg) throws IllegalAmountException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        double expense_amount = Double.parseDouble(sc.nextLine().trim());
        System.out.print(Color.RESET);

        if(expense_amount <= 0)
            throw new IllegalAmountException("amount must be positive value.!");

        return expense_amount;
    }

    public static long id(String msg) throws InvalidUserException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        long user_id = Long.parseLong(sc.nextLine().trim());
        System.out.print(Color.RESET);

        if(user_id < 0)
            throw new InvalidUserException("user id must be positive.!");

        return user_id;
    }

    public static SplitType splitType(String msg) throws InvalidSplitException {
        System.out.print(Color.YELLOW + msg + Color.CYAN); // prints msg for user
        String split_type = sc.nextLine().trim().toUpperCase();
        System.out.print(Color.RESET);

        try {
            return SplitType.valueOf(split_type);

        } catch (IllegalArgumentException e) {
            throw new InvalidSplitException("invalid split type.!");
        }
    }
}