package com.assetcare360.controllers;

import com.assetcare360.models.User;
import com.assetcare360.stores.UserStore;
import com.assetcare360.system.Router;
import com.assetcare360.system.interfaces.BaseController;
import com.sun.net.httpserver.HttpExchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserController extends BaseController {
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
            String response = User.listToJson(users);
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
                    String response = user.toJson();
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
        User user = new User().fromJson(requestBody);
        if (user != null) {
            try {
                User createdUser = userStore.createUser(user);
                String response = createdUser.toJson();
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
            User user = new User().fromJson(requestBody);
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
    
    // sendJsonResponse now inherited from BaseController
    
}