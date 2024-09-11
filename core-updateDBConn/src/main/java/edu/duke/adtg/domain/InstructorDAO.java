package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class InstructorDAO {

    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(InstructorDAO.class);

    @Autowired
    public InstructorDAO(DAOConn conn) {
        this.conn = conn;
    }

    public void setConnection(DAOConn conn) {
    }

    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Instructor getInstructorByNetId(String netId) throws SQLException {
        String sql = "SELECT * FROM adtg.users WHERE netid = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Instructor(
                        rs.getString("netid"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        } 
        return null;
    }
}
