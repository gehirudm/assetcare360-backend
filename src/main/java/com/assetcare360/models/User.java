package com.assetcare360.models;

import com.assetcare360.config.DB;
import com.assetcare360.interfaces.Model;

import java.sql.*;
import java.util.*;

public class User implements Model<User> {
    private Integer id;
    private String username;
    private String email;
    private String password;
    private String role;
    
    public User() {
    }
    
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
    
    // Getters and setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Model interface implementation
    @Override
    public boolean save() throws SQLException {
        if (id == null) {
            // Insert new record
            String query = "INSERT INTO " + getTableName() + 
                          " (username, email, password, role) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, password);
                stmt.setString(4, role);
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    return false;
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getInt(1);
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        } else {
            // Update existing record
            return update();
        }
    }
    
    @Override
    public Optional<User> findById(Object id) throws SQLException {
        String query = "SELECT * FROM " + getTableName() + " WHERE " + getPrimaryKeyName() + " = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setObject(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(fromResultSet(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<User> findAll() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM " + getTableName();
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(fromResultSet(rs));
            }
        }
        
        return users;
    }
    
    @Override
    public List<User> findWhere(Map<String, Object> criteria) throws SQLException {
        List<User> users = new ArrayList<>();
        
        if (criteria.isEmpty()) {
            return findAll();
        }
        
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM " + getTableName() + " WHERE ");
        List<Object> params = new ArrayList<>();
        
        int i = 0;
        for (Map.Entry<String, Object> entry : criteria.entrySet()) {
            if (i > 0) {
                queryBuilder.append(" AND ");
            }
            queryBuilder.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
            i++;
        }
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
            
            for (int j = 0; j < params.size(); j++) {
                stmt.setObject(j + 1, params.get(j));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(fromResultSet(rs));
                }
            }
        }
        
        return users;
    }
    
    @Override
    public boolean update() throws SQLException {
        if (id == null) {
            return false;
        }
        
        String query = "UPDATE " + getTableName() + 
                      " SET username = ?, email = ?, password = ?, role = ? WHERE " + 
                      getPrimaryKeyName() + " = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, role);
            stmt.setInt(5, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public boolean delete() throws SQLException {
        if (id == null) {
            return false;
        }
        
        String query = "DELETE FROM " + getTableName() + " WHERE " + getPrimaryKeyName() + " = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        }
    }
    
    @Override
    public String getTableName() {
        return "users";
    }
    
    @Override
    public String getPrimaryKeyName() {
        return "id";
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return DB.connect();
    }
    
    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("username", username);
        map.put("email", email);
        map.put("password", password);
        map.put("role", role);
        return map;
    }
    
    @Override
    public User fromMap(Map<String, Object> values) {
        User user = new User();
        
        if (values.containsKey("id")) {
            user.setId((Integer) values.get("id"));
        }
        
        if (values.containsKey("username")) {
            user.setUsername((String) values.get("username"));
        }
        
        if (values.containsKey("email")) {
            user.setEmail((String) values.get("email"));
        }
        
        if (values.containsKey("password")) {
            user.setPassword((String) values.get("password"));
        }
        
        if (values.containsKey("role")) {
            user.setRole((String) values.get("role"));
        }
        
        return user;
    }
    
    private User fromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }
}