package com.assetcare360.controllers;

import com.assetcare360.system.Router;
import com.assetcare360.system.interfaces.BaseController;
import com.assetcare360.system.interfaces.Model;
import com.sun.net.httpserver.HttpExchange;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GenericCrudController<T extends Model<T>> extends BaseController {
    private final Class<T> modelClass;
    private final T modelInstance;

    public GenericCrudController(Class<T> modelClass) {
        this.modelClass = modelClass;
        this.modelInstance = createModelInstance();
    }

    private T createModelInstance() {
        try {
            Constructor<T> ctor = modelClass.getDeclaredConstructor();
            ctor.setAccessible(true);
            return ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate model: " + modelClass.getName(), e);
        }
    }

    @Override
    public void registerRoutes(Router router, String basePath) {
        router.register(basePath, "GET", this::getAll);
        router.register(basePath + "/{id}", "GET", this::getById);
        router.register(basePath, "POST", this::create);
        router.register(basePath + "/{id}", "PUT", this::update);
        router.register(basePath + "/{id}", "DELETE", this::delete);
    }

    private void getAll(HttpExchange exchange) throws IOException {
        try {
            List<T> items = modelInstance.findAll();
            String response = (String) modelClass.getMethod("listToJson", List.class).invoke(null, items);
            sendJsonResponse(exchange, 200, response);
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    private void getById(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/([a-zA-Z]+)/([0-9]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(2));
            try {
                Optional<T> itemOpt = (Optional<T>) modelClass.getMethod("findById", int.class).invoke(modelInstance, id);
                if (itemOpt.isPresent()) {
                    T item = itemOpt.get();
                    String response = (String) item.getClass().getMethod("toJson").invoke(item);
                    sendJsonResponse(exchange, 200, response);
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid ID\"}");
        }
    }

    private void create(HttpExchange exchange) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String body = sb.toString();
        try {
            T item = (T) modelClass.getMethod("fromJson", String.class).invoke(modelInstance, body);
            if (item == null) {
                sendJsonResponse(exchange, 400, "{\"error\":\"Invalid JSON\"}");
                return;
            }
            boolean saved = (boolean) item.getClass().getMethod("save").invoke(item);
            if (saved) {
                String response = (String) item.getClass().getMethod("toJson").invoke(item);
                sendJsonResponse(exchange, 201, response);
            } else {
                sendJsonResponse(exchange, 500, "{\"error\":\"Failed to save\"}");
            }
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
        }
    }

    private void update(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/([a-zA-Z]+)/([0-9]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(2));
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            try {
                Optional<T> itemOpt = (Optional<T>) modelClass.getMethod("findById", int.class).invoke(modelInstance, id);
                if (itemOpt.isPresent()) {
                    T item = (T) modelClass.getMethod("fromJson", String.class).invoke(modelInstance, body);
                    item.getClass().getMethod("setId", Integer.class).invoke(item, id);
                    boolean updated = (boolean) item.getClass().getMethod("update").invoke(item);
                    if (updated) {
                        String response = (String) item.getClass().getMethod("toJson").invoke(item);
                        sendJsonResponse(exchange, 200, response);
                    } else {
                        sendJsonResponse(exchange, 500, "{\"error\":\"Failed to update\"}");
                    }
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid ID\"}");
        }
    }

    private void delete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        Pattern pattern = Pattern.compile("/([a-zA-Z]+)/([0-9]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            int id = Integer.parseInt(matcher.group(2));
            try {
                Optional<T> itemOpt = (Optional<T>) modelClass.getMethod("findById", int.class).invoke(modelInstance, id);
                if (itemOpt.isPresent()) {
                    T item = itemOpt.get();
                    boolean deleted = (boolean) item.getClass().getMethod("delete").invoke(item);
                    if (deleted) {
                        sendJsonResponse(exchange, 204, "");
                    } else {
                        sendJsonResponse(exchange, 500, "{\"error\":\"Failed to delete\"}");
                    }
                } else {
                    sendJsonResponse(exchange, 404, "{\"error\":\"Not found\"}");
                }
            } catch (Exception e) {
                sendJsonResponse(exchange, 500, "{\"error\":\"Database error: " + e.getMessage() + "\"}");
            }
        } else {
            sendJsonResponse(exchange, 400, "{\"error\":\"Invalid ID\"}");
        }
    }
}
