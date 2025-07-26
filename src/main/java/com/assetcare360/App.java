package com.assetcare360;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.assetcare360.middleware.AuthMiddleware;
import com.assetcare360.system.Router;
import com.assetcare360.controllers.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class App {
    private static final int PORT = 8080;
    private static final int BACKLOG = 0; // Default backlog value

    public static void main(String[] args) {
        try {
            // Create HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), BACKLOG);

            // Create router
            Router router = new Router();

            // Register controllers
            registerControllers(router);

            // Create context for API endpoints
            server.createContext("/api", new ApiHandler(router));

            // Set executor
            server.setExecutor(Executors.newFixedThreadPool(10));

            // Start server
            server.start();

            System.out.println("Server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void registerControllers(Router router) {
        router.registerController("/users", new UserController());
    }

    static class ApiHandler implements HttpHandler {
        private final Router router;

        public ApiHandler(Router router) {
            this.router = router;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                // Apply global middlewares
                if (!applyMiddlewares(exchange)) {
                    return; // Middleware chain was interrupted
                }

                // Route the request
                boolean handled = router.route(exchange);

                if (!handled) {
                    // No route matched
                    String response = "Not Found";
                    exchange.sendResponseHeaders(404, response.length());
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(response.getBytes());
                    }
                }
            } catch (Exception e) {
                // Handle any exceptions
                String response = "Internal Server Error";
                exchange.sendResponseHeaders(500, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        }

        private boolean applyMiddlewares(HttpExchange exchange) throws IOException {
            // Apply global middlewares here
            // Example: Authentication middleware
            AuthMiddleware authMiddleware = new AuthMiddleware();
            return authMiddleware.process(exchange);
        }
    }
}