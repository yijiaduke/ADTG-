package edu.duke.adtg.domain;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Repository;

@Repository
public class AssessmentDAO implements DAOFactory<Assessment>{
    
    private DAOConn conn;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(AssessmentDAO.class);

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
        Category category = new Category(rs.getString("category"), new CategoryDAO(conn).getPenaltyFormula(rs.getString("category")));
        Course course = new Course(rs.getString("c_subject").trim(), rs.getInt("c_number"));
        return new Assessment(
            course,
            rs.getString("assn"),
            rs.getString("title"),
            rs.getTimestamp("start_date").toLocalDateTime(),
            rs.getTimestamp("due").toLocalDateTime(),
            rs.getInt("token_req"),
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
        assessment.setTokenReq(rs.getInt("token_req"));
        assessment.setMaxScore(rs.getBigDecimal("max_score"));
        assessment.setPassScore(rs.getBigDecimal("passing_score"));
        assessment.setExtraCredit(rs.getBoolean("is_extra_credit"));
        assessment.setTestCmd(rs.getString("test_cmd"));

        Category category = new Category(rs.getString("category"), new CategoryDAO(conn).getPenaltyFormula(rs.getString("category")));
        assessment.setCategory(category);
    }


    public boolean checkAssessmentExists(Course course, String assn) throws SQLException {
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

    
    public boolean checkAssessmentsExistForCourse(Course course) throws SQLException {
        String sql = "SELECT 1 FROM adtg.assessment WHERE c_subject = ? AND c_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    


    public void load(Assessment assessment) throws SQLException{
        assessment = getAssessmentByPK(new Course(assessment.getSubject(), assessment.getNumber()), assessment.getAssn());
    }


    public Boolean pass(Grade grade){
        if(grade==null){
            return false;
        }
        String sql = "select passing_score from adtg.assessment where c_subject = ? and c_number =? and assn = ? ;";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, grade.getAssessment().getSubject());
            ps.setInt(2, grade.getAssessment().getNumber());
            ps.setString(3, grade.getAssessment().getAssn());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal pScore = rs.getBigDecimal("passing_score");
                    if (grade.getAssignmentGrade().compareTo(pScore) >= 0) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while retrieving section", e);
        }
        return false;
    }

    public Boolean pass(Student student,Assessment assessment) throws SQLException{
        GradeDAO gradeDAO = new GradeDAO(conn);
        Course course = new Course(assessment.getSubject(), assessment.getNumber());
        Grade grade = gradeDAO.getPassingGradeForStudet(student.getNetId(), course, assessment.getAssn());
        return pass(grade);
    }

    

    //------------------------------CRUD------------------------------------------------------


