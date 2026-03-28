package com.expense.split.exception;

public class InvalidBalanceException extends Exception {
    public InvalidBalanceException(String msg) {
        super(msg);
    }
}