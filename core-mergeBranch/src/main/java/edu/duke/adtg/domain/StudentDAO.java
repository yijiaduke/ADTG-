package edu.duke.adtg.domain;

import java.sql.*;
import java.util.*;


public class StudentDAO implements DAOFactory<Student> {

    private DAOConn conn;
    private Connection connection;
    CourseDAO courseDAO;

    public StudentDAO(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
        courseDAO = new CourseDAO(conn);
    }


    //allows changing the DAOConn object after the StudentDAO instance has been created
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



    //get the section by student netID
    public List<Section> getSectionsByStudentNetId(String netId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT section.c_subject, section.c_number, section.sec_id, section.instructor_netid, section.startdate " +
                     "FROM enrollment " +
                     "JOIN section ON enrollment.c_subject = section.c_subject " +
                     "AND enrollment.c_number = section.c_number " +
                     "AND enrollment.sec_id = section.sec_id " +
                     "WHERE enrollment.student_netid = ?";

        try (
            // Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Course course = courseDAO.createCourseFromRS(rs);
                Instructor instructor = new Instructor(rs.getString("instructor_netid"));
                Section section = new Section(
                    rs.getInt("sec_id"),
                    course,
                    rs.getDate("startdate").toLocalDate(),
                    instructor
                );
                sections.add(section);

            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return sections;
    }

    public void getStudentByNetid(Student student) throws SQLException{
        String sql = "select name, email from adtg.users where netid=? ;";
        try(PreparedStatement ps = connection.prepareStatement(sql)){
            setStatementObjects(ps, student.getNetId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                student.setEmail(rs.getString("email"));
                student.setName(rs.getString("name"));
            }
        }catch(SQLException e){
            e.printStackTrace();
            throw e;
        }

    }



    // //get the section by student netID
    // public List<Section> getSectionsByStudentNetId(String netId) throws SQLException {
    //     List<Section> sections = new ArrayList<>();
    //     String sql = "SELECT section.c_subject, section.c_number, section.sec_id, section.instructor_netid, section.startdate " +
    //                  "FROM enrollment " +
    //                  "JOIN section ON enrollment.c_subject = section.c_subject " +
    //                  "AND enrollment.c_number = section.c_number " +
    //                  "AND enrollment.sec_id = section.sec_id " +
    //                  "WHERE enrollment.student_netid = ?";

    //     try (
    //         // Connection connection = getConnection();
    //          PreparedStatement ps = connection.prepareStatement(sql)) {
    //         ps.setString(1, netId);
    //         ResultSet rs = ps.executeQuery();
    //         while (rs.next()) {
    //             Section section = new Section(
    //                 rs.getString("c_subject").trim(),  //for CHAR 
    //                 rs.getInt("c_number"),
    //                 rs.getInt("sec_id"), course, instructor
    //                 rs.getString("instructor_netid"),
    //                 rs.getDate("startdate").toLocalDate()
    //             );
    //             sections.add(section);
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace(); // Log the error for debugging
    //         throw e; // Re-throw the exception to let the service/controller handle it
    //     }
    //     return sections;
    // }

    // public List<Assessment> getStudentAssessmentsCompleted(String netId, Section section)throws SQLException{
    //     List<Assessment> ProcessingList = new ArrayList<>();
    //    //1. grade is 
    //     String course_subject = section.getCourseSubject();
    //     String 

    //     String sql = "SELECT assessment.assn, assessment.title, assessment.start_date, assessment.due, assessment.passing_score " +
    //              "FROM assessment " +
    //              "JOIN courses ON assessment.c_subject = course_subject " +
    //              "JOIN enrollments ON courses.id = enrollments.course_id " +
    //              "LEFT JOIN submissions ON assessment.id = submissions.assessment_id AND enrollments.student_id = submissions.student_id " +
    //              "WHERE enrollments.student_id = ? AND CURRENT_DATE <= assessment.due_date";

    // }


    // public List<Assessment> getStudentAssessmentsInProcess(String netId, Section section)throws SQLException{
    //     List<Assessment> ProcessingList = new ArrayList<>();
    //    //1. grade is 
    //     String section
    //     String sql = "SELECT assessment.assn, assessment.title, assessment.start_date, assessment.due, assessment.passing_score " +
    //              "FROM assessment " +
    //              "JOIN courses ON assessment.c_subject = courses.c_subject " +
    //              "JOIN enrollments ON courses.id = enrollments.course_id " +
    //              "LEFT JOIN submissions ON assessment.id = submissions.assessment_id AND enrollments.student_id = submissions.student_id " +
    //              "WHERE enrollments.student_id = ? AND CURRENT_DATE <= assessment.due_date";

    // }



    public Student getStudentByPK(String netId) throws SQLException {
        String sql = "SELECT * FROM adtg.users WHERE netid = ?;";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, netId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                        rs.getString("netid"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        } 
        return null;
    }



}

