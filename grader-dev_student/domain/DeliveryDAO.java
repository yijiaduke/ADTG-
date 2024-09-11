package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DeliveryDAO implements DAOFactory<Delivery> {

    private DAOConn conn;
    private Connection connection;

    public DeliveryDAO(DAOConn conn) {
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

    public void save(Delivery delivery){
        String sql = "INSERT INTO adtg.delivery (delivery_time, c_subject, c_number, assn, netid, status, log_text)"+
        "VALUES (?, ?, ?, ?, ?, ?, ?) ;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, delivery.getTime(),delivery.getAssessment().getSubject(),delivery.getAssessment().getNumber(),
            delivery.getAssessment().getAssn(),delivery.getStudent().getNetId(),delivery.getStatus().toString(),delivery.getLog());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
