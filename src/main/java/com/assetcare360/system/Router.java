package com.assetcare360.system;

import com.assetcare360.system.interfaces.Controller;
import com.assetcare360.system.interfaces.Middleware;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * Router class for handling HTTP requests and routing them to the appropriate
 * handlers
 */
public class Router {
    private final HttpServer server;
    private final Map<RoutePattern, RouteHandler> routes = new HashMap<>();
    private final Map<RoutePattern, List<Middleware>> routeMiddlewares = new HashMap<>();
    private final List<Middleware> globalMiddlewares = new ArrayList<>();

    /**
     * Create a new Router instance
     * 
     * @param port The port to listen on
     * @throws IOException if the server cannot be created
     */
    public Router(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", this::handleRequest);
    }

    /**
     * Start the HTTP server
     */
    public void start() {
        server.start();
        System.out.println("Server started on port " + server.getAddress().getPort());
    }

    /**
     * Stop the HTTP server
     */
    public void stop() {
        server.stop(0);
        System.out.println("Server stopped");
    }

    /**
     * Register a controller with this router
     * 
     * @param basePath   The base path for all routes in the controller
     * @param controller The controller to register
     */
    public void registerController(String basePath, Controller controller) {
        controller.registerRoutes(this, basePath);
    }

    /**
     * Register a route with this router
     * 
     * @param path    The path pattern for this route
     * @param method  The HTTP method for this route
     * @param handler The handler for this route
     */
    public void register(String path, String method, RouteHandler handler) {
        RoutePattern pattern = new RoutePattern(path, method);
        routes.put(pattern, handler);
    }

    /**
     * Register a route with this router and specific middlewares
     * 
     * @param path        The path pattern for this route
     * @param method      The HTTP method for this route
     * @param handler     The handler for this route
     * @param middlewares The middlewares to apply to this route
     */
    public void register(String path, String method, RouteHandler handler, List<Middleware> middlewares) {
        RoutePattern pattern = new RoutePattern(path, method);
        routes.put(pattern, handler);
        routeMiddlewares.put(pattern, new ArrayList<>(middlewares));
    }

    /**
     * Add a global middleware that will be applied to all routes
     * 
     * @param middleware The middleware to add
     */
    public void addGlobalMiddleware(Middleware middleware) {
        globalMiddlewares.add(middleware);
    }

    /**
     * Add multiple global middlewares that will be applied to all routes
     * 
     * @param middlewares The middlewares to add
     */
    public void addGlobalMiddlewares(List<Middleware> middlewares) {
        globalMiddlewares.addAll(middlewares);
    }

    /**
     * Handle an incoming HTTP request
     * 
     * @param exchange The HttpExchange object representing the request and response
     * @throws IOException if an I/O error occurs
     */
    public void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        // Apply global middlewares first
        if (!applyMiddlewares(globalMiddlewares, exchange)) {
            return; // Middleware chain was interrupted
        }

        // Find the appropriate handler
        RoutePattern matchedPattern = null;
        RouteHandler handler = null;

        for (Map.Entry<RoutePattern, RouteHandler> entry : routes.entrySet()) {
            RoutePattern pattern = entry.getKey();
            if (pattern.matches(path, method)) {
                matchedPattern = pattern;
                handler = entry.getValue();
                break;
            }
        }

        if (handler != null) {
            // Apply route-specific middlewares if any
            List<Middleware> routeSpecificMiddlewares = routeMiddlewares.get(matchedPattern);
            if (routeSpecificMiddlewares != null && !applyMiddlewares(routeSpecificMiddlewares, exchange)) {
                return; // Middleware chain was interrupted
            }

            try {
                handler.handle(exchange);
            } catch (Exception e) {
                e.printStackTrace();
                String response = "{\"error\":\"Internal server error: " + e.getMessage() + "\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(500, response.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            }
        } else {
            String response = "{\"error\":\"Not found\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        }
    }

    // Only showing the modified method

    /**
     * Apply a list of middlewares to an HTTP exchange
     * 
     * @param middlewares The middlewares to apply
     * @param exchange    The HTTP exchange
     * @return true if all middlewares passed, false if any middleware interrupted
     *         the chain
     * @throws IOException if an I/O error occurs
     */
    private boolean applyMiddlewares(List<Middleware> middlewares, HttpExchange exchange) throws IOException {
        if (middlewares == null || middlewares.isEmpty()) {
            return true;
        }

        String path = exchange.getRequestURI().getPath();

        for (Middleware middleware : middlewares) {
            // Only apply middleware if it applies to this path
            if (middleware.appliesTo(path) && !middleware.process(exchange)) {
                return false; // Middleware chain was interrupted
            }
        }

        return true; // All middlewares passed
    }

    /**
     * Find a handler for the given path and method
     * 
     * @param path   The request path
     * @param method The request method
     * @return The handler for this route, or null if none is found
     */
    private RouteHandler findHandler(String path, String method) {
        for (Map.Entry<RoutePattern, RouteHandler> entry : routes.entrySet()) {
            RoutePattern pattern = entry.getKey();
            if (pattern.matches(path, method)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Class representing a route pattern
     */
    private static class RoutePattern {
        private final Pattern pattern;
        private final String method;

        public RoutePattern(String path, String method) {
            // Convert path pattern to regex
            // e.g. /users/{id} -> /users/([^/]+)
            String regex = path.replaceAll("\\{[^/]+\\}", "([^/]+)");
            this.pattern = Pattern.compile("^" + regex + "$");
            this.method = method.toUpperCase();
        }

        public boolean matches(String path, String method) {
            return this.method.equals(method.toUpperCase()) && pattern.matcher(path).matches();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null || getClass() != obj.getClass())
                return false;
            RoutePattern that = (RoutePattern) obj;
            return pattern.pattern().equals(that.pattern.pattern()) && method.equals(that.method);
        }

        @Override
        public int hashCode() {
            return 31 * pattern.pattern().hashCode() + method.hashCode();
        }
    }

    /**
     * Functional interface for route handlers
     */
    @FunctionalInterface
    public interface RouteHandler {
        void handle(HttpExchange exchange) throws IOException;
    }
}