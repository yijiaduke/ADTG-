package edu.duke.adtg.domain;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.math.BigDecimal;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

public class AssessmentDAO implements DAOFactory<Assessment>{
    
    private DAOConn conn;
    private Connection connection;
    // private static final Logger logger = LoggerFactory.getLogger(AssessmentDAO.class);

    public AssessmentDAO(DAOConn conn){
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



    public Assessment createAssessmentFromRS(ResultSet rs) throws SQLException {
        Course course = new Course(rs.getString("c_subject").trim(), rs.getInt("c_number"));
        return new Assessment(course, rs.getString("assn"));
    }


    public Assessment createAssessmentFromRS_2(ResultSet rs) throws SQLException {
        Category category = new Category(rs.getString("category"));
        Course course = new Course(rs.getString("c_subject").trim(), rs.getInt("c_number"));
        return new Assessment(
            course,
            rs.getString("assn"),
            rs.getString("title"),
            rs.getTimestamp("start_date").toLocalDateTime(),
            rs.getTimestamp("due").toLocalDateTime(),
            category,
            rs.getBigDecimal("max_score"),
            rs.getBigDecimal("passing_score"),
            rs.getBoolean("is_extra_credit"),
            rs.getString("test_cmd")
        );
    }


    public void mapAssessmentFromRS(ResultSet rs, Assessment assessment) throws SQLException {
        Course course = assessment.getCourse();
        course.setSubject(rs.getString("c_subject").trim());
        course.setNumber(rs.getInt("c_number"));
        assessment.setCourse(course);
        
        assessment.setAssn(rs.getString("assn"));
        assessment.setTitle(rs.getString("title"));
        assessment.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        assessment.setDueDate(rs.getTimestamp("due").toLocalDateTime());
        assessment.setMaxScore(rs.getBigDecimal("max_score"));
        assessment.setPassScore(rs.getBigDecimal("passing_score"));
        assessment.setExtraCredit(rs.getBoolean("is_extra_credit"));
        assessment.setTestCmd(rs.getString("test_cmd"));
    
        Category category = new Category(rs.getString("category"));
        assessment.setCategory(category);
    }


    private boolean checkAssessmentExists(Course course, String assn) throws SQLException {
        String sql = "SELECT 1 FROM adtg.assessment WHERE c_subject = ? AND c_number = ? AND assn = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            ps.setString(3, assn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } 
    }

    public Assessment getAssessment(String c_subject, int c_number, String assessment) throws SQLException {
        // Find the assessment by course and assessment name
        String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? AND assn = ?";
        Assessment result = null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c_subject);
            ps.setInt(2, c_number);
            ps.setString(3, assessment);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = createAssessmentFromRS_2(rs);
                }
            } catch (SQLException e) {
                e.printStackTrace(); // It's usually better to handle exceptions more gracefully
                throw e;
            }
        }
        return result;
    }
    public Assessment getNextAssessment(String c_subject, int c_number, String netId) throws SQLException {
        // Find the assessment by course and assessment name
        String sql = "SELECT * FROM adtg.assn_deadline WHERE c_subject = ? AND c_number = ? AND netid = ? AND assn_to_grade = ?";
        Assessment result = null;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, c_subject);
            ps.setInt(2, c_number);
            ps.setString(3, netId);
            ps.setString(4, "Y");
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    result = getAssessment(c_subject, c_number, rs.getString("assn"));
                }
            } catch (SQLException e) {
                e.printStackTrace(); // It's usually better to handle exceptions more gracefully
                throw e;
            }
        }
        return result;
    }

    public boolean checkPrerequisiteExists(Assessment asses, Assessment PreAsses) throws SQLException {
        String sql = "SELECT 1 FROM adtg.prerequisite WHERE c_subject = ? AND c_number = ? AND assn = ? AND pre_c_subject = ? AND pre_c_number = ? AND pre_assn = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
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
    
    /**
     * Sets the grading status of an assessment for a specific student to N (graded)
     *
     * @param assessment The assessment object representing the assessment to update.
     * @param netId The network ID of the student.
     */
    public void setAssnGradingStatus(Assessment assessment, String netId, String status) {
        String sql = "UPDATE adtg.assn_deadline SET assn_to_grade = ? WHERE c_subject = ? AND c_number = ? AND netid = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, assessment.getCourse().getSubject());
            ps.setInt(3, assessment.getCourse().getNumber());
            ps.setString(4, netId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace(); // It's usually better to handle exceptions more gracefully
            throw new RuntimeException(e);
        }
    }

    //------------------------------CRUD------------------------------------------------------


    //before checking start data
    public List<Assessment> listAssessmentsForCourse(Course course) throws SQLException {
        List<Assessment> assessments = new ArrayList<>();
        String courseSubject = course.getSubject().trim();
        int courseNumber = course.getNumber();

        String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? ORDER BY category ASC, assn ASC, due ASC";
        try (Connection connection = getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) { 
            ps.setString(1, courseSubject);
            ps.setInt(2, courseNumber);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Assessment assessment = createAssessmentFromRS_2(rs);
                    assessments.add(assessment);
                }
            }
        }
        return assessments;
    }


    // Get assessment by primary key
    public Assessment getAssessmentByPK(Course course, String assn) throws SQLException {
        String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? AND assn = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            ps.setString(3, assn);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createAssessmentFromRS_2(rs);
                } else {
                    return null;
                }
            }
        }
    }
        

    public void loadAssessmentByPK(Assessment assessment) throws SQLException {
        String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? AND assn = ?";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mapAssessmentFromRS(rs, assessment);
                } else {
                    throw new SQLException("Assessment not found");
                }
            }
        }
    }


    public List<Assessment> listPreAssessments(Assessment assessment) throws SQLException {
        List<Assessment> preAsses = new ArrayList<>();

        String sql = "SELECT * FROM adtg.prerequisite WHERE c_subject = ? AND c_number = ? AND assn = ? ORDER BY c_subject ASC, c_number ASC, assn ASC";

        try (Connection connection = getConnection(); 
             PreparedStatement ps = connection.prepareStatement(sql)) { 
            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String cSubject = rs.getString("pre_c_subject");
                    int cNumber = rs.getInt("pre_c_number");
                    String assn = rs.getString("pre_assn");

                    Course course = new Course(cSubject, cNumber);
                    Assessment preAssessment = new Assessment(course, assn);
                    preAsses.add(preAssessment);
                }
            }
        }
        return preAsses;
    }



 

        
    public void insertAssessment(Assessment assessment) throws SQLException, IllegalStateException {
        if (checkAssessmentExists(assessment.getCourse(), assessment.getAssn())) {
            throw new IllegalStateException("Assessment with assn " + assessment.getAssn() + " already exists.");
        }
    
        String sql = "INSERT INTO adtg.assessment (c_subject, c_number, assn, title, start_date, due, category, max_score, passing_score, is_extra_credit, test_cmd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getCourse().getSubject());
            ps.setInt(2, assessment.getCourse().getNumber());
            ps.setString(3, assessment.getAssn());
            ps.setString(4, assessment.getTitle());
            ps.setTimestamp(5, Timestamp.valueOf(assessment.getStartDate()));
            ps.setTimestamp(6, Timestamp.valueOf(assessment.getDueDate()));
            ps.setString(7, assessment.getCategory().getName());
            ps.setBigDecimal(8, assessment.getMaxScore());
            ps.setBigDecimal(9, assessment.getPassScore());
            ps.setBoolean(10, assessment.isExtraCredit());
            ps.setString(11, assessment.getTestCmd());
            ps.executeUpdate();
        } 
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
        




}

