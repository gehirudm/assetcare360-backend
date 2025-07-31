package com.assetcare360.system.interfaces;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.util.regex.Pattern;

public interface Middleware {
    /**
     * Process the request through this middleware
     * @param exchange The HTTP exchange
     * @return true if the request should continue to the next middleware/handler, false to stop processing
     * @throws IOException if an I/O error occurs
     */
    boolean process(HttpExchange exchange) throws IOException;
    
    /**
     * Determines if this middleware should be applied to the given path
     * @param path The request path
     * @return true if this middleware should be applied, false otherwise
     */
    default boolean appliesTo(String path) {
        return true; // By default, apply to all paths
    }
    
    /**
     * Create a middleware that only applies to paths matching the given pattern
     * @param middleware The middleware to apply
     * @param pathPattern The regex pattern for paths to apply this middleware to
     * @return A new middleware that only applies to matching paths
     */
    static Middleware forPattern(Middleware middleware, String pathPattern) {
        final Pattern pattern = Pattern.compile(pathPattern);
        
        return new Middleware() {
            @Override
            public boolean process(HttpExchange exchange) throws IOException {
                return middleware.process(exchange);
            }
            
            @Override
            public boolean appliesTo(String path) {
                return pattern.matcher(path).matches();
            }
        };
    }
    
    /**
     * Create a middleware that only applies to paths with the given prefix
     * @param middleware The middleware to apply
     * @param prefix The prefix for paths to apply this middleware to
     * @return A new middleware that only applies to paths with the given prefix
     */
    static Middleware forPrefix(Middleware middleware, String prefix) {
        return new Middleware() {
            @Override
            public boolean process(HttpExchange exchange) throws IOException {
                return middleware.process(exchange);
            }
            
            @Override
            public boolean appliesTo(String path) {
                return path.startsWith(prefix);
            }
        };
    }
}