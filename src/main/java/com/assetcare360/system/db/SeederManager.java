package com.assetcare360.system.db;

import com.assetcare360.config.DB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

public class SeederManager {
    private final String tableName;
    private final String seedersTableName = "seeders";

    public SeederManager(String tableName) {
        this.tableName = tableName;
    }

    public void seed() throws SQLException {
        ensureSeedersTableExists();

        if (!isSeeded()) {
            runSeeder();
        }
    }

    private void ensureSeedersTableExists() throws SQLException {
        try (Connection conn = DB.connect();
                Statement stmt = conn.createStatement()) {

            String createTableSQL = "CREATE TABLE IF NOT EXISTS " + seedersTableName + " (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "table_name VARCHAR(255) NOT NULL, " +
                    "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY unique_table (table_name)" +
                    ")";

            stmt.executeUpdate(createTableSQL);
        }
    }

    private boolean isSeeded() throws SQLException {
        String query = "SELECT 1 FROM " + seedersTableName + " WHERE table_name = ?";

        try (Connection conn = DB.connect();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void runSeeder() throws SQLException {
        String resourceName = "seeders/" + tableName + "_seeder.sql";
        String sql = loadResourceAsString(resourceName);

        if (sql == null || sql.trim().isEmpty()) {
            System.out.println("Seeder file not found or empty: " + resourceName);
            return;
        }

        try (Connection conn = DB.connect()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()) {
                // Execute the seeder SQL
                stmt.executeUpdate(sql);

                // Record the seeder execution
                String insertQuery = "INSERT INTO " + seedersTableName +
                        " (table_name) VALUES (?)";

                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, tableName);
                    pstmt.executeUpdate();
                }

                conn.commit();
                System.out.println("Successfully seeded " + tableName);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private String loadResourceAsString(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            System.err.println("Error loading resource: " + e.getMessage());
            return null;
        }
    }
}
