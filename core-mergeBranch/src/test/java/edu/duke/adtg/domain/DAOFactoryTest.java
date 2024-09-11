// package edu.duke.adtg.domain;

// import java.sql.Connection;
// import java.sql.SQLException;
// import org.junit.jupiter.api.Test;

// public class DAOFactoryTest {

//     @Test
//     // public static void main(String[] args) {
//     //     DAOFactoryTest test = new DAOFactoryTest();
//     //     test.testConnection();
//     // }

//     public void testConnection() {
//         DAOFactory daoFactory = new DAOFactory();
//         Connection connection = null;

//         try {
//             // Establishing a connection
//             connection = daoFactory.getConnection();

//             if (connection != null) {
//                 System.out.println("Connected to the database!");
//             } else {
//                 System.out.println("Failed to make connection!");
//             }

//         } catch (SQLException e) {
//             System.out.println("SQL Exception: " + e.getMessage());
//             e.printStackTrace();
//         } finally {
//             try {
//                 if (connection != null) {
//                     connection.close();
//                 }
//             } catch (SQLException ex) {
//                 ex.printStackTrace();
//             }
//         }
//     }
// }