    // public Pair<String, Assessment> getNextAssessment(String status, String owner) throws SQLException {
    //     // Find the assessment by course and assessment name
    //     String sql = "SELECT * FROM adtg.grade_request WHERE status = ?";
    //     Assessment assessment = null;
    //     String student = null;
    //     Pair<String, Assessment> result = new Pair<>();
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, status);
    //         if (owner != null) {
    //             ps.setString(2, owner);
    //         }
    
    //         try (ResultSet rs = ps.executeQuery()) {
    //             if (rs.next()) {
    //                 System.out.println(rs);
    //                 assessment = getAssessment(rs.getString("c_subject").trim(), rs.getInt("c_number"), rs.getString("assn"));
    //                 student = rs.getString("netid");
    //                 result = new Pair<String, Assessment>(student, assessment);
    //                 return result;
    //             }
    //         } catch (SQLException e) {
    //             e.printStackTrace(); // It's usually better to handle exceptions more gracefully
    //             throw e;
    //         }
    //     }
    //     return result;
    // }

    // public Boolean isThereAssessmentToGrade(String status) throws SQLException {
    //     return (getNextAssessment(status, null) != null);
    // }


    

    
  

    public Map<String, Integer> countAssessmentsByCategory(Course course) throws SQLException {
        String sql = "SELECT category, COUNT(*) AS count " +
                       "FROM adtg.assessment " +
                       "WHERE c_subject = ? AND c_number = ? " +
                       "GROUP BY category";

        Map<String, Integer> categoryCountMap = new HashMap<>();

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String category = rs.getString("category");
                    int count = rs.getInt("count");
                    categoryCountMap.put(category, count);
                }
            }
        }

        return categoryCountMap;
    }



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
        



    public Assessment getAssessment(String c_subject, Integer c_number, String assessment) throws SQLException {
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


    public List<Assessment> getAllAssessmentsForCourse(Course course) throws SQLException {
        List<Assessment> assessments = new ArrayList<>();
        String courseSubject = course.getSubject().trim();
        int courseNumber = course.getNumber();
    
        String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? ORDER BY due ASC";
    
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

    public List<Assessment> listPreAssessments(Assessment assessment) throws SQLException {
        List<Assessment> preAsses = new ArrayList<>();
    
        String sql = "SELECT prerequisite.*, assessment.title " +
                     "FROM adtg.prerequisite " +
                     "JOIN adtg.assessment ON prerequisite.pre_c_subject = assessment.c_subject " +
                     "AND prerequisite.pre_c_number = assessment.c_number " +
                     "AND prerequisite.pre_assn = assessment.assn " +
                     "WHERE prerequisite.c_subject = ? " +
                     "AND prerequisite.c_number = ? " +
                     "AND prerequisite.assn = ? " +
                     "ORDER BY prerequisite.c_subject ASC, prerequisite.c_number ASC, prerequisite.assn ASC";
    
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
                    String title = rs.getString("title"); // Get the title from the assessment table
    
                    Course course = new Course(cSubject, cNumber);
                    Assessment preAssessment = new Assessment(course, assn, title); 
                    preAsses.add(preAssessment);
                }
            }
        }
        return preAsses;
    }
    

    public List<String> getCategoriesForCourse(Course course) throws SQLException {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT category FROM adtg.assessment WHERE c_subject = ? AND c_number = ? ORDER BY category ASC ";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    categories.add(rs.getString("category"));
                }
            }
        }
        return categories;
    }


    public void updateAssnTestCmd(Assessment assessment) throws SQLException {
        String sql = "UPDATE adtg.assessment SET test_cmd = ? WHERE c_subject = ? AND c_number = ? AND assn = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, assessment.getTestCmd());
            ps.setString(2, assessment.getCourse().getSubject());
            ps.setInt(3, assessment.getCourse().getNumber());
            ps.setString(4, assessment.getAssn());
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
                throw new SQLException("Failed to update Test Command for assessment: " + assessment.getAssn());
            }
        }
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
        


    public void addAssessmentList(List<Assessment> assessments) throws SQLException {
        String sql = "INSERT INTO adtg.assessment (c_subject, c_number, assn, title, start_date, due, category, max_score, passing_score, is_extra_credit, test_cmd, token_req) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try {
                for (Assessment assessment : assessments) {
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
                    ps.setInt(12, assessment.getTokenReq() != null ? assessment.getTokenReq() : 0); // Default to 0 if null
                    
                    ps.addBatch(); // Add to batch
                }
                
                ps.executeBatch(); // Execute the batch
                connection.commit(); // Commit 
                logger.info("Successfully added assessments.");
            } catch (SQLException e) {
                connection.rollback(); // Rollback the transaction in case of error
                logger.error("Error during batch execution. Transaction rolled back.", e);
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Error adding assessments", e);
            throw e;
        }
    }
    
    
 //---------------------------------no use ---------------------------------------------

  
    // //before checking start data
    // public List<Assessment> getAssessmentsInTwoWeeks(Course course) throws SQLException {
    //     List<Assessment> assessments = new ArrayList<>();
    //     String courseSubject = course.getSubject().trim();
    //     int courseNumber = course.getNumber();
    
    //     // filter assessments by course and due date within the next two weeks
    //     String sql = """
    //         SELECT * FROM adtg.assessment
    //         WHERE c_subject = ? AND c_number = ? AND due BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + INTERVAL '14 days' ORDER BY due ASC;
    //     """;
    
    //     // Use try-with-resources to ensure that resources are closed after the program is finished
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, courseSubject);
    //         ps.setInt(2, courseNumber);
            
    //         try (ResultSet rs = ps.executeQuery()) {
    //             while (rs.next()) {
    //                 Assessment assessment = createAssessmentFromRS_2(rs);
    //                 assessments.add(assessment);
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace(); // It's usually better to handle exceptions more gracefully
    //         throw e;
    //     }
    //     return assessments;
    // }

    


    // //get formative/evaluative assessments
    // public List<Assessment> getAssessmentsForCourseByCategory(Course course, String category) throws SQLException {
    //     List<Assessment> assessments = new ArrayList<>();
    //     String sql = "SELECT * FROM adtg.assessment WHERE c_subject = ? AND c_number = ? AND category = ?";

    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, course.getSubject());
    //         ps.setInt(2, course.getNumber());
    //         ps.setString(3, category);
    
    //         try (ResultSet rs = ps.executeQuery()) {
    //             while (rs.next()) {
    //                 Assessment assessment = createAssessmentFromRS_2(rs);
    //                 assessments.add(assessment);
    //             }
    //         }
    //     }
    //     return assessments;
    // }

     
        // /**
        //  * Sets the grading status of an assessment for a specific student to N (graded)
        //  *
        //  * @param assessment The assessment object representing the assessment to update.
        //  * @param netId The network ID of the student.
        //  */
        // public void setAssnGradingStatus(Assessment assessment, String netId, String status) {
        //     String sql = "UPDATE adtg.assn_deadline SET assn_to_grade = ? WHERE c_subject = ? AND c_number = ? AND netid = ?";
            
        //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
        //         ps.setString(1, status);
        //         ps.setString(2, assessment.getCourse().getSubject());
        //         ps.setInt(3, assessment.getCourse().getNumber());
        //         ps.setString(4, netId);
        //         ps.executeUpdate();
        //     } catch (SQLException e) {
        //         e.printStackTrace(); // It's usually better to handle exceptions more gracefully
        //         throw new RuntimeException(e);
        //     }
        // }




    // // Get all grades for a student for an assessment from latest version to oldest 
    // public List<Grade> getGradesForStudentAssn(String netId, String courseSubject, int courseNumber, String assn) throws SQLException {
    //     List<Grade> grades = new ArrayList<>();
    //     String sql = "SELECT * FROM adtg.grades WHERE netid = ? AND c_subject = ? AND c_number = ? AND assn = ? ORDER BY grade_time DESC";
        
    //     try (
    //          PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, netId);
    //         ps.setString(2, courseSubject);
    //         ps.setInt(3, courseNumber);
    //         ps.setString(4, assn);
    
    //         ResultSet rs = ps.executeQuery(); 
    //             while (rs.next()) {
    //                 Grade grade = GradeDAO.createGradeFromRS(rs);
    //                 grades.add(grade);
    //             }
    //     }
    //     return grades;
    // }
    


    // public Grade getLatestGradeForStudentAssn(String netId, String courseSubject, int courseNumber, String assn) throws SQLException {
    //     Grade grade = null;
    //     String sql = """
    //         SELECT g.*
    //         FROM adtg.grades g
    //         WHERE g.netid = ? AND g.c_subject = ? AND g.c_number = ? AND g.assn = ?
    //           AND g.grade_time = (
    //             SELECT MAX(grade_time)
    //             FROM adtg.grades
    //             WHERE netid = g.netid AND c_subject = g.c_subject AND c_number = g.c_number AND assn = g.assn
    //           );
    //         """;
    
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, netId);
    //         ps.setString(2, courseSubject);
    //         ps.setInt(3, courseNumber);
    //         ps.setString(4, assn);
    
    //         try (ResultSet rs = ps.executeQuery()) {
    //             if (rs.next()) {
    //                 grade = GradeDAO.createGradeFromRS(rs);
    //             }
    //         }
    //     }
    //     return grade;
    // }

    

    // public List<Assessment> getPastDueAssessments(String netId, String courseSubject, int courseNumber, String assn) throws SQLException {
    //     List<Assessment> assessments = new ArrayList<>();

    //     String sql = """
    //         WITH LatestGrades AS (
    //             SELECT
    //                 g.c_subject,
    //                 g.c_number,
    //                 g.assn,
    //                 g.netid,
    //                 MAX(g.grade_time) as LatestGradeTime
    //             FROM adtg.grades g
    //             WHERE g.netid = ? AND g.c_subject = ? AND g.c_number = ? AND g.assn = ?
    //             GROUP BY g.c_subject, g.c_number, g.assn, g.netid
    //         ),
    //         LatestGradesDetails AS (
    //             SELECT
    //                 lg.c_subject,
    //                 lg.c_number,
    //                 lg.assn,
    //                 g.final_grade,
    //                 g.grade_time,
    //                 a.passing_score,
    //                 a.due,
    //                 a.category,
    //                 a.title,
    //                 a.start_date,
    //                 a.max_score
    //             FROM LatestGrades lg
    //             JOIN adtg.grades g ON lg.c_subject = g.c_subject AND lg.c_number = g.c_number AND lg.assn = g.assn AND lg.netid = g.netid AND lg.LatestGradeTime = g.grade_time
    //             JOIN adtg.assessment a ON lg.c_subject = a.c_subject AND lg.c_number = a.c_number AND lg.assn = a.assn
    //             WHERE g.final_grade < a.passing_score AND a.due < CURRENT_TIMESTAMP
    //         )
    //         SELECT
    //             l.c_subject,
    //             l.c_number,
    //             l.assn,
    //             l.final_grade,
    //             l.grade_time,
    //             l.passing_score,
    //             l.due,
    //             l.category,
    //             l.title,
    //             l.start_date,
    //             l.max_score
    //         FROM LatestGradesDetails l
    //         ORDER BY l.due DESC;

    //         """;

    //         try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //             ps.setString(1, netId);
    //             ps.setString(2, courseSubject);
    //             ps.setInt(3, courseNumber);
    //             ps.setString(4, assn);
            
    //             try (ResultSet rs = ps.executeQuery()) {
    //                 while (rs.next()) {

    //                     Assessment assessment = createAssessmentFromRS_2(rs);
    //                     assessments.add(assessment);
    //                 }
    //             }
    //         }
    //         return assessments;
    //     }

    // /**
    //  * Sets the grading status of an assessment for a specific student to N (graded)
    //  *
    //  * @param assessment The assessment object representing the assessment to update.
    //  * @param netId The network ID of the student.
    //  */
    // public void setAssnGradingStatus(Assessment assessment, String netId) {
    //     String sql = "UPDATE adtg.assn_deadline SET assn_to_grade = ? WHERE c_subject = ? AND c_number = ? AND netid = ?";
    //     try (PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, "N");
    //         ps.setString(2, assessment.getCourse().getSubject());
    //         ps.setInt(3, assessment.getCourse().getNumber());
    //         ps.setString(4, netId);
    //         ps.executeUpdate();
    //     } catch (SQLException e) {
    //         e.printStackTrace(); // It's usually better to handle exceptions more gracefully
    //         throw new RuntimeException(e);
    //     }
    // }


}

