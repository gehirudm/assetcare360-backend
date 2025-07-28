package com.assetcare360.interfaces;

import java.util.List;

/**
 * Generic interface for model factories
 * @param <T> The model type this factory creates
 */
public interface Factory<T> {
    /**
     * Create a single model instance with default values
     * @return A new model instance
     */
    T create();
    
    /**
     * Create a single model instance with custom attributes
     * @param attributes Custom attributes to override defaults
     * @return A new model instance
     */
    T create(Object... attributes);
    
    /**
     * Create multiple model instances with default values
     * @param count Number of instances to create
     * @return List of new model instances
     */
    List<T> createMany(int count);
    
    /**
     * Create multiple model instances with custom attributes
     * @param count Number of instances to create
     * @param attributes Custom attributes to override defaults
     * @return List of new model instances
     */
    List<T> createMany(int count, Object... attributes);
}