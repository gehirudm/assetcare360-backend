package com.assetcare360.system.interfaces;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface for all database models in the application.
 * Provides standard methods for CRUD operations and database interactions.
 */
public interface Model<T> extends JsonSerializable<T> {
    
    /**
     * Saves the current model instance to the database.
     * Creates a new record if it doesn't exist, or updates if it does.
     * 
     * @return true if the operation was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean save() throws SQLException;
    
    /**
     * Retrieves a model instance by its primary key.
     * 
     * @param id the primary key value
     * @return an Optional containing the model if found, empty otherwise
     * @throws SQLException if a database error occurs
     */
    Optional<T> findById(Object id) throws SQLException;
    
    /**
     * Retrieves all records from the associated table.
     * 
     * @return a List of model instances
     * @throws SQLException if a database error occurs
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Finds records matching the specified criteria.
     * 
     * @param criteria a Map of column names to values for filtering
     * @return a List of matching model instances
     * @throws SQLException if a database error occurs
     */
    List<T> findWhere(Map<String, Object> criteria) throws SQLException;
    
    /**
     * Updates the current model instance in the database.
     * 
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean update() throws SQLException;
    
    /**
     * Deletes the current model instance from the database.
     * 
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    boolean delete() throws SQLException;
    
    /**
     * Gets the name of the database table associated with this model.
     * 
     * @return the table name
     */
    String getTableName();
    
    /**
     * Gets the name of the primary key column for this model.
     * 
     * @return the primary key column name
     */
    String getPrimaryKeyName();
    
    /**
     * Gets a database connection for operations.
     * 
     * @return a Connection object
     * @throws SQLException if a connection cannot be established
     */
    Connection getConnection() throws SQLException;
    
    /**
     * Converts the model to a Map representation.
     * 
     * @return a Map of column names to values
     */
    Map<String, Object> toMap();
    
    /**
     * Creates a model instance from a Map of values.
     * 
     * @param values a Map of column names to values
     * @return a new model instance
     */
    T fromMap(Map<String, Object> values);
}