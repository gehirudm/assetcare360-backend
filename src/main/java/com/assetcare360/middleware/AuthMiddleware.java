package com.assetcare360.middleware;

import com.assetcare360.interfaces.Middleware;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class AuthMiddleware implements Middleware {
    
    @Override
    public boolean process(HttpExchange exchange) throws IOException {
        // Get the path
        String path = exchange.getRequestURI().getPath();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return true;
        }
        
        // Check for Authorization header
        List<String> authHeaders = exchange.getRequestHeaders().get("Authorization");
        if (authHeaders == null || authHeaders.isEmpty()) {
            sendUnauthorizedResponse(exchange, "Authorization header is required");
            return false;
        }
        
        String authHeader = authHeaders.get(0);
        if (!authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(exchange, "Invalid authorization format");
            return false;
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate token (simplified for example)
        if (!validateToken(token)) {
            sendUnauthorizedResponse(exchange, "Invalid or expired token");
            return false;
        }
        
        return true;
    }
    
    private boolean isPublicEndpoint(String path) {
        // Define public endpoints that don't require authentication
        return path.equals("/api/login") || path.equals("/api/register");
    }
    
    private boolean validateToken(String token) {
        // Implement token validation logic
        // This is a placeholder - you would implement JWT validation or similar
        return !token.isEmpty();
    }
    
    private void sendUnauthorizedResponse(HttpExchange exchange, String message) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        String response = "{\"error\":\"" + message + "\"}";
        exchange.sendResponseHeaders(401, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
        exchange.close();
    }
}