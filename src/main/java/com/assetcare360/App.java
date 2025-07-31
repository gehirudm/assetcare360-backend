package com.assetcare360;

import com.assetcare360.middleware.AuthMiddleware;
import com.assetcare360.system.Router;
import com.assetcare360.controllers.*;
import java.io.IOException;

public class App {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try {
            // Create router (which creates its own HTTP server)
            Router router = new Router(PORT);

            // Register global middlewares
            router.addGlobalMiddleware(new AuthMiddleware());
            // Add other global middlewares here as needed
            
            // Register controllers
            registerControllers(router);

            // Start the server
            router.start();

            System.out.println("Server started on port " + PORT);
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void registerControllers(Router router) {
        router.registerController("/users", new com.assetcare360.controllers.GenericCrudController<>(com.assetcare360.models.User.class));
        router.registerController("/api/auth", new AuthController());
        // Register other controllers here
    }
}