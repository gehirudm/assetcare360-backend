package com.assetcare360.controllers;

import com.assetcare360.models.User;
import com.assetcare360.stores.UserStore;
import com.assetcare360.system.Router;
import com.assetcare360.system.interfaces.Controller;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;

import com.assetcare360.system.interfaces.BaseController;

public class AuthController extends BaseController {
    private final UserStore userStore = new UserStore();

    @Override
    public void registerRoutes(Router router, String basePath) {
        router.register(basePath + "/login", "POST", this::login);
    }

    private void login(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
            return;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        Map<String, String> payload = parseJsonBody(body);
        String employeeId = payload.get("employeeId");
        String password = payload.get("password");
        String email = payload.get("email");
        if (employeeId == null || password == null || email == null) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Missing employeeId, email, or password\"}");
            return;
        }
        try {
            List<User> users = userStore.findByEmployeeId(employeeId);
            if (users.isEmpty()) {
                sendJsonResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                return;
            }
            User user = users.get(0);
            if (!user.getEmail().equalsIgnoreCase(email)) {
                sendJsonResponse(exchange, 401, "{\"error\":\"Email does not match\"}");
                return;
            }
            // Password check (assuming bcrypt hash)
            if (!BCrypt.checkpw(password, user.getPassword())) {
                sendJsonResponse(exchange, 401, "{\"error\":\"Invalid credentials\"}");
                return;
            }
            // Success: return user info (omit password)
            sendJsonResponse(exchange, 200, user.toJson());
        } catch (SQLException e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    private Map<String, String> parseJsonBody(String body) {
        Map<String, String> map = new HashMap<>();
        body = body.trim().replaceAll("[{}\" ]", "");
        String[] pairs = body.split(",");
        for (String pair : pairs) {
            String[] kv = pair.split(":");
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }
}
