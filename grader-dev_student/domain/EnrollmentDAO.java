package edu.duke.adtg.domain;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentDAO {
    private DAOConn conn;
    private Connection connection;

    public EnrollmentDAO(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
    }


    public void setConnection(DAOConn conn) {
        this.conn = conn;
        connection = getConnection();
    }

    public Connection getConnection() {
        try {
            return conn.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Student mapStudent(ResultSet rs) throws SQLException{
        StudentDAO studentDAO = new StudentDAO(conn);
        Student student = new Student(rs.getString("student_netid"));
        studentDAO.getStudentByNetid(student);
        return student;
    }

    public void loadBySection(Section section) throws SQLException{
        String sql = "select * from adtg.enrollment where  c_subject = ? and c_number = ? and sec_id = ?;";
        List<Student> stus = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)){
            ps.setString(1, section.getCourse().getSubject());
            ps.setInt(2, section.getCourse().getNumber());
            ps.setInt(3, section.getSectionId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Student stu = mapStudent(rs);
                stus.add(stu);
            }
            section.setEnrollStudents(stus);
        }catch(SQLException e){
            e.printStackTrace();
            throw e;
        }
    }   
}
