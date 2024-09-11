package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class EnrollmentDAO implements  DAOFactory<Enrollment>{
    
    private DAOConn conn;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(EnrollmentDAO.class);

    public EnrollmentDAO(DAOConn conn) {
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

    // ----------------------------- Helper --------------------------------------------------


    public Student mapStudent(ResultSet rs) throws SQLException{
        StudentDAO studentDAO = new StudentDAO(conn);
        Student student = new Student(rs.getString("student_netid"));
        studentDAO.getStudentByNetid(student);
        return student;
    }

  

    public Section getSection(Course course,Student student) throws SQLException{
        SectionDAO sectionDAO = new SectionDAO(conn);
        String sql = "select * from adtg.enrollment where  c_subject = ? and c_number = ? and student_netid = ?;";
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            ps.setString(3, student.getNetId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Integer sec_id = rs.getInt("sec_id");
                Section section = sectionDAO.getSectionsByPK(course.getSubject(), course.getNumber(), sec_id);
                return section;
            }
        }catch(SQLException e){
            e.printStackTrace();
            throw e;
        }
        return null;
    }   


    public boolean checkEnrollmentExists(Course course, String netId) throws SQLException {
        String sql = "SELECT 1 FROM adtg.enrollment WHERE c_subject = ? AND c_number = ? AND student_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            ps.setString(3, netId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    
     public boolean checkEnrollmentsExistForCourse(Course course) throws SQLException {
        String sql = "SELECT 1 FROM adtg.enrollment WHERE c_subject = ? AND c_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
    

    private Enrollment createEnrollmentFromRS(ResultSet rs) throws SQLException {
        String subject = rs.getString("c_subject");
        int number = rs.getInt("c_number");
        int sectionId = rs.getInt("sec_id");
        String netId = rs.getString("student_netid");
        Integer tokensAvail = rs.getInt("tokens_avail");
    
        // Create the Course and Section objects
        Course course = new Course();
        course.setSubject(subject);
        course.setNumber(number);
    
        Section section = new Section();
        section.setCourse(course);
        section.setSectionId(sectionId);
    
        return new Enrollment(section, netId, tokensAvail);
    }

    // ----------------------------- CRUD -----------------------------------------------


    public Enrollment getEnrollmentByPK(Section section, String netId) throws SQLException {
        String sql = "SELECT * FROM adtg.enrollment WHERE c_subject = ? AND c_number = ? AND sec_id = ? AND student_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            ps.setString(4, netId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return createEnrollmentFromRS(rs);
                }
            }
        }
        return null;
    }

    //load the section with field "enrollStudents"
    public void loadBySection(Section section) throws SQLException {
        StudentDAO studentDAO = new StudentDAO(conn);

        String sql = "SELECT * FROM adtg.enrollment WHERE c_subject = ? AND c_number = ? AND sec_id = ?";
        List<Student> students = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Student student = studentDAO.getStudentByPK(rs.getString("student_netid"));
                    students.add(student);
                }
            }
        }
        section.setEnrollStudents(students);
    }   
 


    public void addEnrollment(Section section, String netId) throws SQLException, IllegalStateException {
        UserDAO userDAO = new UserDAO(conn);

        // Step 1: Check if the user exists
        if (!userDAO.checkUserExists(netId)) {
            throw new IllegalStateException("User with NetID " + netId + " does not exist.");
        }

        // Step 2: Check if the user is a student
        if (!userDAO.checkUserRole(netId, "STUDENT")) {
            throw new IllegalStateException("User with NetID " + netId + " is not authorized as STUDENT.");
        }

        // Step 3: Check if the student is already enrolled in any section of the course
        if (checkEnrollmentExists(section.getCourse(), netId)) {
            throw new IllegalStateException("Enrollment already exists for NetID " + netId + 
                " in course " + section.getCourse().getSubject() + " " + section.getCourse().getNumber());
        }

        // Step 4: Add the student to enrollment
        String sql = "INSERT INTO adtg.enrollment (c_subject, c_number, sec_id, student_netid) VALUES (?,?,?,?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            ps.setString(4, netId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Adding enrollment failed, no rows affected.");
            }
        }
    }

    // For admin portal, massively load the enrollments in sections
    public void addEnrollments(List<Enrollment> enrollments) throws SQLException {
        String enrollmentSql = "INSERT INTO enrollment (c_subject, c_number, sec_id, student_netid, tokens_avail) VALUES (?, ?, ?, ?, ?)";
        String enrollmentSqlNoTokens = "INSERT INTO enrollment (c_subject, c_number, sec_id, student_netid) VALUES (?, ?, ?, ?)";
    
        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
    
            try (PreparedStatement enrollmentPs = connection.prepareStatement(enrollmentSql);
                 PreparedStatement enrollmentPsNoTokens = connection.prepareStatement(enrollmentSqlNoTokens)) {
    
                for (Enrollment enrollment : enrollments) {
                    logger.debug("DAO!!!!: enrollment:{}", enrollment.getStudentNetId(),enrollment.getTokensAvail());
                    if (enrollment.getTokensAvail() != null) {
                        enrollmentPs.setString(1, enrollment.getSection().getCourse().getSubject());
                        enrollmentPs.setInt(2, enrollment.getSection().getCourse().getNumber());
                        enrollmentPs.setInt(3, enrollment.getSection().getSectionId());
                        enrollmentPs.setString(4, enrollment.getStudentNetId());
                        enrollmentPs.setInt(5, enrollment.getTokensAvail());
                        enrollmentPs.addBatch();
                    } else {
                        enrollmentPsNoTokens.setString(1, enrollment.getSection().getCourse().getSubject());
                        enrollmentPsNoTokens.setInt(2, enrollment.getSection().getCourse().getNumber());
                        enrollmentPsNoTokens.setInt(3, enrollment.getSection().getSectionId());
                        enrollmentPsNoTokens.setString(4, enrollment.getStudentNetId());
                        enrollmentPsNoTokens.addBatch();
                    }
                }

                enrollmentPs.executeBatch();
                enrollmentPsNoTokens.executeBatch();
                connection.commit();
                logger.info("Successfully added enrollments.");
            } catch (SQLException e) {
                connection.rollback();
                logger.error("Error during batch execution. Transaction rolled back.", e);
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            logger.error("Error adding enrollments", e);
            throw e;
        }
    }




}
