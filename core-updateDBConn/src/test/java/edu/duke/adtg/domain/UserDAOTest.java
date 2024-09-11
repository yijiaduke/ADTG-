// package edu.duke.adtg.domain;



// import edu.duke.adtg.dao.UserDAO;
// import edu.duke.adtg.domain.DAOFactory;
// import edu.duke.adtg.domain.User;


// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.mockito.Mockito;
// import org.springframework.test.annotation.Rollback;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.DriverManager;
// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.*;


// public class UserDAOTest {

//     private DAOFactory daoFactory;
//     private UserDAO userDAO;
//     private Connection connection;
//     private PreparedStatement preparedStatement;
//     private ResultSet resultSet;

//     @BeforeEach
//     public void setUp() throws SQLException, ClassNotFoundException {
//         // Load the PostgreSQL JDBC driver
//         Class.forName("org.postgresql.Driver");

//         daoFactory = mock(DAOFactory.class);
//         connection = mock(Connection.class);
//         preparedStatement = mock(PreparedStatement.class);
//         resultSet = mock(ResultSet.class);

//         when(daoFactory.getConnection()).thenReturn(connection);
//         when(connection.prepareStatement(anyString(), anyInt())).thenReturn(preparedStatement);
//         when(preparedStatement.executeUpdate()).thenReturn(1);
//         when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        
//         userDAO = new UserDAO(daoFactory);
//     }
    
//     @Test 
//     @Rollback(false)
//     void testCreate() {
//         User test = new User ("yz853", "yijia", "yijia@duke.edu");
//         userDAO.createUser(test);
//         assertNotNull(test.getNetId());
//         assertEquals("yijia", test.getName());
//         assertEquals("yz853", test.getNetId());
//         assertEquals("yijia@duke.edu", test.getEmail());
//     }
// }
