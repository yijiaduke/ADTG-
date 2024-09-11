package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class StudentDAO implements DAOFactory<Student> {

    private final DAOConn conn;
    private final CourseDAO courseDAO;

    @Autowired
    public StudentDAO(DAOConn conn) {
        this.conn = conn;
        this.courseDAO = new CourseDAO(conn);
    }

    // Allows changing the DAOConn object after the StudentDAO instance has been created
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


    //get the section by student netID
    public List<Section> getSectionsByStudentNetId(String netId) throws SQLException {
        List<Section> sections = new ArrayList<>();
        String sql = "SELECT section.c_subject, section.c_number, section.sec_id, section.instructor_netid, section.startdate " +
                     "FROM enrollment " +
                     "JOIN section ON enrollment.c_subject = section.c_subject " +
                     "AND enrollment.c_number = section.c_number " +
                     "AND enrollment.sec_id = section.sec_id " +
                     "WHERE enrollment.student_netid = ?";

        try (Connection connection = getConnection();
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
        } 
        return sections;
    }

    public void getStudentByNetid(Student student) throws SQLException{
        String sql = "select name, email from adtg.users where netid=? ;";
        try (Connection connection = getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            setStatementObjects(ps, student.getNetId());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                student.setEmail(rs.getString("email"));
                student.setName(rs.getString("name"));
            }
        }
    }


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

