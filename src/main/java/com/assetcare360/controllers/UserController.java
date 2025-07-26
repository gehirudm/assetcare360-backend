package com.assetcare360.controllers;

import com.assetcare360.interfaces.Controller;
import com.assetcare360.stores.UserStore;
import com.assetcare360.system.Router;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

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
        String response = "{\"users\":[]}"; // Replace with actual implementation
        sendJsonResponse(exchange, 200, response);
    }
    
    private void getUserById(HttpExchange exchange) throws IOException {
        // Extract ID from path
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);
        
        String response = "{\"id\":\"" + id + "\",\"name\":\"User Name\"}"; // Replace with actual implementation
        sendJsonResponse(exchange, 200, response);
    }
    
    private void createUser(HttpExchange exchange) throws IOException {
        // Read request body
        String requestBody = readRequestBody(exchange);
        
        // Process the request (in a real app, parse JSON and create user)
        String response = "{\"message\":\"User created\",\"user\":" + requestBody + "}";
        sendJsonResponse(exchange, 201, response);
    }
    
    private void updateUser(HttpExchange exchange) throws IOException {
        // Extract ID from path
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);
        
        // Read request body
        String requestBody = readRequestBody(exchange);
        
        String response = "{\"message\":\"User updated\",\"id\":\"" + id + "\"}";
        sendJsonResponse(exchange, 200, response);
    }
    
    private void deleteUser(HttpExchange exchange) throws IOException {
        // Extract ID from path
        String path = exchange.getRequestURI().getPath();
        String id = path.substring(path.lastIndexOf('/') + 1);
        
        String response = "{\"message\":\"User deleted\",\"id\":\"" + id + "\"}";
        sendJsonResponse(exchange, 200, response);
    }
    
    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             Scanner scanner = new Scanner(is, "UTF-8")) {
            return scanner.useDelimiter("\\A").next();
        } catch (Exception e) {
            return "";
        }
    }
    
    private void sendJsonResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}