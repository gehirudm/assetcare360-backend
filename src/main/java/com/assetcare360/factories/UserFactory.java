package com.assetcare360.factories;

import com.assetcare360.models.User;
import com.assetcare360.system.FactoryManager;
import com.assetcare360.system.interfaces.Factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Factory for creating User model instances for testing
 */
public class UserFactory implements Factory<User> {
    private static final Random random = new Random();
    private static final String[] ROLES = {"admin", "user", "manager"};
    
    static {
        // Register this factory with the FactoryManager
        FactoryManager.register(User.class, new UserFactory());
    }
    
    @Override
    public User create() {
        User user = new User();
        user.setEmployeeId("EMP" + randomString(6));
        user.setUsername("user_" + randomString(8));
        user.setEmail(randomString(8) + "@example.com");
        user.setPassword("password_" + randomString(8));
        user.setRole(ROLES[random.nextInt(ROLES.length)]);
        return user;
    }
    
    @Override
    public User create(Object... attributes) {
        User user = create();
        
        // Apply custom attributes if provided
        for (int i = 0; i < attributes.length; i += 2) {
            if (i + 1 < attributes.length) {
                String key = attributes[i].toString();
                Object value = attributes[i + 1];
                
                switch (key) {
                    case "employeeId":
                        user.setEmployeeId(value.toString());
                        break;
                    case "username":
                        user.setUsername(value.toString());
                        break;
                    case "email":
                        user.setEmail(value.toString());
                        break;
                    case "password":
                        user.setPassword(value.toString());
                        break;
                    case "role":
                        user.setRole(value.toString());
                        break;
                }
            }
        }
        
        return user;
    }
    
    @Override
    public List<User> createMany(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(create());
        }
        return users;
    }
    
    @Override
    public List<User> createMany(int count, Object... attributes) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(create(attributes));
        }
        return users;
    }
    
    private String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}