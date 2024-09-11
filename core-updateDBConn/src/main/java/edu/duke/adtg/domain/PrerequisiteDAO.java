package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



@Repository
public class PrerequisiteDAO implements DAOFactory<Assessment>{

    private final DAOConn conn;
    private final AssessmentDAO asmDAO;
    private static final Logger logger = LoggerFactory.getLogger(AssessmentDAO.class);

    @Autowired
    public PrerequisiteDAO(DAOConn conn){
        this.conn = conn;
        this.asmDAO = new AssessmentDAO(conn);
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


    // ----------------------------- Helper --------------------------------------------------


    public Assessment createAssessmentFromRS(ResultSet rs) throws SQLException {
        Course course = new Course(rs.getString("pre_c_subject").trim(), rs.getInt("pre_c_number"));
        return new Assessment(course, rs.getString("pre_assn"));
    }



    public boolean checkPrerequisiteExists(Assessment asses, Assessment PreAsses) throws SQLException {
        String sql = "SELECT 1 FROM adtg.prerequisite WHERE c_subject = ? AND c_number = ? AND assn = ? AND pre_c_subject = ? AND pre_c_number = ? AND pre_assn = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, asses.getCourse().getSubject());
            ps.setInt(2, asses.getCourse().getNumber());
            ps.setString(3, asses.getAssn());
            ps.setString(4, PreAsses.getCourse().getSubject());
            ps.setInt(5, PreAsses.getCourse().getNumber());
            ps.setString(6, PreAsses.getAssn());
    
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ----------------------------- CRUD -----------------------------------------------


    public List<Assessment> getPrereq(Assessment assessment) throws SQLException{
        String sql = "SELECT pre_c_subject, pre_c_number, pre_assn " +
        "FROM adtg.prerequisite WHERE c_subject = ? AND c_number = ? AND assn = ?;";
        List<Assessment> prereq = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getSubject());
            ps.setInt(2, assessment.getNumber());
            ps.setString(3, assessment.getAssn());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assessment assessmentNew = createAssessmentFromRS(rs);
                    prereq.add(assessmentNew);
                }
            }
        }
        return prereq;
    }


    public List<Assessment> getAssessmentWithoutPrereq(Course course) throws SQLException{
        List<Assessment> all = asmDAO.getAllAssessmentsForCourse(course);
        List<Assessment> noPrereq = new ArrayList<>();
        for(Assessment assm:all){
            // System.out.println(assm.getSubject()+assm.getNumber()+assm.getAssn());
            List<Assessment> prereq = getPrereq(assm);
            // System.out.println(prereq.size());
            if(prereq.size()==0){
                noPrereq.add(assm);
            }
        }
        return noPrereq;
    }

    public void insertPrerequisite(Assessment asses, Assessment PreAsses) throws SQLException, IllegalStateException {
        if (checkPrerequisiteExists(asses, PreAsses)) {
            throw new IllegalStateException("Prerequisite already exists.");
        }
    
        String sql = "INSERT INTO adtg.prerequisite (c_subject, c_number, assn, pre_c_subject, pre_c_number, pre_assn) VALUES (?, ?, ?, ?, ?, ?)";
    
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, asses.getCourse().getSubject());
            ps.setInt(2, asses.getCourse().getNumber());
            ps.setString(3, asses.getAssn());
            ps.setString(4, PreAsses.getCourse().getSubject());
            ps.setInt(5, PreAsses.getCourse().getNumber());
            ps.setString(6, PreAsses.getAssn());
    
            ps.executeUpdate();
        } 
    }
        


    public void addPrerequisiteList(List<Prerequisite> prerequisites) throws SQLException {

        String sql = "INSERT INTO adtg.prerequisite (c_subject, c_number, assn, pre_c_subject, pre_c_number, pre_assn) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
    
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
    
            try {
                for (Prerequisite prerequisite : prerequisites) {
                    ps.setString(1, prerequisite.getAssn().getCourse().getSubject());
                    ps.setInt(2, prerequisite.getAssn().getCourse().getNumber());
                    ps.setString(3, prerequisite.getAssn().getAssn());
                    ps.setString(4, prerequisite.getPreAssn().getCourse().getSubject());
                    ps.setInt(5, prerequisite.getPreAssn().getCourse().getNumber());
                    ps.setString(6, prerequisite.getPreAssn().getAssn());
                    ps.addBatch();
                }
    
                ps.executeBatch();
                connection.commit();
                logger.info("Successfully added prerequisites.");
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Error during batch execution. Transaction rolled back.", e);
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Error adding prerequisites", e);
            throw e;
        }
    }
    
    

    
}
