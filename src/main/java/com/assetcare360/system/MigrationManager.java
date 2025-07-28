package com.assetcare360.system;

import com.assetcare360.config.DB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

public class MigrationManager {
    private final String tableName;
    private final String migrationsTableName = "migrations";
    
    public MigrationManager(String tableName) {
        this.tableName = tableName;
    }
    
    public void migrate() throws SQLException {
        ensureMigrationsTableExists();
        int currentVersion = getCurrentVersion();
        int latestVersion = findLatestVersion();
        
        if (currentVersion < latestVersion) {
            for (int version = currentVersion + 1; version <= latestVersion; version++) {
                runMigration(version);
            }
        }
    }
    
    private void ensureMigrationsTableExists() throws SQLException {
        try (Connection conn = DB.connect();
             Statement stmt = conn.createStatement()) {
            
            String createTableSQL = 
                "CREATE TABLE IF NOT EXISTS " + migrationsTableName + " (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "table_name VARCHAR(255) NOT NULL, " +
                "version INT NOT NULL, " +
                "executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY unique_table_version (table_name, version)" +
                ")";
            
            stmt.executeUpdate(createTableSQL);
        }
    }
    
    private int getCurrentVersion() throws SQLException {
        String query = "SELECT MAX(version) as version FROM " + migrationsTableName + 
                       " WHERE table_name = ?";
        
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, tableName);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int version = rs.getInt("version");
                    return rs.wasNull() ? 0 : version;
                }
                return 0;
            }
        }
    }
    
    private int findLatestVersion() {
        int latestVersion = 0;
        String resourcePattern = "migrations/migration_" + tableName + "_v";
        
        try {
            // This is a simplified approach. In a real application, you might want to use
            // a more robust method to scan resources, especially if packaged in a JAR
            ClassLoader classLoader = getClass().getClassLoader();
            
            // For demonstration purposes, we'll check up to version 100
            for (int i = 1; i <= 100; i++) {
                String resourceName = resourcePattern + i + ".sql";
                InputStream is = classLoader.getResourceAsStream(resourceName);
                
                if (is != null) {
                    latestVersion = i;
                    is.close();
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error finding latest migration version: " + e.getMessage());
        }
        
        return latestVersion;
    }
    
    private void runMigration(int version) throws SQLException {
        String resourceName = "migrations/migration_" + tableName + "_v" + version + ".sql";
        String sql = loadResourceAsString(resourceName);
        
        if (sql == null || sql.trim().isEmpty()) {
            System.err.println("Migration file not found or empty: " + resourceName);
            return;
        }
        
        try (Connection conn = DB.connect()) {
            conn.setAutoCommit(false);
            
            try (Statement stmt = conn.createStatement()) {
                // Execute the migration SQL
                stmt.executeUpdate(sql);
                
                // Record the migration
                String insertQuery = "INSERT INTO " + migrationsTableName + 
                                    " (table_name, version) VALUES (?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
                    pstmt.setString(1, tableName);
                    pstmt.setInt(2, version);
                    pstmt.executeUpdate();
                }
                
                conn.commit();
                System.out.println("Successfully migrated " + tableName + " to version " + version);
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