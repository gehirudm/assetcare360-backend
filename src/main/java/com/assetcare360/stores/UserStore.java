package com.assetcare360.stores;

import com.assetcare360.models.User;
import com.assetcare360.system.FactoryManager;
import com.assetcare360.system.MigrationManager;
import com.assetcare360.system.SeederManager;
import com.assetcare360.factories.UserFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserStore {
    private final String TABLE_NAME = "users";
    private final MigrationManager migrationManager;
    private final SeederManager seederManager;
    private final User userModel;
    
    public UserStore() {
        this.migrationManager = new MigrationManager(TABLE_NAME);
        this.seederManager = new SeederManager(TABLE_NAME);
        this.userModel = new User();
        
        try {
            // Run migrations on initialization
            migrationManager.migrate();
        } catch (SQLException e) {
            System.err.println("Failed to run migrations for " + TABLE_NAME + ": " + e.getMessage());
        }
        
        // Ensure UserFactory is registered
        new UserFactory();
    }
    
    public void seed() throws SQLException {
        seederManager.seed();
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userModel.findAll();
    }
    
    public Optional<User> getUserById(int id) throws SQLException {
        return userModel.findById(id);
    }
    
    public List<User> findUsersByRole(String role) throws SQLException {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("role", role);
        return userModel.findWhere(criteria);
    }
    
    public User createUser(User user) throws SQLException {
        user.save();
        return user;
    }
    
    public boolean updateUser(User user) throws SQLException {
        return user.update();
    }
    
    public boolean deleteUser(int id) throws SQLException {
        Optional<User> userOpt = getUserById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.delete();
        }
        return false;
    }
    
    /**
     * Create test users using the factory
     * @param count Number of test users to create
     * @return List of created users
     * @throws SQLException if database error occurs
     */
    public List<User> createTestUsers(int count) throws SQLException {
        List<User> users = FactoryManager.getFactory(User.class).createMany(count);
        for (User user : users) {
            user.save();
        }
        return users;
    }
    
    /**
     * Create a test user with specific attributes
     * @param attributes Key-value pairs of attributes (e.g., "username", "testuser", "role", "admin")
     * @return The created user
     * @throws SQLException if database error occurs
     */
    public User createTestUser(Object... attributes) throws SQLException {
        User user = FactoryManager.getFactory(User.class).create(attributes);
        user.save();
        return user;
    }
}