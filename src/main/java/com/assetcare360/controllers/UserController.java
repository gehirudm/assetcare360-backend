package com.assetcare360.controllers;

import com.assetcare360.interfaces.Controller;
import com.assetcare360.models.User;
import com.assetcare360.stores.UserStore;
import com.assetcare360.system.Router;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserController implements Controller {
    private final UserStore userStore = new UserStore();
    
    @Override
    public void registerRoutes(Router router, String basePath) {
        router.register(basePath, "GET", this::getAllUsers);
        router.register(basePath + "/{id}", "GET", this::getUserById);
        router.register(basePath, "POST", this::createUser);
        router.register(basePath + "/{id}", "PUT", this::updateUser);
        router.register(basePath + "/{id}", "DELETE", this::deleteUser);
    }
    
    private void getAllUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> users = userStore.getAllUsers();
            String response = convertUsersToJson(users);
            sendJsonResponse(exchange, 200, response);
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }
    
    private void getUserById(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/users/(\\d+)");
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            
            try {
                Optional<User> userOpt = userStore.getUserById(id);
                
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    String response = convertUserToJson(user);
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"User not found\"}");
                }
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid user ID\"}");
        }
    }
    
    private void createUser(HttpExchange exchange) throws IOException {
        String requestBody = readRequestBody(exchange);
        User user = parseUserFromJson(requestBody);
        
        if (user != null) {
            try {
                User createdUser = userStore.createUser(user);
                String response = convertUserToJson(createdUser);
                sendJsonResponse(exchange, 201, response);
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid user data\"}");
        }
    }
    
    private void updateUser(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/users/(\\d+)");
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            String requestBody = readRequestBody(exchange);
            User user = parseUserFromJson(requestBody);
            
            if (user != null) {
                user.setId(id);
                
                try {
                    boolean updated = userStore.updateUser(user);
                    
                    if (updated) {
                        sendJsonResponse(exchange, 200, "{\"message\":\"User updated successfully\"}");
                    } else {
                        sendJsonResponse(exchange, 404, "{\"error\":\"User not found\"}");
                    }
                } catch (SQLException e) {
                    sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
                }
            } else {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid user data\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid user ID\"}");
        }
    }
    
    private void deleteUser(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/users/(\\d+)");
        Matcher matcher = pattern.matcher(path);
        
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(1));
            
            try {
                boolean deleted = userStore.deleteUser(id);
                
                if (deleted) {
                    sendJsonResponse(exchange, 200, "{\"message\":\"User deleted successfully\"}");
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"User not found\"}");
                }
            } catch (SQLException e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid user ID\"}");
        }
    }
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            return br.lines().collect(Collectors.joining());
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
    
    // Simple JSON conversion methods - in a real app, use a proper JSON library
    private String convertUsersToJson(List<User> users) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(convertUserToJson(users.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }
    
    private String convertUserToJson(User user) {
        return "{" +
               "\"id\":" + user.getId() + "," +
               "\"username\":\"" + user.getUsername() + "\"," +
               "\"email\":\"" + user.getEmail() + "\"," +
               "\"role\":\"" + user.getRole() + "\"" +
               "}";
    }
    
    private User parseUserFromJson(String json) {
        // This is a very simple JSON parser - in a real app, use a proper JSON library
        try {
            User user = new User();
            
            // Extract username
            Pattern usernamePattern = Pattern.compile("\"username\"\\s*:\\s*\"([^\"]+)\"");
            Matcher usernameMatcher = usernamePattern.matcher(json);
            if (usernameMatcher.find()) {
                user.setUsername(usernameMatcher.group(1));
            }
            
            // Extract email
            Pattern emailPattern = Pattern.compile("\"email\"\\s*:\\s*\"([^\"]+)\"");
            Matcher emailMatcher = emailPattern.matcher(json);
            if (emailMatcher.find()) {
                user.setEmail(emailMatcher.group(1));
            }
            
            // Extract password
            Pattern passwordPattern = Pattern.compile("\"password\"\\s*:\\s*\"([^\"]+)\"");
            Matcher passwordMatcher = passwordPattern.matcher(json);
            if (passwordMatcher.find()) {
                user.setPassword(passwordMatcher.group(1));
            }
            
            // Extract role
            Pattern rolePattern = Pattern.compile("\"role\"\\s*:\\s*\"([^\"]+)\"");
            Matcher roleMatcher = rolePattern.matcher(json);
                        if (roleMatcher.find()) {
                user.setRole(roleMatcher.group(1));
            }
            
            // Validate required fields
            if (user.getUsername() == null || user.getEmail() == null || 
                user.getPassword() == null || user.getRole() == null) {
                return null;
            }
            
            return user;
        } catch (Exception e) {
            return null;
        }
    }
}