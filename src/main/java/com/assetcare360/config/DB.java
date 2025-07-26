package com.assetcare360.config;
import java.sql.*;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/?user=root";

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}