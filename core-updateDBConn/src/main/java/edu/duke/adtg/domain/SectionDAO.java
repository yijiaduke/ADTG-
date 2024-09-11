package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




@Repository
public class SectionDAO implements DAOFactory<Section> {
    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(SectionDAO.class);

    @Autowired
    public SectionDAO(DAOConn conn) {
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


    // ----------------------------- Helper --------------------------------------------------


    public void map(ResultSet rs,Section section) throws SQLException {
        if (!rs.next()) {
            return;
        }
//        Course course = courseDAO.load();
//        Section section = new Section(rs.getString("sectionid"),course);
       mapInstructor(rs, section);

    }


    public void mapInstructor(ResultSet rs, Section section) throws SQLException {
        if (rs.getString("instructor_netid") != null && section != null) {
            Instructor instructor = new Instructor(
                    rs.getString("instructor_netid"), rs.getString("name"), rs.getString("email"));
            section.setInstructor(instructor);
        }
    }
    

    public void mapStudents(ResultSet rs,Section section) throws SQLException {
        if(rs.getString("netid")!=null && section!=null) {
            List<Student> enrolledStudents = new ArrayList<>();
            while (rs.next()) {
                Student student = new Student(
                        rs.getString("netid"),
                        rs.getString("email"),
                        rs.getString("name")
                );
                enrolledStudents.add(student);
            }
            section.setEnrollStudents(enrolledStudents);
        }

    }

    
    
    public Section mapSectionFromRS(ResultSet rs) throws SQLException {
        Course course = new Course(rs.getString("c_subject").trim(), rs.getInt("c_number"));
        Integer sectionId = rs.getInt("sec_id");
        LocalDate startDate = rs.getDate("startdate").toLocalDate();
        Instructor instructor = new Instructor(rs.getString("instructor_netid"));
        String gitlab_group = rs.getString("gitlab_group");
        String gitlab_token = rs.getString("gitlab_token");
    
        Section section = new Section(sectionId, course, startDate, instructor, gitlab_group, gitlab_token);
        return section;
    }



    public void mapSectionFromRS(ResultSet rs, Section section) throws SQLException {
        InstructorDAO instructorDAO = new InstructorDAO(conn);

        Course course = section.getCourse();
        if (course == null) {
            course = new Course();
            section.setCourse(course);
        }

        course.setSubject(rs.getString("c_subject").trim());
        course.setNumber(rs.getInt("c_number"));
    
        section.setSectionId(rs.getInt("sec_id"));
        section.setDate(rs.getDate("startdate").toLocalDate());
        section.setGitlab_group(rs.getString("gitlab_group"));
        section.setGitlab_token(rs.getString("gitlab_token"));
        
        String instructorNetId = rs.getString("instructor_netid");
        Instructor instructor = instructorDAO.getInstructorByNetId(instructorNetId);
        section.setInstructor(instructor);
    }
    


    // check if a section already exists 
    public boolean checkSectionExists(Section section) throws SQLException {
        String sql = "SELECT 1 FROM adtg.section WHERE c_subject = ? AND c_number = ? AND sec_id = ?";
        
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            try (ResultSet resultSet = ps.executeQuery()) {
                return resultSet.next();
            }
        }

    }


    // ----------------------------- CRUD -----------------------------------------------


