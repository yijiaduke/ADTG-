package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class DeliveryDAO implements DAOFactory<Delivery> {

    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(DeliveryDAO.class);

    @Autowired
    public DeliveryDAO(DAOConn conn) {
        this.conn = conn;
    }

    @Override
    public void setConnection(DAOConn conn) {
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
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, delivery.getTime(),delivery.getAssessment().getSubject(),delivery.getAssessment().getNumber(),
            delivery.getAssessment().getAssn(),delivery.getStudent().getNetId(),delivery.getStatus().toString(),delivery.getLog());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(Delivery delivery){
        String sql = "UPDATE adtg.delivery SET status = ?, log_text = ? WHERE "+
        "delivery_time = ? AND c_subject = ? AND c_number = ? AND assn = ? AND netid = ? ;";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, delivery.getStatus().toString(),delivery.getLog(),
            delivery.getTime(),delivery.getAssessment().getSubject(),delivery.getAssessment().getNumber(),
            delivery.getAssessment().getAssn(),delivery.getStudent().getNetId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deliverAssn(Delivery delivery) throws SQLException{
        String sql = "INSERT INTO adtg.assn_deadline (c_subject, c_number, assn, netid, due)"+
        "VALUES (?, ?, ?, ?, ?) ;";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, delivery.getAssessment().getSubject(),delivery.getAssessment().getNumber(),
            delivery.getAssessment().getAssn(),delivery.getStudent().getNetId(),delivery.getAssessment().getDueDate());
            ps.executeUpdate();
        }
    }

    public Boolean checkDelivery(Assessment assessment,Student student) throws SQLException{
        String sql = "SELECT * from adtg.assn_deadline where c_subject = ? AND c_number = ? AND assn = ? AND netid = ? ;";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, assessment.getSubject(),assessment.getNumber(),assessment.getAssn(),student.getNetId());
            ps.execute();
            ResultSet rs = ps.getResultSet();
            if(rs.next()){
                return true;
            }
            return false;
        }
    }

    public Delivery map(ResultSet rs) throws SQLException{
        StudentDAO studentDAO = new StudentDAO(conn);
        AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
        Student student = new Student(rs.getString("netid"));
        studentDAO.getStudentByNetid(student);
        Assessment assessment = assessmentDAO.getAssessmentByPK(new Course(rs.getString("c_subject"), rs.getInt("c_number")), 
        rs.getString("assn"));
        Delivery delivery = new Delivery(rs.getTimestamp("delivery_time").toLocalDateTime(), Status.fromString(rs.getString("status")), 
        rs.getString("log_text"), student, assessment);
        return delivery;
    }

    public List<Delivery> getDelivery(){
        String sql = "SELECT * from adtg.delivery;";
        List<Delivery> deliveries = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
            ResultSet rs = ps.getResultSet();
            while(rs.next()){
                deliveries.add(map(rs));
            }
            return deliveries;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    


    public void insertDeliveries(Assessment assn, List<String> students, LocalDateTime deliveryTime) throws SQLException {
        String sql = "INSERT INTO adtg.delivery (delivery_time, c_subject, c_number, assn, netid, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false); // Disable auto-commit mode
    
            try {
                for (String student_netid : students) {
                    ps.setTimestamp(1, Timestamp.valueOf(deliveryTime));
                    ps.setString(2, assn.getCourse().getSubject());
                    ps.setInt(3, assn.getCourse().getNumber());
                    ps.setString(4, assn.getAssn());
                    ps.setString(5, student_netid);
                    ps.setString(6, "INIT");
                    ps.addBatch();
                }
                
                ps.executeBatch();
                connection.commit(); 
                logger.info("Successfully inserted initial delivery rows.");
            } catch (SQLException e) {
                connection.rollback(); 
                logger.error("Error during batch execution. Transaction rolled back.", e);
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Error inserting delivery rows", e);
            throw e;
        }
    }
    

}

