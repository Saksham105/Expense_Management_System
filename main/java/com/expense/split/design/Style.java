package com.expense.split.design;

public enum Style {
    RESET("\u001B[0m"),
    
    BOLD("\u001B[1m"),
    DIM("\u001B[2m"),
    ITALIC("\u001B[3m"),
    UNDERLINE("\u001B[4m"),
    BLINK("\u001B[5m"),
    REVERSE("\u001B[7m"),
    HIDDEN("\u001B[8m");

    private final String code;
    Style(String code) {
        this.code = code;
    }

    public String get() {
        return code;
    }

    @Override
    public String toString() {
        return code;
    }
}