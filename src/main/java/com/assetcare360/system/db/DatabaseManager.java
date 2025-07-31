package com.assetcare360.system.db;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for managing database operations from the command line
 */
public class DatabaseManager {
    private static final List<String> TABLES = Arrays.asList(
        "users"
        // Add more tables as they are created
    );
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            return;
        }
        
        String command = args[0].toLowerCase();
        
        try {
            switch (command) {
                case "migrate":
                    runMigrations();
                    break;
                case "seed":
                    runSeeders();
                    break;
                case "refresh":
                    // This would drop tables and re-run migrations
                    System.out.println("Refresh not implemented yet");
                    break;
                default:
                    printUsage();
                    break;
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void runMigrations() throws SQLException {
        System.out.println("Running migrations...");
        
        for (String table : TABLES) {
            System.out.println("Migrating table: " + table);
            MigrationManager manager = new MigrationManager(table);
            manager.migrate();
        }
        
        System.out.println("Migrations completed successfully.");
    }
    
    private static void runSeeders() throws SQLException {
        System.out.println("Running seeders...");
        
        for (String table : TABLES) {
            System.out.println("Seeding table: " + table);
            SeederManager manager = new SeederManager(table);
            manager.seed();
        }
        
        System.out.println("Seeders completed successfully.");
    }
    
    private static void printUsage() {
        System.out.println("Usage: java -cp <classpath> com.assetcare360.util.DatabaseManager <command>");
        System.out.println("Commands:");
        System.out.println("  migrate    Run all pending migrations");
        System.out.println("  seed       Run all seeders");
        System.out.println("  refresh    Drop all tables and re-run migrations (not implemented)");
    }
}