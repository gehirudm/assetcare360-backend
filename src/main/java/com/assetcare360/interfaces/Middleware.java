package com.assetcare360.interfaces;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public interface Middleware {
    /**
     * Process the request through this middleware
     * @param exchange The HTTP exchange
     * @return true if the request should continue to the next middleware/handler, false to stop processing
     */
    boolean process(HttpExchange exchange) throws IOException;
}
