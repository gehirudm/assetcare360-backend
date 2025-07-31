package com.assetcare360.config;
import java.sql.*;
import io.github.cdimascio.dotenv.Dotenv;

public class DB {
    private static final Dotenv dotenv = Dotenv.configure()
        .directory(System.getProperty("user.dir"))
        .ignoreIfMissing()
        .load();

    private static final String HOST = dotenv.get("DB_HOST", "localhost");
    private static final String PORT = dotenv.get("DB_PORT", "3306");
    private static final String DB = dotenv.get("DB_NAME", "assetcare");
    private static final String USER = dotenv.get("DB_USER", "root");
    private static final String PASS = dotenv.get("DB_PASS", "");

    private static final String URL = String.format(
        "jdbc:mysql://%s:%s/%s?user=%s&password=%s",
        HOST, PORT, DB, USER, PASS
    );

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}