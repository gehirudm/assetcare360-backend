package com.assetcare360.system.interfaces;

import com.assetcare360.system.Router;

public interface Controller {
    void registerRoutes(Router router, String basePath);
}