package edu.duke.adtg.domain;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AssnDeadlineDAO implements DAOFactory<AssnDeadline>{
    
    private DAOConn conn;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(AssnDeadlineDAO.class);


    public AssnDeadlineDAO(DAOConn conn){
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


    //--------------------------------- Helper ------------------------------------------------

    private void mapAssnDeadlineFromRS(ResultSet rs, AssnDeadline assnDeadline) throws SQLException {
    
        String cSubject = rs.getString("c_subject");
        int cNumber = rs.getInt("c_number");
        String assn = rs.getString("assn");
        String netId = rs.getString("netid");
        LocalDateTime due = rs.getTimestamp("due").toLocalDateTime();
    
        Course course = new Course(cSubject, cNumber);
        Assessment assessment = new Assessment(course, assn);
        Student student = new Student(netId);
    
        // Set values
        assnDeadline.setAssessment(assessment);
        assnDeadline.setStudent(student);
        assnDeadline.setDue(due);
    }
    

    public AssnDeadline getAssnDeadlineByPK(Assessment assessment, String netId) throws SQLException {
        String sql = "SELECT * FROM adtg.assn_deadline WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());
            ps.setString(4, netId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AssnDeadline assnDeadline = new AssnDeadline();
                    mapAssnDeadlineFromRS(rs, assnDeadline);
                    return assnDeadline;
                }
            }
        }
        return null; // Return null if no record is found
    }

    // Check if an AssnDeadline exists
    public boolean checkAssnDeadlineExists(AssnDeadline assnDeadline) throws SQLException {
        String sql = "SELECT 1 FROM adtg.assn_deadline WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assnDeadline.getAssessment().getCourse().getSubject());
            ps.setInt(2, assnDeadline.getAssessment().getCourse().getNumber());
            ps.setString(3, assnDeadline.getAssessment().getAssn());
            ps.setString(4, assnDeadline.getStudent().getNetId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }




    //----------------------------------- CRUD -------------------------------------------------

    
    
    
    //load the whole assnDeadline by pk (assessment, netId)
    public void loadAssnDeadlineByPK(AssnDeadline assnDeadline) throws SQLException {
        String sql = "SELECT * FROM adtg.assn_deadline WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assnDeadline.getAssessment().getCourse().getSubject());
            ps.setInt(2, assnDeadline.getAssessment().getCourse().getNumber());
            ps.setString(3, assnDeadline.getAssessment().getAssn());
            ps.setString(4, assnDeadline.getStudent().getNetId());
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mapAssnDeadlineFromRS(rs, assnDeadline);
                }
            }
        }
    }
    

    // get all assn_deadline for student in course
    public List<AssnDeadline> listAssnDeadlineByStudent(Course course, String netId) throws SQLException {
        List<AssnDeadline> allAssnDeadlines = new ArrayList<>();
        String sql = "SELECT * FROM adtg.assn_deadline WHERE c_subject = ? AND c_number = ? AND netid = ? ORDER BY due ASC";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            ps.setString(3, netId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AssnDeadline assnDeadline = new AssnDeadline();
                    mapAssnDeadlineFromRS(rs, assnDeadline);
                    allAssnDeadlines.add(assnDeadline);
                }
            }
        }
        return allAssnDeadlines;
    }



    // for webapp instructor portal give extension
    public void updateDue(AssnDeadline assnDeadline) throws SQLException {
        String sql = "UPDATE adtg.assn_deadline SET due = ? WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(assnDeadline.getDue()));
            ps.setString(2, assnDeadline.getAssessment().getCourse().getSubject());
            ps.setInt(3, assnDeadline.getAssessment().getCourse().getNumber());
            ps.setString(4, assnDeadline.getAssessment().getAssn());
            ps.setString(5, assnDeadline.getStudent().getNetId());
            ps.executeUpdate();
        }
    }
    

    
    public void remove(Assessment assessment,Student student) throws SQLException{
        String sql = "delete from adtg.assn_deadline where c_subject = ? and c_number = ? and assn = ? and netid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            setStatementObjects(ps, assessment.getSubject(),assessment.getNumber(),assessment.getAssn(),student.getNetId());
            ps.executeUpdate();
        }
    }

}
