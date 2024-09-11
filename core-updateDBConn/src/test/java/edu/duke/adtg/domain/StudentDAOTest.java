// package edu.duke.adtg.domain;

// import org.junit.jupiter.api.*;
// import java.sql.*;
// import java.util.*;
// import static org.junit.jupiter.api.Assertions.*;
// import java.time.LocalDate;

// @TestInstance(TestInstance.Lifecycle.PER_CLASS)
// public class StudentDAOTest {

//     private static DAOConn daoConn;
//     private static StudentDAO studentDAO;

//     @BeforeAll
//     public static void setUp(){
//         daoConn = new DAOConn();
//         studentDAO = new StudentDAO(daoConn);
//         // studentDAO.setConnection(daoConn);
//         // initializeDatabase();
//     }

//     // // @AfterAll
//     // // public static void tearDown() throws SQLException {
//     // //     // Close database connection
//     // //     Connection connection = daoConn.getConnection();
//     // //     if (connection != null) {
//     // //         connection.close();
//     // //     }
//     // // }


//     // // @BeforeEach
//     // // public void cleanDatabase() throws SQLException {
//     // //     try (Connection connection = daoFactory.getConnection();
//     // //          PreparedStatement clearUsers = connection.prepareStatement("DELETE FROM users");
//     // //          PreparedStatement clearUserAuth = connection.prepareStatement("DELETE FROM user_auth");
//     // //          PreparedStatement clearUserRole = connection.prepareStatement("DELETE FROM user_role")) {
//     // //         clearUserRole.executeUpdate();
//     // //         clearUserAuth.executeUpdate();
//     // //         clearUsers.executeUpdate();
//     // //     }
//     // // }

//     // // @Test
//     // // void testCreateStudent() throws SQLException {
//     // //     // Test creating a new student
//     // //     Student test = new Student("yz123", "yz", "yz@duke.edu", "newhash");
//     // //     studentDAO.createStudent(test, "newhash", "student", "section1");

//     // //     // Verify user was created
//     // //     try (Connection connection = daoFactory.getConnection();
//     // //          PreparedStatement query = connection.prepareStatement("SELECT * FROM users WHERE netId = ?")) {
//     // //         query.setString(1, "yz123");
//     // //         ResultSet resultSet = query.executeQuery();
//     // //         assertTrue(resultSet.next());
//     // //         assertEquals("yz@duke.edu", resultSet.getString("email"));
//     // //         assertEquals("yz", resultSet.getString("name"));
//     // //         assertEquals("yz123", resultSet.getString("netId"));
//     // //     }

//     // //     // Verify user_auth was created
//     // //     try (Connection connection = daoFactory.getConnection();
//     // //          PreparedStatement query = connection.prepareStatement("SELECT * FROM user_auth WHERE netId = ?")) {
//     // //         query.setString(1, "yz123");
//     // //         ResultSet resultSet = query.executeQuery();
//     // //         assertTrue(resultSet.next());
//     // //         assertEquals("newhash", resultSet.getString("hash"));
//     // //     }

//     // //     // Verify user_role was created
//     // //     try (Connection connection = daoFactory.getConnection();
//     // //          PreparedStatement query = connection.prepareStatement("SELECT * FROM user_role WHERE netId = ?")) {
//     // //         query.setString(1, "yz123");
//     // //         ResultSet resultSet = query.executeQuery();
//     // //         assertTrue(resultSet.next());
//     // //         assertEquals("student", resultSet.getString("role"));
//     // //         assertEquals("section1", resultSet.getString("sectionId"));
//     // //     }
//     // // }

//     // // @Test
//     // // void testGetEnrollment() throws SQLException {
//     // //     // Call the method to test getEnrollment
//     // //     List<String> sections = studentDAO.getEnrollment("yz123");

//     // //     // Verify the result
//     // //     assertEquals(2, sections.size());
//     // //     // assertTrue(sections.contains("1"));
//     // //     // assertTrue(sections.contains("1"));
//     // // }

//     // @Test
//     // void testGetSectionByStudentNetId() throws SQLException {
//     //     List<Section> sections = studentDAO.getSectionsByStudentNetId("yz123");

//     //     // Verify the result
//     //     assertEquals(2, sections.size());

//     //     Section section1 = sections.get(0);
//     //     assertEquals("CS", section1.getCourse().getSubject().trim());
//     //     assertEquals(101, section1.getCourse().getNumber().intValue());
//     //     assertEquals("instructor1", section1.getInstructor().getNetId());
//     //     assertEquals(LocalDate.of(2023, 1, 10), section1.getDate());
    
//     //     Section section2 = sections.get(1);
//     //     assertEquals("CS", section2.getCourse().getSubject().trim());
//     //     assertEquals(102, section2.getCourse().getNumber().intValue());
//     //     assertEquals("instructor2", section2.getInstructor().getNetId());
//     //     assertEquals(LocalDate.of(2023, 1, 15), section2.getDate());
//     // }
// }
