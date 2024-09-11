package edu.duke.adtg.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


@Component
public class DAOConn {
    
    private final DataSource dataSource;
    private final String schema;
   
    @Autowired
    public DAOConn(DataSource dataSource, @Value("${spring.datasource.schema}") String schema) {
        this.dataSource = dataSource;
        this.schema = schema;
    }

    public Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + schema);
        }
        return connection;
    }

}



