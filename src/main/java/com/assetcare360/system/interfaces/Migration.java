package com.assetcare360.system.interfaces;

import java.sql.Connection;
import java.sql.SQLException;

public interface Migration {
    void up(Connection connection) throws SQLException;
    void down(Connection connection) throws SQLException;
    int getVersion();
}