package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class GradeDAO implements DAOFactory<Student> {

    private final DAOConn conn;

    @Autowired
    public GradeDAO(DAOConn conn) {
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



    // ----------------------------- Helper --------------------------------------------------

    public Grade createGradeFromRS(ResultSet rs) throws SQLException {
        AssessmentDAO assessmentDAO = new AssessmentDAO(conn);
        Student student = new Student(rs.getString("netid"));
        Assessment assessment = assessmentDAO.createAssessmentFromRS(rs);
        return new Grade(
            assessment,
            student,
            rs.getTimestamp("grade_time").toLocalDateTime(),
            rs.getBigDecimal("assn_grade"),
            rs.getBigDecimal("penalty"),
            rs.getBigDecimal("final_grade"),
            rs.getString("log_text")
        );
    }
    

    
    // ----------------------------- CRUD -----------------------------------------------

    public void updateFinalGrade(Grade grade) throws SQLException {
        String upsertSQL = "INSERT INTO adtg.final_grade (c_subject, c_number, assn, netid, grade) " +
                           "VALUES (?, ?, ?, ?, ?) " +
                           "ON CONFLICT (assn, c_subject, c_number, netid) " +
                           "DO UPDATE SET grade = EXCLUDED.grade ;";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(upsertSQL)) {                  
            setStatementObjects(ps, grade.getAssessment().getSubject(), grade.getAssessment().getNumber(), grade.getAssessment().getAssn(),
                                grade.getStudent().getNetId(), grade.getFinalGrade());
            ps.executeUpdate();
        }
    }

    
     public Grade getGradeByPK(Assessment assessment, String netId, LocalDateTime gradeTime) throws SQLException {
        String sql = "SELECT * FROM adtg.grades WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ? AND grade_time = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());
            ps.setString(4, netId);
            ps.setTimestamp(5, java.sql.Timestamp.valueOf(gradeTime));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createGradeFromRS(rs);
                }
            }
        }
        return null;
    }

    public Grade getLatestGradeForStudentAssn(String netId, Course course, String assn) throws SQLException {
        Grade grade = null;

        String sql = """
            SELECT * FROM adtg.grades g
            WHERE g.netid = ? AND g.c_subject = ? AND g.c_number = ? AND g.assn = ? AND g.grade_time = (
                SELECT MAX(grade_time)
                FROM adtg.grades
                WHERE netid = g.netid AND c_subject = g.c_subject AND c_number = g.c_number AND assn = g.assn
              );
            """;
    
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, course.getSubject().trim());
            ps.setInt(3, course.getNumber());
            ps.setString(4, assn);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    grade = createGradeFromRS(rs);
                }
            }
        }
        return grade;
    }




    public Grade getPassingGradeForStudet(String netId, Course course, String assn) throws SQLException {
        Grade grade = null;
        String courseSubject = course.getSubject().trim();
        int courseNumber = course.getNumber();

        String sql = """
            SELECT g.*
            FROM adtg.grades g
            WHERE g.netid = ? AND g.c_subject = ? AND g.c_number = ? AND g.assn = ?
              AND g.assn_grade = (
                SELECT MAX(assn_grade)
                FROM adtg.grades
                WHERE netid = g.netid AND c_subject = g.c_subject AND c_number = g.c_number AND assn = g.assn
              );
            """;
    
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, courseSubject);
            ps.setInt(3, courseNumber);
            ps.setString(4, assn);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    grade = createGradeFromRS(rs);
                }
            }
        }
        return grade;
    }

    
    // Get all grades for a student for an assessment from latest version to oldest 
    public List<Grade> listGradesForStudentAssn(String netId, Course course, String assn) throws SQLException {

        List<Grade> grades = new ArrayList<>();
        String sql = "SELECT * FROM adtg.grades WHERE netid = ? AND c_subject = ? AND c_number = ? AND assn = ? ORDER BY grade_time DESC";
        
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ps.setString(2, course.getSubject().trim());
            ps.setInt(3, course.getNumber());
            ps.setString(4, assn);
    
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grades.add(createGradeFromRS(rs));
                }
            }
        }
        return grades;
    }


    public void save(Grade grade) throws SQLException {
        String sql = "INSERT INTO adtg.grades (c_subject, c_number, assn, netid, grade_time, assn_grade, penalty, final_grade, log_text) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ;";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, grade.getAssessment().getSubject(), grade.getAssessment().getNumber(), grade.getAssessment().getAssn(), grade.getStudent().getNetId(), grade.getGradeTime(), grade.getAssignmentGrade(), grade.getPenalty(), grade.getFinalGrade(), grade.getLogText());
            // System.out.println(ps);
            ps.executeUpdate();
        }
    }

    public void remove(Grade grade) throws SQLException {
        String sql = "DELETE FROM adtg.grades " +
                 "WHERE c_subject = ? AND c_number = ? AND assn = ? AND netid = ? AND grade_time = ? ;";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, grade.getAssessment().getSubject(), grade.getAssessment().getNumber(), grade.getAssessment().getAssn(), grade.getStudent().getNetId(), grade.getGradeTime());
            ps.executeUpdate();
        }
    }
    

}
