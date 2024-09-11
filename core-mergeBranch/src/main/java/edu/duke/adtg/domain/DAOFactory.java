package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface DAOFactory<T> {

    default void setStatementObjects(PreparedStatement statement, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            statement.setObject(i + 1, values[i]);
        }
    }
    public void setConnection(DAOConn conn);
    public Connection getConnection();
    // public void add(T t);
    // public T getById(String id);
    // public void deleteById(String id);
    // public void update(T t);
    // public T map(ResultSet rs) throws SQLException;
    // public void removeAll();
}
