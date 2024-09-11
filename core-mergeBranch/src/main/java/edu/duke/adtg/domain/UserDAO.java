package edu.duke.adtg.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Repository
public class UserDAO implements DAOFactory<User> {

    private DAOConn conn;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(UserDAO.class);


    @Autowired
    public UserDAO(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
    }
    
    @Override
    public void setConnection(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
    }

    @Override
    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    
    // ------------------------------Helper ------------------------------------------------


    // Check if user exists in the database
    public boolean checkUserExists(String netId) throws SQLException {
        String sql = "SELECT 1 FROM adtg.users WHERE netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
    
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        }
    }

    
    //check if user have specific role
    public boolean checkUserRole(String netId, String role) throws SQLException {
        String sql = "SELECT 1 FROM adtg.user_role WHERE netid = ? AND user_role = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
    
            ps.setString(1, netId);
            ps.setString(2, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); 
            }
        }
    }



    // Check if the user is enrolled in any course
    public boolean isUserEnrolledInAnyCourse(String netId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adtg.enrollment WHERE student_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Check if the user is assigned as a TA
    public boolean isUserAssignedAsTA(String netId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adtg.course_ta WHERE ta_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // Check if the user is assigned as an instructor
    public boolean isUserAssignedAsInstructor(String netId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM adtg.section WHERE instructor_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
    // ------------------------------CRUD------------------------------------------------------

    // Retrieve user by NetID
    public User getUserByPK(String netId) throws SQLException {
        String sql = "SELECT * FROM adtg.users WHERE netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getString("netid"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        }
        return null;
    }


    
    // retrieve a user with roles by netId
    public User findByNetId(String netId) throws SQLException {
        String sql = "SELECT users.netid, users.name, users.email, user_auth.hash, user_role.user_role FROM adtg.users " +
                     "JOIN adtg.user_auth ON users.netid = user_auth.netid " +
                     "LEFT JOIN adtg.user_role ON users.netid = user_role.netid WHERE users.netid = ? ORDER BY netid ASC, user_role ASC ";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                User user = null;
                Set<String> roles = new HashSet<>();
                while (rs.next()) {
                    if (user == null) {
                        user = new User(
                            rs.getString("netid"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("hash")
                        );
                    }
                    roles.add(rs.getString("user_role"));
                    
                }
                if (user != null) {
                    user.setRoles(roles);
                }
                return user;
            }
        }
    }



   // Load user with details and roles
    public void loadUserByPK(User user) throws SQLException {
        String sql = "SELECT users.*, user_role.user_role FROM adtg.users JOIN adtg.user_role ON users.netid = user_role.netid WHERE users.netid = ? ORDER BY netid ASC, user_role ASC ";
        
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setString(1, user.getNetId());
            
            try (ResultSet rs = ps.executeQuery()) {
                Set<String> roles = new HashSet<>();
                
                while (rs.next()) {
                    // Set user details directly
                    user.setNetId(rs.getString("netid"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    roles.add(rs.getString("user_role"));
                }
                
                user.setRoles(roles);
            }
        }
    }

    



    // Retrieve all users from the database
    public List<User> listUsers() throws SQLException {
        String sql = "SELECT * FROM adtg.users ORDER BY netid ASC";
        List<User> userList = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User(
                    rs.getString("netid"),
                    rs.getString("name"),
                    rs.getString("email")
                );
                userList.add(user);
            }
        }
        return userList;
    }

    

    // Retrieve roles for a user
    public Set<String> getUserRoles(String netId) throws SQLException {
        Set<String> roles = new HashSet<>();
        String sql = "SELECT user_role FROM adtg.user_role WHERE netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("user_role"));
                }
            }
        }
        return roles;
    }


    // Insert a new user 
    public void insertUser(User user) throws SQLException {
        String sql = "INSERT INTO adtg.users (netid, email, name) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getNetId());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getName());
            ps.executeUpdate();
        }
    }


    // Insert roles for a user
    public void insertUserRoles(String netId, Set<String> roles) throws SQLException {
        String sql = "INSERT INTO adtg.user_role (netid, user_role) VALUES (?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            for (String role : roles) {
                ps.setString(1, netId);
                ps.setString(2, role);
                ps.executeUpdate();
            }
        }
    }


    public void addUserRole(String netId, String role) throws SQLException {
        String sql = "INSERT INTO adtg.user_role (netid, user_role) VALUES (?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, role);
            ps.executeUpdate();
        }
    }

    public void deleteUserRole(String netId, String role) throws SQLException {
        String sql = "DELETE FROM adtg.user_role WHERE netid = ? AND user_role = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, role);
            ps.executeUpdate();
        }
    }

    // Insert password hash for a user
    public void insertUserPassword(String netId, String passwordHash) throws SQLException {
        String sql = "INSERT INTO adtg.user_auth (netid, hash) VALUES (?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, passwordHash);
            ps.executeUpdate();
        }
    }


    // Update user details
    public void updateUserDetails(User user) throws SQLException {
        String sql = "UPDATE adtg.users SET email = ?, name = ? WHERE netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getName());
            ps.setString(3, user.getNetId());
            ps.executeUpdate();
        }
    }

    // Update user hash
    public void updatePasswordHash(String netId, String newPasswordHash) throws SQLException {
        String sql = "UPDATE adtg.user_auth SET hash = ? WHERE netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, netId);
            ps.executeUpdate();
        }
    }


    // For admin portal, massively load the user roles
    public void addUserRoles(List<User> users, String userRole) throws SQLException {
        String roleSql = "INSERT INTO user_role (netid, user_role) VALUES (?, ?)";

        try (Connection connection = getConnection();
            PreparedStatement rolePs = connection.prepareStatement(roleSql)) {

            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                for (User user : users) {
                    rolePs.setString(1, user.getNetId());
                    rolePs.setString(2, userRole);
                    rolePs.addBatch();
                }

                rolePs.executeBatch();
                connection.commit();
                logger.info("Successfully added user roles.");
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Error during batch execution. Transaction rolled back.", e);
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Error adding user roles", e);
            throw e;
        }
    }


}
