package edu.duke.adtg.domain;
// package grader.domain;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Repository;
// import java.sql.*;
// import java.util.HashSet;
// import java.util.Set;


// @Repository
// public class UserDAO implements DAOFactory<User> {

//     private DAOConn conn;
//     private Connection connection;

//     @Autowired
//     public UserDAO(DAOConn conn) {
//         this.conn = conn;
//         connection = getConnection();
//     }
    
//     @Override
//     public void setConnection(DAOConn conn) {
//         this.conn = conn;
//         connection = getConnection();
//     }

//     @Override
//     public Connection getConnection() {
//         try {
//             return conn.getConnection();
//         } catch (SQLException e) {
//             throw new RuntimeException(e);
//         }
//     }

    
//     // retrieve a user with their roles by their netId
//     public User findByNetId(String netId) {
//         String sql = "SELECT users.netid, users.name, users.email, user_auth.hash, user_role.user_role FROM users " +
//                      "JOIN user_auth ON users.netid = user_auth.netid " +
//                      "LEFT JOIN user_role ON users.netid = user_role.netid WHERE users.netid = ?";
//         try (PreparedStatement ps = connection.prepareStatement(sql)) {
//             ps.setString(1, netId);
//             try (ResultSet rs = ps.executeQuery()) {
//                 User user = null;
//                 Set<String> roles = new HashSet<>();
//                 while (rs.next()) {
//                     if (user == null) {
//                         user = new User(
//                             rs.getString("netid"),
//                             rs.getString("name"),
//                             rs.getString("email"),
//                             rs.getString("hash")
//                         );
//                     }
//                     roles.add(rs.getString("user_role"));
//                 }
//                 if (user != null) {
//                     user.setRoles(roles);
//                 }
//                 return user;
//             }
//         } catch (SQLException e) {
//             throw new RuntimeException(e);
//         }
//     }
// }
