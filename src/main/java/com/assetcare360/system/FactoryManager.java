package com.assetcare360.system;

import java.util.HashMap;
import java.util.Map;

import com.assetcare360.system.interfaces.Factory;

/**
 * Manager class for all factories in the application
 */
public class FactoryManager {
    private static final Map<Class<?>, Factory<?>> factories = new HashMap<>();
    
    /**
     * Register a factory for a specific model class
     * @param modelClass The model class
     * @param factory The factory instance
     * @param <T> The model type
     */
    public static <T> void register(Class<T> modelClass, Factory<T> factory) {
        factories.put(modelClass, factory);
    }
    
    /**
     * Get a factory for a specific model class
     * @param modelClass The model class
     * @param <T> The model type
     * @return The factory instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Factory<T> getFactory(Class<T> modelClass) {
        Factory<?> factory = factories.get(modelClass);
        if (factory == null) {
            throw new IllegalArgumentException("No factory registered for " + modelClass.getName());
        }
        return (Factory<T>) factory;
    }
}