package edu.duke.adtg.domain;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class DAOConn {
    // private static final String PROPERTIES_FILE = "dao.properties";
    // private static final Properties properties = new Properties();


    private static final String CREATE_TABLE_SQL_FILE = "/graderApp/config/databaseSetup.sql";
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    InputStream createDBFile = classLoader.getResourceAsStream(CREATE_TABLE_SQL_FILE);
    private String url;
    private String username;
    private String password;
    private String schema;


    public DAOConn() {
        loadProperties();
    }
    public void loadProperties() {
        Properties properties = new Properties();
        try (InputStream input = new FileInputStream("/graderApp/volume/resources/db.properties")) {
            // if (input == null) {
            //     System.out.println("Sorry, unable to find db.properties");
            //     return;
            // }
            // Load the properties file
            properties.load(input);

            // Get the properties value
            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            schema = properties.getProperty("db.schema");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(url, username, password);
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + schema);
        }
        return connection;
    }

    // public Connection getConnection() throws SQLException {
    //     return DriverManager.getConnection(url, username, password);
    // }

    public void createSchemaPublic() throws SQLException {
        Connection conn = getConnection();

        if (conn!=null){
            Statement stmt = conn.createStatement();
            stmt.execute("create schema public;");
            System.out.println("Table 'Course' created successfully.");
        }
    }


    public void initializeTable() throws  SQLException{
        Connection conn = getConnection();
        String SQL;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(createDBFile))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            SQL = sb.toString();
        } catch (IOException e) {
            throw new RuntimeException("Cannot read SQL file '" + CREATE_TABLE_SQL_FILE + "'", e);
        }
        if (conn!=null){
            Statement stmt = conn.createStatement();
            stmt.execute(SQL);
        }
    }

    public void dropSchema() throws SQLException {
        Connection conn = getConnection();
        String sql = "DROP SCHEMA IF EXISTS " + "adtg" + " CASCADE";
        if (conn!=null){
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
        }
    }

    public void dropAllTables() throws  SQLException{
        Connection conn = getConnection();
        if (conn!=null){
            Statement stmt = conn.createStatement();
            stmt.execute("DROP SCHEMA IF EXISTS public CASCADE;");
        }
    }
}



