package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class PasswordResetTokenDAO implements DAOFactory<PasswordResetToken> {

    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetTokenDAO.class);

    @Autowired
    public PasswordResetTokenDAO(DAOConn conn) {
        this.conn = conn;
    }

    @Override
    public void setConnection(DAOConn conn) {
        // No need to set connection as it's handled by DataSource
    }

    @Override
    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void insertToken(String netId, String token, LocalDateTime expirationTime) throws SQLException {
        String sql = "INSERT INTO user_token (netid, token, expiration_time, used) VALUES (?, ?, ?, 'N')";
        try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, token);
            ps.setTimestamp(3, Timestamp.valueOf(expirationTime));
            ps.executeUpdate();
        }
    }

    public void setTokenAsUsed(String netId, String token) throws SQLException {
        String sql = "UPDATE user_token SET used = 'Y' WHERE netid = ? AND token = ?";
        try (Connection conn = getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setString(1, netId);
            ps.setString(2, token);
            ps.executeUpdate();
        }
    }

    
    
    public PasswordResetToken getPasswordResetTokenByPK(String netId, String token) throws SQLException {
        String sql = "SELECT * FROM user_token WHERE netid = ? AND token = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, netId);
            stmt.setString(2, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp expirationTimestamp = rs.getTimestamp("expiration_time");
                LocalDateTime expirationTime = expirationTimestamp.toLocalDateTime();
                char used = rs.getString("used").charAt(0);
                return new PasswordResetToken(netId, token, expirationTime, used);
            }
        }
        return null;
    }
    
    
    public PasswordResetToken getLatestTokenByNetId(String netId) throws SQLException {
        String sql = "SELECT * FROM user_token WHERE netid = ? ORDER BY expiration_time DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, netId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String token = rs.getString("token");
                Timestamp expirationTimestamp = rs.getTimestamp("expiration_time");
                LocalDateTime expirationTime = expirationTimestamp.toLocalDateTime();
                char used = rs.getString("used").charAt(0);
                return new PasswordResetToken(netId, token, expirationTime, used);
            }
        }
        return null;
    }



}
