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
public class CourseDAO implements DAOFactory<Course> {

    private final DAOConn conn;
    private static final Logger logger = LoggerFactory.getLogger(CourseDAO.class);

    @Autowired
    public CourseDAO(DAOConn conn) {
        this.conn = conn;
    }

    @Override
    public void setConnection(DAOConn conn) {
        // Not needed as we are using dependency injection
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


    public void map(ResultSet rs,Course course) throws SQLException {
        if (!rs.next()) {
            // No data found
            return;
        }

        course.setTitle(rs.getString("title"));
        course.setRepo(rs.getString("gitlab_repo"));
        course.setRepoToken(rs.getString("gitlab_token"));
    }



    public void load(Course course) {
        String sql = "SELECT * FROM adtg.course WHERE c_subject = ? AND c_number = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps1 = connection.prepareStatement(sql)) {
            setStatementObjects(ps1, course.getSubject(), course.getNumber());
            ps1.execute();
            ResultSet rs = ps1.getResultSet();
            map(rs, course);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

     
    public Course createCourseFromRS(ResultSet rs) throws SQLException {
        return new Course(
            rs.getString("c_subject").trim(),
            rs.getInt("c_number")
        );
    }

    
    // map all fields for course object
    public void mapCourseFromRS(ResultSet rs,Course course) throws SQLException {
        course.setTitle(rs.getString("title"));
        course.setRepo(rs.getString("gitlab_repo"));
        course.setRepoToken(rs.getString("gitlab_token"));
    }


    // map the  List<Section> sectionList for course
    public void mapSections(ResultSet rs, Course course) throws SQLException {
        List<Section> sections = new ArrayList<>();
        while (rs.next()) {
            Section section = new Section(rs.getInt("sec_id"), course);
            section.setInstructor(new Instructor(rs.getString("instructor_netid"), null, null));
            section.setDate(rs.getDate("startdate").toLocalDate());
            section.setGitlab_group(rs.getString("gitlab_group"));
            section.setGitlab_token(rs.getString("gitlab_token"));
            sections.add(section);
        }
        course.setSectionList(sections);
    }
    
    
    // check if a course exists
    public boolean checkCourseExist(String subject, int number) throws SQLException {
        String sql = "SELECT 1 FROM adtg.course WHERE c_subject = ? AND c_number = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, subject);
            ps.setInt(2, number);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    
    // ----------------------------- Handle TA -----------------------------------------------
    

    public boolean checkTAexist(Course course, String netId) throws SQLException {
        String sql = "SELECT 1 FROM adtg.course_ta WHERE c_subject = ? AND c_number = ? AND ta_netid = ?";
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
    
    
    public List<Course> listCourseByTANetId(String netId) throws SQLException {
        List<Course> courses = new ArrayList<>();
        String sql = "SELECT * FROM adtg.course c JOIN adtg.course_ta ta ON c.c_subject = ta.c_subject AND c.c_number = ta.c_number WHERE ta.ta_netid = ? ORDER BY c.c_subject ASC, c.c_number ASC";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Course course = createCourseFromRS(rs);
                    courses.add(course);
                }
            }
        } 
        return courses;
    }


    // list all TAs of a course
    public List<User> listTAforCourse(Course course) throws SQLException {
        UserDAO userDAO = new UserDAO(conn);
        List<User> TAs = new ArrayList<>();
        String sql = "SELECT * FROM adtg.course_ta WHERE c_subject = ? AND c_number = ? ORDER BY ta_netid ASC";

        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String taNetId = rs.getString("ta_netid");
                    User ta = userDAO.getUserByPK(taNetId);
                    if (ta != null) {
                        TAs.add(ta);
                    }
                }
            }
        }
        return TAs;
    }



    public void insertCourseTA(Course course, String netId) throws SQLException, IllegalStateException {
        UserDAO userDAO = new UserDAO(conn);
        
        // Step 1: Check if the student exists
        if (!userDAO.checkUserExists(netId)) {
            throw new IllegalStateException("User with NetID " + netId + " does not exist.");
        }
    
        // Step 2: Check if the user is a TA
        if (!userDAO.checkUserRole(netId, "TA")) {
            throw new IllegalStateException("User with NetID " + netId + " is not authorized as TA.");
        }
    
        // Step 3: Check if the TA already exists for the course
        if (checkTAexist(course, netId)) {
            throw new IllegalStateException("TA with NetID " + netId + " already exists for Course " + course.getSubject() + " " + course.getNumber());
        }
    
        // Step 4: Add the TA to the course
        String sql = "INSERT INTO adtg.course_ta (c_subject, c_number, ta_netid) VALUES (?, ?, ?)";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            ps.setString(3, netId);
            ps.executeUpdate();
        }
    }
        

    public void deleteCourseTA(Course course, String netId) throws SQLException {
        String sql = "DELETE FROM adtg.course_ta WHERE c_subject = ? AND c_number = ? AND ta_netid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            ps.setString(3, netId);
            ps.executeUpdate();
        }
    }

    

    // ----------------------------- CRUD ----------------------------------------------------


    public void insertCourse(Course course) throws SQLException, IllegalStateException {

        //check if course exist
        if (checkCourseExist(course.getSubject(), course.getNumber())) {
            throw new IllegalStateException("Course with subject " + course.getSubject() + " and number " + course.getNumber() + " already exists.");
        }

        //insert course
        String sql = "INSERT INTO adtg.course (c_subject, c_number, title, gitlab_repo, gitlab_token) VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getSubject());
            ps.setInt(2, course.getNumber());
            ps.setString(3, course.getTitle());
            ps.setString(4, course.getRepo());
            ps.setString(5, course.getRepoToken());
            ps.executeUpdate();
        }
    }



    public List<Course> listAllCourse()throws SQLException {
        String sql = "SELECT * FROM adtg.course ORDER BY c_subject ASC, c_number ASC, title ASC";
        List<Course> courses = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Course course = createCourseFromRS(rs);
                mapCourseFromRS(rs, course);
                courses.add(course);
            }
        }
        return courses;
    }
    

    

    public void loadWholeCourse(Course course) throws SQLException {
        SectionDAO sectionDAO = new SectionDAO(conn);

        String sql = "SELECT * FROM adtg.course WHERE c_subject = ? AND c_number = ?";
        String sql2 = "SELECT * FROM adtg.section WHERE c_subject = ? AND c_number = ?";
    
        try (Connection connection = getConnection();
             PreparedStatement coursePs = connection.prepareStatement(sql);
             PreparedStatement sectionsPs = connection.prepareStatement(sql2)) {
    
            coursePs.setString(1, course.getSubject().trim());
            coursePs.setInt(2, course.getNumber());
            try (ResultSet courseRs = coursePs.executeQuery()) {
                if (courseRs.next()) {
                    mapCourseFromRS(courseRs, course);
                } else {
                    throw new SQLException("No course found with subject: " + course.getSubject() + " and number: " + course.getNumber());
                }
            }
    
            // Load sections
            List<Section> sections = new ArrayList<>();

            sectionsPs.setString(1, course.getSubject().trim());
            sectionsPs.setInt(2, course.getNumber());
            try (ResultSet sectionsRs = sectionsPs.executeQuery()) {
               while (sectionsRs.next()) {
                    Section section = sectionDAO.mapSectionFromRS(sectionsRs);
                    sections.add(section);
                }
            }
            course.setSectionList(sections);
        }
    }

    //after seclect course, instructor portal to load course details and sections details( belong to instructor)
    public void loadCourseForInstructor(Course course, String netId) throws SQLException{
        SectionDAO sectionDAO = new SectionDAO(conn);

        String sql = "SELECT * from adtg.course WHERE c_subject = ? AND c_number = ?";

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setString(1, course.getSubject().trim());
            ps.setInt(2, course.getNumber());
            try(ResultSet rs = ps.executeQuery()){
                if(rs.next()){
                    mapCourseFromRS(rs, course);
                }else {
                    throw new SQLException("No course found with subject: " + course.getSubject() + " and number: " + course.getNumber());
                }
            }

            // Load sections belongs to the instructor
            List<Section> instructorSections = sectionDAO.listSectionsByInstructorCourse(course, netId);
            course.setSectionList(instructorSections);
        }
    }




}

