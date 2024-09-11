package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PrerequisiteDAO implements DAOFactory<Assessment>{

    private DAOConn conn;
    private Connection connection;
    private AssessmentDAO asmDAO;

    public PrerequisiteDAO(DAOConn conn){
        this.conn = conn;
        connection = getConnection();
        asmDAO = new AssessmentDAO(conn);
    }
    
    @Override
    public void setConnection(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
        asmDAO = new AssessmentDAO(conn);
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

    
}

