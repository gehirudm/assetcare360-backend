package com.assetcare360.config;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class Roles {
    private static final String CONFIG_PATH = "src/main/resources/roles-config.json";
    private static Map<String, List<String>> rolePermissions;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (FileReader reader = new FileReader(CONFIG_PATH)) {
            rolePermissions = new Gson().fromJson(reader, new TypeToken<Map<String, List<String>>>(){}.getType());
        } catch (IOException e) {
            rolePermissions = new HashMap<>();
            System.err.println("Failed to load roles config: " + e.getMessage());
        }
    }

    public static List<String> getPermissions(String role) {
        return rolePermissions.getOrDefault(role, Collections.emptyList());
    }

    public static boolean hasPermission(String role, String permission) {
        return getPermissions(role).contains(permission);
    }

    public static Set<String> getAllRoles() {
        return rolePermissions.keySet();
    }
}
