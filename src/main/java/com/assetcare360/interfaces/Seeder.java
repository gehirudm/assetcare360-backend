package com.assetcare360.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface Seeder {
    void seed(Connection connection) throws SQLException;
    String getName();
}