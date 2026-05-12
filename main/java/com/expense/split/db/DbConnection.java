package com.expense.split.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/expense_splitter";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Pass@1234";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }
}