    public Section getSectionsByPK(String courseSubject, int courseNumber, int sectionId) {
        String sql = "SELECT * FROM section WHERE c_subject = ? AND c_number = ? AND sec_id = ?;";
        Section section = null;
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, courseSubject);
            ps.setInt(2, courseNumber);
            ps.setInt(3, sectionId);
    
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { // Assuming only one result is expected
                    section = mapSectionFromRS(rs);
                } else {
                    return null; // TODO: handle here
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error while retrieving section", e);
        }
        return section;
    }


    //get the section by student netID
    public List<Section> getSectionsByStudentNetId(String netId) throws SQLException {
        CourseDAO courseDAO = new CourseDAO(conn);
        SectionDAO sectionDAO = new SectionDAO(conn);
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.* " +
                    "FROM adtg.enrollment e " +
                    "JOIN adtg.section s ON e.c_subject = s.c_subject " +
                    "AND e.c_number = s.c_number " +
                    "AND e.sec_id = s.sec_id " +
                    "JOIN adtg.course c ON s.c_subject = c.c_subject " +
                    "AND s.c_number = c.c_number " +
                    "WHERE e.student_netid = ? ORDER BY s.c_subject ASC, s.c_number ASC";

        try (Connection connection = getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = courseDAO.createCourseFromRS(rs);
                    Section section = new Section(rs.getInt("sec_id"), course);
                    sectionDAO.mapSectionFromRS(rs, section);
                    sections.add(section);
                }
            }
        }
        return sections;
    }

    /**
     * Retrieves a section based on the given netId and course.
     *
     * @param netId  the netId of the student
     * @param course the course object
     * @return the section matching the netId and course
     * @throws IllegalArgumentException if no section is found for the given netId and course
     */
    public Section getSectionByNetIdAndCourse(String netId, Course course) throws IllegalArgumentException {
        Section result = null;
        try {
            List<Section> sections = getSectionsByStudentNetId(netId);
            for (Section section : sections) {
                if (section.getCourse().getSubject().equals(course.getSubject())
                        && section.getCourse().getNumber() == course.getNumber()) {
                    new SectionDAO(conn).loadWholeSection(section);
                    result = section;
                }
            }
        }
        catch (SQLException e) {
            System.out.println("Error in getSectionsByStudentNetId");
        }
        if (result != null) {
            return result;
        }
        else {
            throw new IllegalArgumentException("No section of " + netId + " in " + course.getSubject() + " " + course.getNumber());
        }
    }
   

    // for instructor portal, Get all sections by instructor NetId
    public List<Section> listSectionsByInstructorNetId(String netId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT s.*, c.title FROM adtg.section s " +
        "JOIN adtg.course c ON s.c_subject = c.c_subject AND s.c_number = c.c_number " +
        "WHERE s.instructor_netid = ? ORDER BY s.c_subject ASC, s.c_number AsC, s.sec_id ASC";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = new Course(rs.getString("c_subject").trim(), rs.getInt("c_number"));
                    course.setTitle(rs.getString("title"));  
                    Section section = new Section(rs.getInt("sec_id"), course); 
                    mapSectionFromRS(rs, section);  
                    sections.add(section);
                }
            }
        } 
        return sections;
    }



    // for instructor portal, only show sections belong to the instructor
    public List<Section> listSectionsByInstructorCourse(Course course, String netId)throws SQLException {

        List<Section> instructorSections = new ArrayList<>();
        String sql = "SELECT * FROM adtg.section WHERE c_subject = ? AND c_number = ? AND instructor_netid = ?";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            ps.setString(3, netId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Section section = new Section(rs.getInt("sec_id"), course); 
                    mapSectionFromRS(rs, section);
                    instructorSections.add(section);
                }
            }
        }
        return instructorSections;
    }

    

    // Get section by primary key
    public Section getSectionByPK(Course course, int sectionId) throws SQLException {
        String sql = "SELECT * FROM adtg.section WHERE c_subject = ? AND c_number = ? AND sec_id = ?";
        Section section = null;
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            ps.setInt(3, sectionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    section = new Section(rs.getInt("sec_id"), course);
                    mapSectionFromRS(rs, section);
                }
            }
        }
        return section;
    }


    
    public void loadWholeSection(Section section) throws SQLException{
        EnrollmentDAO enrollmentDAO = new EnrollmentDAO(conn);
        String sql = "SELECT * FROM adtg.section WHERE c_subject = ? AND c_number = ? AND sec_id = ? ";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject().trim());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    mapSectionFromRS(rs, section); 
                } else {
                    throw new SQLException("Section not found");
                }
            }
        }
        enrollmentDAO.loadBySection(section); // Load students
    }
    


    public void insertSection(Section section) throws SQLException, IllegalStateException {
        UserDAO userDAO = new UserDAO(conn);

        // Step 1: Check if the instructor exists
        String instructor_netId = section.getInstructor().getNetId();
        if (!userDAO.checkUserExists(instructor_netId)) {
            throw new IllegalStateException("User with NetID " + instructor_netId + " does not exist.");
        }

        // Step 2: Check if the user is a instructor
        if (!userDAO.checkUserRole(instructor_netId, "INSTRUCTOR")) {
            throw new IllegalStateException("User with NetID " + instructor_netId + " is not authorized as INSTRUCTOR.");

        }
    
        //step3: Check if the section exist
        if (checkSectionExists(section)) {
            throw new IllegalStateException("Section already exists");
        }

        // Step 4: Add the section
        String sql = "INSERT INTO adtg.section (c_subject, c_number, sec_id, instructor_netid, startdate, gitlab_group, gitlab_token) " +
         "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
                ps.setString(1, section.getCourse().getSubject());
                ps.setInt(2, section.getCourse().getNumber());
                ps.setInt(3, section.getSectionId());
                ps.setString(4, section.getInstructor().getNetId());
                 ps.setDate(5, Date.valueOf(section.getDate()));
                ps.setString(6, section.getGitlab_group());
                ps.setString(7, section.getGitlab_token());
        
            ps.executeUpdate();
        }
    }
  
    public void deleteSection(Section section) throws SQLException {
        String sql = "DELETE FROM adtg.section WHERE c_subject = ? AND c_number = ? AND sec_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, section.getCourse().getSubject().trim());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
    
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Section not found or could not be deleted");
            }
        }
    }
    

    public void updateSection(Section section) throws SQLException {
        String sql = "UPDATE adtg.section SET startdate = ?, gitlab_group = ?, gitlab_token = ?, instructor_netid = ? WHERE c_subject = ? AND c_number = ? AND sec_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(section.getDate()));
            ps.setString(2, section.getGitlab_group());
            ps.setString(3, section.getGitlab_token());
            ps.setString(4, section.getInstructor().getNetId());
            ps.setString(5, section.getCourse().getSubject().trim());
            ps.setInt(6, section.getCourse().getNumber());
            ps.setInt(7, section.getSectionId());
    
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Section not found or could not be updated");
            }
        }
    }
    



}

