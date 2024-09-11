// package edu.duke.adtg.domain;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.Mockito.*;

// public class CourseDAOTest {
    
//     private DAOConn conn = new DAOConn();
    
    
//     private CourseDAO courseDAO = new CourseDAO(conn);



//     @Test
//     void testLoad() throws SQLException {
//         Course course = new Course("CS", 101);
//         courseDAO.load(course);
//         assertNotNull(course.getTitle());
//         assertNotNull(course.getRepo());
//         System.out.println(course.getTitle());
//     }
// }
