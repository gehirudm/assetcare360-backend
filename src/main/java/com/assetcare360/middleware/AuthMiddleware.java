package com.assetcare360.middleware;

import com.assetcare360.system.interfaces.Middleware;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthMiddleware implements Middleware {
    // Set of paths that don't require authentication
    private final Set<String> publicPaths = new HashSet<>(Arrays.asList(
        "/login",
        "/register",
        "/public",
        "/health"
        // Add other public paths here
    ));
    
    @Override
    public boolean process(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        
        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            return true;
        }
        
        // Get the Authorization header
        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
        
        // Check if the Authorization header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(exchange, "Missing or invalid Authorization header");
            return false;
        }
        
        // Extract the token
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        // Validate the token
        if (!validateToken(token)) {
            sendUnauthorizedResponse(exchange, "Invalid or expired token");
            return false;
        }
        
        // If we get here, the token is valid
        // You can add user information to the exchange attributes for use in handlers
        String userId = extractUserIdFromToken(token);
        exchange.setAttribute("userId", userId);
        
        // Continue to the next middleware or handler
        return true;
    }
    
    private boolean isPublicEndpoint(String path) {
        // Check if the path is in the public paths set
        for (String publicPath : publicPaths) {
            if (path.equals(publicPath) || path.startsWith(publicPath + "/")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean validateToken(String token) {
        // Implement token validation logic here
        // This could involve checking against a database, verifying a JWT, etc.
        // For now, we'll just return true for demonstration purposes
        return !token.isEmpty();
    }
    
    private String extractUserIdFromToken(String token) {
        // Extract user ID from the token
        // This would typically involve decoding a JWT or looking up in a database
        // For now, we'll just return a placeholder
        return "user-123";
    }
    
    private void sendUnauthorizedResponse(HttpExchange exchange, String message) throws IOException {
        String response = "{\"error\":\"" + message + "\"}";
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("WWW-Authenticate", "Bearer");
        exchange.sendResponseHeaders(401, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}