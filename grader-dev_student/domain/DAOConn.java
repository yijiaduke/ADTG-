package edu.duke.adtg.domain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DAOConn {
    // private static final String PROPERTIES_FILE = "dao.properties";
    // private static final Properties properties = new Properties();
    // // private static final String CREATE_TABLE_SQL_FILE = "databaseSetup.sql";
    // private static String SQL;

    // static {
    //     ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    //     InputStream propertiesFile = classLoader.getResourceAsStream(PROPERTIES_FILE);

    //     if (propertiesFile == null) {
    //         throw new RuntimeException("Properties file '" + PROPERTIES_FILE + "' not found in the classpath");
    //     }

    //     try {
    //         properties.load(propertiesFile);
    //     } catch (IOException e) {
    //         throw new RuntimeException("Cannot load properties file '" + PROPERTIES_FILE + "'", e);
    //     }

    //     // InputStream createDBFile = classLoader.getResourceAsStream(CREATE_TABLE_SQL_FILE);

    //     // if (createDBFile == null) {
    //     //     throw new RuntimeException("Database table '" + CREATE_TABLE_SQL_FILE + "' not found in the classpath");
    //     // }

    //     try (BufferedReader reader = new BufferedReader(new InputStreamReader(createDBFile))) {
    //         StringBuilder sb = new StringBuilder();
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             sb.append(line).append("\n");
    //         }
    //         SQL = sb.toString();
    //     } catch (IOException e) {
    //         throw new RuntimeException("Cannot read SQL file '" + CREATE_TABLE_SQL_FILE + "'", e);
    //     }

    // }

    private final String url;
    private final String username;
    private final String password;
    private final String schema;

    // public DAOConn() {
    //     url = properties.getProperty("url");
    //     username = properties.getProperty("username");
    //     password = properties.getProperty("password");
    // }

    public DAOConn() {
        // You can hard-code the values here or pass them through constructor
        this.url = "jdbc:postgresql://vcm-41568.vm.duke.edu:5432/proj24db"; // Change as needed
        this.username = "postgres"; // Change as needed
        this.password = "12345"; // Change as needed
        this.schema = "adtg";
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


    // public void initializeTable() throws  SQLException{
    //     Connection conn = getConnection();

    //     if (conn!=null){
    //         Statement stmt = conn.createStatement();
    //         stmt.execute(SQL);
    //         System.out.println("Table 'Course' created successfully.");
    //     }
    // }

    public void dropAllTables() throws  SQLException{
        Connection conn = getConnection();
        if (conn!=null){
            Statement stmt = conn.createStatement();
            stmt.execute("DROP SCHEMA IF EXISTS public CASCADE;");
        }
    }
}



