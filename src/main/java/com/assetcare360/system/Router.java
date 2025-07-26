package com.assetcare360.system;

import com.assetcare360.interfaces.Controller;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private final Map<String, RouteHandler> routes = new HashMap<>();
    
    public void register(String path, String method, RouteHandler handler) {
        String key = method + ":" + path;
        routes.put(key, handler);
    }
    
    public boolean route(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath().substring(4); // Remove "/api" prefix
        String method = exchange.getRequestMethod();
        String key = method + ":" + path;
        
        RouteHandler handler = routes.get(key);
        if (handler != null) {
            handler.handle(exchange);
            return true;
        }
        
        return false;
    }
    
    public void registerController(String basePath, Controller controller) {
        controller.registerRoutes(this, basePath);
    }
    
    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}
